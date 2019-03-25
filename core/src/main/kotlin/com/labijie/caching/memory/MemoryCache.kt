package com.labijie.caching.memory

import com.labijie.caching.CacheException
import com.labijie.caching.CacheItemPriority
import com.labijie.caching.EvictionReason
import com.labijie.caching.IChangeToken
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.function.Consumer

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
class MemoryCache(options: MemoryCacheOptions) : AutoCloseable {
    private val entries: ConcurrentHashMap<Any, CacheEntry> = ConcurrentHashMap()
    private var closed: Boolean = false
    private val setEntry: Consumer<CacheEntry>
    private val entryExpirationNotification: Consumer<CacheEntry>

    private val expirationScanFrequencyMilliseconds: Long
    private var lastExpirationScan: LocalDateTime = LocalDateTime.now(ZoneOffset.UTC)

    init {
        setEntry = Consumer { this.setEntry(it) }
        this.entryExpirationNotification = Consumer { this.entryExpired(it) }
        this.expirationScanFrequencyMilliseconds = options.scanFrequency.toMillis()

        if (options.isCompact) {
            GcNotification.register(null, Consumer {
                this.doMemoryPreassureCollection()
            })
        }
    }

    private fun checkClosed() {
        if (closed) {
            throw CacheException("memory cache was closed.")
        }
    }

    /**
     * 获取当前缓存的大小。
     * @return
     */
    val size: Int
        get() = this.entries.size

    fun createEntry(key: Any): CacheEntry {
        checkClosed()
        //该缓存项并不会被添加到缓存队列，而是等待GC 收集时才会被添加。
        return CacheEntry(
            key,
            this.setEntry,
            this.entryExpirationNotification
        )
    }

    /**
     * 根据缓存键获取缓存对象（注意不是获取缓存项 CacheEntry，而是真实缓存对象）。
     * @param key
     * @return
     */
    fun getOrDefault(key: Any, defaultValue: Any?): Any? {
        checkClosed()

        var result: Any? = null
        val utcNow = LocalDateTime.now(ZoneOffset.UTC)

        val entry = this.entries.getOrDefault(key, null)
        if (entry != null) {
            // 由于使用惰性过期算法，首先去检查是否过期，过期直接移除。
            if (entry.checkExpired(utcNow) && entry.evictionReason !== EvictionReason.Replaced) {
                removeEntry(entry)
            } else {
                entry.lastAccessed = utcNow
                result = entry.value

                // 当缓存项是在其他上下文创建时，需要复制过期令牌。
                entry.propagateOptions(CacheEntryHelper.current)
            }
        }
        startScanForExpiredItems()

        return result ?: defaultValue
    }

    fun remove(key: Any) {
        checkClosed()
        val entry = this.entries.remove(key)
        if (entry != null) {
            entry.setExpired(EvictionReason.Removed)
            entry.invokeEvictionCallbacks()
        }

        startScanForExpiredItems()
    }

    fun <T> get(key: Any): T? {
        @Suppress("UNCHECKED_CAST")
        return this.getOrDefault(key, null) as? T
    }

    fun <T> set(key: Any, value: T, options: MemoryCacheEntryOptions?): T {
        val entry = this.createEntry(key)
        if (options != null) {
            MemoryCacheEntryOptions.configureCacheEntry(entry, options)
        }
        entry.value = value
        // 必须手动调用 close，而不能使用 try\final 结构，中间过程出错无法调用到 close 不会将缓存添加到缓存列表
        entry.close()

        return value
    }

    fun <T> set(key: Any, value: T, expirationToken: IChangeToken): T {
        val options = MemoryCacheEntryOptions()
        options.expirationTokens.add(expirationToken)
        return this.set<T>(key, value, options)
    }

    fun <T> set(key: Any, value: T, absoluteExpirationUtc: LocalDateTime?): T {
        var options: MemoryCacheEntryOptions? = null
        if (absoluteExpirationUtc != null) {
            options = MemoryCacheEntryOptions()
            options.absoluteExpiration = absoluteExpirationUtc
        }
        return this.set(key, value, options)
    }

    fun <T> set(key: Any, value: T, slidingExpirationMilliseconds: Long): T {
        if (slidingExpirationMilliseconds <= 0) {
            throw CacheException("slidingExpirationMilliseconds must greater than 0")
        }
        var options = MemoryCacheEntryOptions()
        options.slidingExpirationMilliseconds = slidingExpirationMilliseconds
        return this.set<T>(key, value, options)
    }

    //添加一个缓存对象到HASH表（当GC回收时可以自动添加），用于防止对象被意外收集（由于JVM GC过程无法 Hook，暂时无效，或许可以使用 finalize？）
    private fun setEntry(entry: CacheEntry) {
        if (closed) {
            return
        }

        val utcNow = LocalDateTime.now(ZoneOffset.UTC)

        // 实体被添加的时间进行记录（LUA算法）
        entry.lastAccessed = utcNow

        val priorEntry = this.entries.getOrDefault(entry.key, null)
        priorEntry?.setExpired(EvictionReason.Replaced)

        if (!entry.checkExpired(utcNow)) {
            var entryAdded = if (priorEntry == null) {
                // 注意：只在没有存在键时候添加。
                val inMap = this.entries.computeIfAbsent(entry.key) { entry }
                inMap === entry //同 一个引用表明已经被添加。
            } else {
                // 这里表示新的项没有被添加进去，可能 getOrDefault 之后有其他线程去动了缓存项。
                //发生这种情况时候看看新的项是不是我们添加进去的，通过 replace 确认一下
                val added = this.entries.replace(entry.key, priorEntry, entry)

                if (!added) {
                    // 确定有其他线程动过这个的缓存项了，尝试看看是不是过期了，不是过期以另外一个线程给进去的值稳准。
                    // 如果过期了要主要不要让他过期，所以 Absent 确保有值，我们正在做 set 啊，没值会很奇怪。
                    val inMap = this.entries.computeIfAbsent(entry.key) { entry }
                   (inMap === entry)
                }
                added
            }

            if (entryAdded) {
                entry.attachTokens()
            } else {
                entry.setExpired(EvictionReason.Replaced)
                entry.invokeEvictionCallbacks()
            }

            priorEntry?.invokeEvictionCallbacks()
        } else {
            entry.invokeEvictionCallbacks()
            if (priorEntry != null) {
                this.removeEntry(priorEntry)
            }
        }
        startScanForExpiredItems()
    }

