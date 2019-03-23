package com.labijie.caching.aspect

import com.labijie.caching.CacheException
import com.labijie.caching.ICacheManager
import com.labijie.caching.CacheOperation
import com.labijie.caching.ICacheScopeHolder
import com.labijie.caching.annotation.CacheRemove
import com.labijie.caching.component.IDelayTimer
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.lang.reflect.Method

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
@Aspect
class CacheRemoveAspect(
    private val cacheManager: ICacheManager,
    cacheScopeHolder: ICacheScopeHolder,
    private val timer:IDelayTimer
) :
    CacheAspectBase(cacheScopeHolder), ApplicationContextAware {

    private lateinit var applicationContext: ApplicationContext
    private var transactionManager: PlatformTransactionManager? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    companion object {
        val logger = LoggerFactory.getLogger(CacheRemoveAspect::class.java)!!
    }

    @Pointcut("@annotation(io.xstar.infra.caching.annotation.CacheRemove)")
    private fun cacheRemoveMethod() {
    }

    private val isInTransaction: Boolean
        get() {
            this.transactionManager = transactionManager ?:
                    this.applicationContext.getBean(PlatformTransactionManager::class.java)
            val def = DefaultTransactionDefinition()
            def.propagationBehavior = TransactionDefinition.PROPAGATION_MANDATORY

            return try {
                val status = this.transactionManager!!.getTransaction(def)
                !status.isCompleted && !status.isRollbackOnly
            } catch (ex: IllegalTransactionStateException) {
                false
            }
        }

    @Around("cacheRemoveMethod()")
    fun aroundScope(joinPoint: ProceedingJoinPoint): Any? {
        val cacheUsed = cacheScopeHolder.cacheRequired(CacheOperation.Remove)

        val returnValue = joinPoint.proceed(joinPoint.args)

        if(cacheUsed) {
            val method = (joinPoint.signature as MethodSignature).method
            if (isInTransaction) {
                TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                    override fun afterCommit() {
                        removeCache(method)
                    }
                })
            } else {
                removeCache(method)
            }
        }
        return returnValue
    }

    private fun removeCache(method: Method) {
        val cacheRemove = method.annotations.first {
            it.annotationClass == CacheRemove::class
        } as CacheRemove

        val (key, region) = this.parseKeyAndRegion(cacheRemove.key, cacheRemove.region, method)

        this.timer.delay(cacheRemove.delayMills) {
            try {
                this.cacheManager.remove(key, region)
            } catch (ex: CacheException) {
                logger.error(
                    "Remove cache fault ( method: ${method.declaringClass.simpleName}.${method.name} ).",
                    ex
                )
            }
        }
    }
}