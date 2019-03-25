package com.labijie.caching

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
@FunctionalInterface
interface IPostEvictionCallback {
    fun callback(key: Any, value: Any, reason: EvictionReason, state: Any?)
}