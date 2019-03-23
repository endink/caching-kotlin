package com.labijie.caching.annotation

import com.labijie.caching.CacheOperation

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-22
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CacheScope(
    val prevent: Array<CacheOperation>
) {
}