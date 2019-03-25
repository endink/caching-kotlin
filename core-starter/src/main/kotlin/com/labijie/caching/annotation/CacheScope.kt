package com.labijie.caching.annotation

import com.labijie.caching.CacheOperation
import java.lang.annotation.Inherited

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-22
 */
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CacheScope(
    val prevent: Array<CacheOperation>
) {
}