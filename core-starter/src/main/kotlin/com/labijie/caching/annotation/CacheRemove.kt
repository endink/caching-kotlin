package com.labijie.caching.annotation

import com.labijie.caching.TimePolicy
import com.labijie.caching.CacheLocation
import java.lang.annotation.Inherited
import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-22
 */
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CacheRemove(
    val key:String,
    val region:String = "",
    val delayMills:Long = 2000) {
}