package com.labijie.caching.memory

import com.labijie.caching.CacheException
import com.labijie.caching.CacheItemPriority
import com.labijie.caching.IChangeToken
import com.labijie.caching.PostEvictionCallbackRegistration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.ArrayList

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
class MemoryCacheEntryOptions {
    /**
     * 获取缓存项绝对过期时间。
     * @return
     */
    var absoluteExpiration: LocalDateTime? = null
    /**
     * 获取缓存项滑动过期时间。
     * @return
     */
    var slidingExpirationMilliseconds: Long? = null
        set(milliseconds) {
            if (milliseconds != null && milliseconds.toLong() <= 0) {
                throw CacheException("The slidingExpirationMilliseconds for memory cache must be null or greater than 0")
            }
            field = milliseconds
        }
    /**
     * 获取缓存项令牌集合。
     * @return
     */
    val expirationTokens: MutableList<IChangeToken> = mutableListOf()
    /**
     * 获取缓存移除时的回调列表。
     * @return
     */
    val postEvictionCallbacks: MutableList<PostEvictionCallbackRegistration> = mutableListOf()
    /**
     * 获取缓存项优先级。
     * @return
     */
    var priority: CacheItemPriority = CacheItemPriority.Normal

    /**
     * 设置相对于当前时间的绝对过期时间（单位：毫秒）
     * @param milliseconds  相对于当前时间的毫秒数。
     */
    fun setAbsoluteExpirationRelativeToNow(milliseconds: Long?) {
        if (milliseconds != null && milliseconds.toInt() <= 0) {
            throw CacheException("The milliseconds must be greater than 0 or null.")
        }
        if (milliseconds == null) {
            this.absoluteExpiration = null
        } else {
            val exp = LocalDateTime.now(ZoneOffset.UTC).plus(milliseconds, ChronoUnit.MILLIS)
            this.absoluteExpiration = exp
        }
    }

    companion object {

        internal fun configureCacheEntry(entry: CacheEntry, options: MemoryCacheEntryOptions) {
            entry.absoluteExpiration = options.absoluteExpiration
            entry.slidingExpirationMilliseconds = options.slidingExpirationMilliseconds
            entry.priority = options.priority

            val tokens = entry.expirationTokens
            for (token in options.expirationTokens) {
                tokens.add(token)
            }

            for (postEvictionCallback in options.postEvictionCallbacks) {
                entry.registerPostEvictionCallback(
                    postEvictionCallback.evictionCallback,
                    postEvictionCallback.state
                )
            }
        }
    }
}