    private fun entryExpired(entry: CacheEntry) {
        //TODO: 应该考虑批量处理？
        removeEntry(entry)
        startScanForExpiredItems()
    }

    private fun startScanForExpiredItems() {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        if (this.lastExpirationScan.plus(
                this.expirationScanFrequencyMilliseconds,
                ChronoUnit.MILLIS
            ) < now
        ) {
            this.lastExpirationScan = now
            val cache = this
            MemoryCache.executionThreadPool.execute { scanForExpiredItems(cache) }
        }
    }

    private fun removeEntry(entry: CacheEntry) {
        if (this.entries.remove(entry.key, entry)) {
            entry.invokeEvictionCallbacks()
        }
    }

    /// 在内存紧张时通过调用此方法来回收内存，但是内存真正回收时间取决于下一次的GC.
    /// 回收 10% 条目以压缩内存。
    private fun doMemoryPreassureCollection(): Boolean {
        if (closed) {
            return false
        }

        compact(0.10)

        return true
    }

    /**
     * 指定一个百分比 (0.10 for 10%) 元素数（或者内存？）对缓存, 移除使用以下策略:
     * 1. 移除所有已过期的缓存项。
     * 2.  不同的 CacheItemPriority 按桶分装。
     * 3. 排列出“最不活跃”（最后一次使用时间距离现在最远）的项.
     * 可以考虑的策略： 最接近的绝对过期时间优先清理。
     * 可以考虑的策略： 最接近的滑动过期时间优先清理。
     * 可以考虑的策略： 占用内存最大的对象优先清理。
     * @param percentage 收缩百分比，1表示全部收缩。
     */
    fun compact(percentage: Double) {
        val entriesToRemove = ArrayList<CacheEntry>()
        val lowPriEntries = ArrayList<CacheEntry>()
        val normalPriEntries = ArrayList<CacheEntry>()
        val highPriEntries = ArrayList<CacheEntry>()

        //  按缓存项优先级和是否过期装桶。
        val utcNow = LocalDateTime.now(ZoneOffset.UTC)
        for (entry in this.entries.values) {
            if (entry.checkExpired(utcNow)) {
                entriesToRemove.add(entry)
            } else {
                when (entry.priority) {
                    CacheItemPriority.Low -> lowPriEntries.add(entry)
                    CacheItemPriority.Normal -> normalPriEntries.add(entry)
                    CacheItemPriority.High -> highPriEntries.add(entry)
                    CacheItemPriority.NeverRemove -> {
                    }
                    else -> throw CacheException("Not implemented: " + entry.priority!!)
                }
            }
        }

        val removalCountTarget = (this.entries.size * percentage).toInt()

        expirePriorityBucket(removalCountTarget, entriesToRemove, lowPriEntries)
        expirePriorityBucket(removalCountTarget, entriesToRemove, normalPriEntries)
        expirePriorityBucket(removalCountTarget, entriesToRemove, highPriEntries)

        entriesToRemove.forEach(Consumer { this.removeEntry(it) })
    }

    // 处理每个优先级桶的元素过期。
    private fun expirePriorityBucket(
        removalCountTarget: Int,
        entriesToRemove: MutableList<CacheEntry>,
        priorityEntries: List<CacheEntry>
    ) {
        // 移除指标达不到不操作?或许可以改进
        if (removalCountTarget <= entriesToRemove.size) {
            return
        }

        //桶中的元素数量未达到移除指标，无脑全部移除。
        if (entriesToRemove.size + priorityEntries.size <= removalCountTarget) {
            for (entry in priorityEntries) {
                entry.setExpired(EvictionReason.Capacity)
            }
            entriesToRemove.addAll(priorityEntries)
            return
        }

        //桶中的元素数量超出了移除指标，事实 LRU 算法（先排序，在移除）
        val sortedList = priorityEntries.sortedBy { it.lastAccessed }

        for (entry in sortedList) {
            entry.setExpired(EvictionReason.Capacity)
            entriesToRemove.add(entry)
            if (removalCountTarget <= entriesToRemove.size) {
                break
            }
        }
    }

    @Throws(Exception::class)
    override fun close() {
        if (!this.closed) {
            this.closed = true
            this.entries.clear()
        }
    }

    companion object {

        /**
         * MemoryCache 所有异步操作使用的统一线程池。
         *
         * @return
         */
        val executionThreadPool = Executors.newCachedThreadPool()!!

        @JvmStatic
        private fun scanForExpiredItems(cache: MemoryCache) {
            val now = LocalDateTime.now(ZoneOffset.UTC)
            val toRemove = ArrayList<CacheEntry>()
            cache.entries.values.stream().filter { entry -> entry.checkExpired(now) }.forEach { e -> toRemove.add(e) }

            toRemove.stream().forEach { cache.removeEntry(it) }
        }
    }
}