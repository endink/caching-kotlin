package com.labijie.caching

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
internal class CacheScopeObject(private val cacheScopeHolder: ICacheScopeHolder, vararg prevent: CacheOperation) :AutoCloseable {

    var parent: ICacheScopeHolder.ScopeSettings? = null

    init {
        val hasParentScope = cacheScopeHolder.isInScope
        if(hasParentScope){
            parent = cacheScopeHolder.settings
        }
        cacheScopeHolder.settings = ICacheScopeHolder.ScopeSettings(prevent)
    }

    override fun close() {
        cacheScopeHolder.settings = parent
    }

}