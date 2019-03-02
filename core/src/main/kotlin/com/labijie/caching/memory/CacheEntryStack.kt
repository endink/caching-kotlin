package com.labijie.caching.memory

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
class CacheEntryStack {
    private val previous: CacheEntryStack?
    private val entry: CacheEntry?

    private constructor() {
        previous = null
        entry = null
    }

    private constructor(previous: CacheEntryStack, entry: CacheEntry) {

        this.previous = previous
        this.entry = entry
    }

    fun push(c: CacheEntry): CacheEntryStack {
        return CacheEntryStack(this, c)
    }

    fun peek(): CacheEntry? {
        return entry
    }

    companion object {
        private val empty = CacheEntryStack()

        fun createEmpty(): CacheEntryStack {
            return empty
        }
    }
}
