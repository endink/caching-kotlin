package com.labijie.caching.annotation

import com.labijie.caching.TimePolicy
import com.labijie.caching.CacheLocation
import org.springframework.boot.autoconfigure.cache.CacheType
import java.lang.annotation.Inherited
import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-22
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class Cache(
    val key:String,
    val expireMills:Long,
    val region:String = "",
    val timePolicy:TimePolicy = TimePolicy.Absolute,
    val ignoreCacheError:Boolean = true) {
}