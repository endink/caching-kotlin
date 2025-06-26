/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-26
 */

package com.labijie.caching.component

import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager


class JdbcTransactionInjection : ApplicationContextAware, ITransactionInjection {

    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    private val transactionManager by lazy {
        try {
            applicationContext.getBean(PlatformTransactionManager::class.java)
        } catch (_: NoSuchBeanDefinitionException) {
            null
        }
    }

    override fun hookTransaction(action: () -> Unit) {
        if (isInTransaction) {
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    action()
                }
            })
        } else {
            action()
        }
    }

    override val isInTransaction: Boolean
        get() {
            if (this.transactionManager != null) {
                val def = DefaultTransactionDefinition()
                def.propagationBehavior = TransactionDefinition.PROPAGATION_MANDATORY

                return try {
                    this.transactionManager?.getTransaction(def) != null
                } catch (_: IllegalTransactionStateException) {
                    false
                }
            } else {
                return false
            }
        }


}