package com.labijie.caching.component

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-26
 */
interface ITransactionInjection {
    val isInTransaction:Boolean
    fun hookTransaction(action:()->Unit)
}