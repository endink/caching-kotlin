/**
 * @author Anders Xiao
 * @date 2025-06-16
 */
package com.labijie.caching.component


class NoopTransactionInjection: ITransactionInjection {
    override fun hookTransaction(action: () -> Unit) {
        throw UnsupportedOperationException()
    }

    override val isInTransaction: Boolean = false

}