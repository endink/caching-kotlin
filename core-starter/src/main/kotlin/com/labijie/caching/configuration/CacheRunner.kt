package com.labijie.caching.configuration

import com.labijie.caching.ICacheScopeHolder
import org.springframework.boot.CommandLineRunner

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
class CacheRunner(private val cacheScopeHolder: ICacheScopeHolder):CommandLineRunner {
    override fun run(vararg args: String?) {
        if(ICacheScopeHolder.Current == null) {
            ICacheScopeHolder.Current = cacheScopeHolder
        }
    }
}