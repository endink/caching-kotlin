package com.labijie.caching.memory

import com.labijie.caching.*
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
class CacheEntry
/**
 * 创建内存缓存项的新实例。
 * @param key 缓存键
 * @param notifyCacheEntryReleased 缓存项失效时的回调通知的函数。
 * @param notifyCacheOfExpiration 缓存项过期时的回调通知的函数。
 */
    (
    /**
     * 获取缓存项的键。
     * @return
     */
    val key: Any,
    private val notifyCacheEntryReleased: Consumer<CacheEntry>,
    private val notifyCacheOfExpiration: Consumer<CacheEntry>
) : AutoCloseable {
    private var added: Boolean = false

    /**
     * 获取缓存移除的原因。
     * @return
     */
    internal var evictionReason: EvictionReason? = null
    private var expirationTokenRegistrations: MutableList<Closeable>? = null
    private var isExpired: Boolean = false
    private var expirationTokensValue: MutableList<IChangeToken>? = null
    /**
     * 获取缓存项的绝对过期时间（Utc）。
     * @return
     */
    var absoluteExpiration: LocalDateTime? = null
    /**
     * 获取缓存项的滑动过期时间（单位：毫秒）。
     * @return
     */
    var slidingExpirationMilliseconds: Long? = null
        set(milliseconds) {
            if (milliseconds != null && milliseconds.toInt() <= 0) {
                throw IllegalArgumentException("setSlidingExpirationMilliseconds 方法 milliseconds 必须大于 0 或为空。")
            }
            field = milliseconds
        }
    /**
     * 获取缓存项最后访问时间（用于 LRU 算法）。
     * @return
     */
    /**
     * 设置缓存项最后访问时间（用于 LRU 算法）。
     * @return
     */
    internal var lastAccessed: LocalDateTime? = null
    private val lock = Any()

    private var postEvictionCallbacksValue: MutableList<PostEvictionCallbackRegistration>? = null
    /**
     * 获取缓存项的值。
     * @return
     */
    /**
     * 设置缓存项的值。
     * @param value
     */
    var value: Any? = null
    private val scope: AutoCloseable
    /**
     * 获取缓存项的优先级，该优先级在处理内存收缩时会影响缓存清理策略。
     * @return
     */
    var priority: CacheItemPriority? = null

    init {
        this.priority = CacheItemPriority.Normal

        scope = CacheEntryHelper.enterScope(this)

    }

    /**
     * 获取缓移除时的回调链列表。
     * @return
     */
    val postEvictionCallbacks: MutableList<PostEvictionCallbackRegistration>
        get() {
            if (postEvictionCallbacksValue == null) {
                postEvictionCallbacksValue = mutableListOf()
            }
            return postEvictionCallbacksValue!!
        }

    /**
     * 注册缓存项失效的回调方法。
     * @param callback 回调函数。
     * @param state 回调函数传递的状态对象。
     */
    fun registerPostEvictionCallback(callback: IPostEvictionCallback, state: Any?) {
        this.postEvictionCallbacks.add(PostEvictionCallbackRegistration(callback, state))
    }


    /**
     * 获取用于处理缓存过期的令牌。
     * @return
     */
    val expirationTokens: MutableList<IChangeToken>
        get() {
            if (this.expirationTokensValue == null) {
                this.expirationTokensValue = mutableListOf()

            }
            return expirationTokensValue!!
        }

    /**
     * 设置相对于当前时间的缓存项绝对过期时间（单位：毫秒）。
     * @param milliseconds 相对于当前时间的毫秒数。
     */
    fun setAbsoluteExpirationRelativeToNowMS(milliseconds: Long?) {
        if (milliseconds != null && milliseconds.toInt() <= 0) {
            throw IllegalArgumentException("setAbsoluteExpirationRelativeToNowMS 方法参数 milliseconds 必须大于 0 或为空。")
        }
        if (milliseconds == null) {
            this.absoluteExpiration = null
        } else {
            val time = LocalDateTime.now(ZoneOffset.UTC).plus(milliseconds, ChronoUnit.MILLIS)
            this.absoluteExpiration = time
        }
    }

    /**
     * 强制缓存项过期。
     * @param reason 过期原因。
     */
    fun setExpired(reason: EvictionReason) {
        if (this.evictionReason != EvictionReason.None) {
            this.evictionReason = reason
        }
        this.isExpired = true
        this.detachTokens()
    }

    /**
     * 检查缓存项是否过期。
     * @param now 判断过期的基准时间。
     * @return
     */
    fun checkExpired(now: LocalDateTime): Boolean {
        return this.isExpired || checkForExpiredTime(now) || checkForExpiredTokens()
    }

    private fun checkForExpiredTime(now: LocalDateTime): Boolean {
        if (this.absoluteExpiration != null && this.absoluteExpiration!! <= now) {
            setExpired(EvictionReason.Expired)
            return true
        }

        if (this.slidingExpirationMilliseconds != null && this.lastAccessed!!.plus(
                this.slidingExpirationMilliseconds!!,
                ChronoUnit.MILLIS
            ) < now
        ) {
            setExpired(EvictionReason.Expired)
            return true
        }
        return false
    }

    private fun checkForExpiredTokens(): Boolean {
        if (this.expirationTokensValue != null) {
            for (expiredToken in this.expirationTokens) {
                if (expiredToken.hasChanged()) {
                    this.setExpired(EvictionReason.TokenExpired)
                    return true
                }
            }
        }
        return false
    }

    internal fun attachTokens() {
        if (this.expirationTokensValue != null) {
            synchronized(this.lock) {
                for (expirationToken in this.expirationTokens) {
                    if (expirationToken.enableActiveChangeCallbacks()) {
                        if (this.expirationTokenRegistrations == null) {
                            this.expirationTokenRegistrations = ArrayList(1)
                        }
                        val registration = expirationToken.registerChangeCallback(expirationCallback, this)
                        this.expirationTokenRegistrations!!.add(registration)
                    }
                }
            }
        }
    }

    private fun detachTokens() {
        synchronized(lock) {
            val registrations = expirationTokenRegistrations
            if (registrations != null) {
                expirationTokenRegistrations = null
                //关闭一下。
                for (registration in registrations) {
                    try {
                        registration.close()
                    } catch (e: IOException) {
                    }

                }
            }
        }
    }

    internal fun invokeEvictionCallbacks() {
        if (this.postEvictionCallbacksValue != null) {
            val entry = this
            MemoryCache.executionThreadPool.execute { invokeCallbacks(entry) }
        }
    }

    override fun close() {
        if (!added) {
            added = true
            try {
                scope.close()
            } catch (e: Exception) {
                throw RuntimeException(e.message, e)
            }

            notifyCacheEntryReleased.accept(this)
            //多线程下，可能会通过子线程调用close 因此，需要处理线程栈，要求 CacheEntry 总是成对的 create 和 close 调用。
            //可以解决在 create 之后发起其他线程调用后状态保持问题。
            propagateOptions(CacheEntryHelper.current)
        }
    }

    internal fun propagateOptions(parent: CacheEntry?) {
        if (parent == null) {
            return
        }

        // 复制 expiration tokens 和  AbsoluteExpiration 到缓存项层次.
        // 无需关心是否已经被缓存 Token 总是和缓存项关联。
        if (expirationTokensValue != null) {
            synchronized(lock) {
                synchronized(parent.lock) {
                    for (expirationToken in expirationTokens) {
                        parent.expirationTokens.add(expirationToken)
                    }
                }
            }
        }

        if (absoluteExpiration != null) {
            if (parent.absoluteExpiration == null || absoluteExpiration!!.compareTo(parent.absoluteExpiration!!) < 0) {
                parent.absoluteExpiration = absoluteExpiration
            }
        }
    }

    //处理引用比较提高性能。
    override fun equals(other: Any?): Boolean {
        return this === other
    }

    companion object {

        private val expirationCallback = Consumer<Any> { expirationTokensExpired(it) }

        private val logger = LoggerFactory.getLogger(CacheEntry::class.java)

        private fun expirationTokensExpired(obj: Any) {
            MemoryCache.executionThreadPool.execute {
                val entry = obj as CacheEntry
                entry.setExpired(EvictionReason.TokenExpired)
                entry.notifyCacheOfExpiration.accept(entry)
            }
        }

        private fun invokeCallbacks(entry: CacheEntry) {
            val atom = AtomicReference<List<PostEvictionCallbackRegistration>?>()
            atom.set(entry.postEvictionCallbacks)

            //在进行一次回调以后需要清理。
            val callbackRegistrations = atom.getAndUpdate {
                null
            } ?: return
            for (registration in callbackRegistrations) {
                val callback = registration.evictionCallback
                try {
                    callback.callback(entry.key, entry.value!!, entry.evictionReason!!, registration.state)
                } catch (e: Throwable) {
                    logger.warn("call back fault.", e)
                }
            }
        }
    }
}