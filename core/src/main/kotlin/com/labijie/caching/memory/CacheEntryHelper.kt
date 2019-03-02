package com.labijie.caching.memory

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
object CacheEntryHelper {
    private val currentScopes = InheritableThreadLocal<CacheEntryStack>()

    var scopes: CacheEntryStack?
        get() = currentScopes.get()
        set(entry) = currentScopes.set(entry)

    private val orCreateScopes: CacheEntryStack
        get() {
            var scopes: CacheEntryStack? = CacheEntryHelper.scopes
            if (scopes == null) {
                scopes = CacheEntryStack.createEmpty()
                scopes = scopes
            }

            return scopes
        }

    val current: CacheEntry?
        get() {
            val stack = orCreateScopes
            return stack.peek()
        }

    fun enterScope(entry: CacheEntry): AutoCloseable {
        val scopes = orCreateScopes

        val scopeLease = ScopeLease(scopes)
        val stack = scopes.push(entry)
        CacheEntryHelper.scopes = stack
        return scopeLease
    }


    private class ScopeLease(private val cacheEntryStack: CacheEntryStack) : AutoCloseable {

        @Throws(Exception::class)
        override fun close() {
            CacheEntryHelper.scopes = cacheEntryStack
        }
    }
}