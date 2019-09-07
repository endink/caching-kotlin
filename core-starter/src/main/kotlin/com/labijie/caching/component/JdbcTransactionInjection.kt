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
import java.util.*

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-26
 */
class JdbcTransactionInjection:ApplicationContextAware, ITransactionInjection {
    private var transactionManager: Optional<PlatformTransactionManager>? = null
    private lateinit var applicationContext: ApplicationContext

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    private fun ensureManager(){
        if(this.transactionManager == null){
            this.transactionManager = try {
                val m = applicationContext.getBean(PlatformTransactionManager::class.java)
                Optional.of(m)
            }catch (e:NoSuchBeanDefinitionException){
                Optional.empty()
            }
        }
    }

    override fun hookTransaction(action: () -> Unit) {
        TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
            override fun afterCommit() {
                action()
            }
        })
    }

    override val isInTransaction: Boolean
        get() {
            this.ensureManager()
            if(this.transactionManager!!.isPresent) {
                val def = DefaultTransactionDefinition()
                def.propagationBehavior = TransactionDefinition.PROPAGATION_MANDATORY

                return try {
                    this.transactionManager!!.get().getTransaction(def)
                    return true
                } catch (ex: IllegalTransactionStateException) {
                    false
                }
            }else{
                return false
            }
        }


}