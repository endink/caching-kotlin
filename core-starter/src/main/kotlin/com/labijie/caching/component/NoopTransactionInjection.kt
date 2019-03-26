package com.labijie.caching.component

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-26
 */
class NoopTransactionInjection() :ITransactionInjection {
    override fun hookTransaction(action: () -> Unit) {
        throw UnsupportedOperationException()
    }

    override val isInTransaction: Boolean = false

}