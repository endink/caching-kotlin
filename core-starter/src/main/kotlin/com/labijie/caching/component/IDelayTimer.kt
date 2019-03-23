package com.labijie.caching.component

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
interface IDelayTimer {
    fun delay(mills:Long, action: ()->Unit)
}