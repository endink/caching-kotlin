package com.labijie.caching

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-22
 */
interface ICacheScopeHolder {
    val isInScope: Boolean
    var settings: ScopeSettings?

    companion object {
        var Current: ICacheScopeHolder? = null
            internal set
    }

    fun cacheRequired(operation: CacheOperation): Boolean {
        return !isInScope || !(settings?.preventOperations?.contains(operation) ?: false)
    }

    class ScopeSettings(var preventOperations: Array<out CacheOperation> = arrayOf())
}