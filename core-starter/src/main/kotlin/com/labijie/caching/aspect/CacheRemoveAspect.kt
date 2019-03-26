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
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.transaction.IllegalTransactionStateException
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
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
    private var transactionManager: PlatformTransactionManager? = null

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.transactionManager = try {
            applicationContext.getBean(PlatformTransactionManager::class.java)
        }catch (e: NoSuchBeanDefinitionException){
            null
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(CacheRemoveAspect::class.java)!!
    }

    @Pointcut("@annotation(com.labijie.caching.annotation.CacheRemove)")
    private fun cacheRemoveMethod() {
    }

    private val isInTransaction: Boolean
        get() {
            if(this.transactionManager != null) {
                val def = DefaultTransactionDefinition()
                def.propagationBehavior = TransactionDefinition.PROPAGATION_MANDATORY

                return try {
                    this.transactionManager!!.getTransaction(def)
                    return true
                } catch (ex: IllegalTransactionStateException) {
                    false
                }
            }else{
                return false
            }
        }

    @Around("cacheRemoveMethod()")
    fun aroundScope(joinPoint: ProceedingJoinPoint): Any? {
        val cacheUsed = cacheScopeHolder.cacheRequired(CacheOperation.Remove)

        val returnValue = joinPoint.proceed(joinPoint.args)
        val method = (joinPoint.signature as MethodSignature).method
        if(cacheUsed) {
            if (isInTransaction) {
                TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                    override fun afterCommit() {
                        removeCache(method, joinPoint.args)
                    }
                })
            } else {
                removeCache(method, joinPoint.args)
            }
        }else{
            if(logger.isDebugEnabled) {
                logger.debug("Cache scope prevent cache get operation. ( method:${method.declaringClass.simpleName}.${method.name}).")
            }
        }
        return returnValue
    }

    private fun removeCache(method: Method, args: Array<Any?>) {
        val cacheRemove = method.annotations.first {
            it.annotationClass == CacheRemove::class
        } as CacheRemove

        val (key, region) = this.parseKeyAndRegion(cacheRemove.key, cacheRemove.region, method, args)

        if(logger.isDebugEnabled){
            logger.debug("Cache will be removed after ${Math.floor(cacheRemove.delayMills / 1000.0)} seconds because of CacheRemove annotation" +
                    "( method:${method.declaringClass.simpleName}.${method.name}, cache key:$key, cache region:$region ).")
        }

        val delayMills = if(cacheRemove.delayMills < 500){
            if(logger.isDebugEnabled){
                logger.debug("${CacheRemove::class.java.simpleName} delay must be greater than 500ms, if not will be set to 500" +
                        "( method:${method.declaringClass.simpleName}.${method.name}, cache key:$key, cache region:$region ).")
            }
            500
        }else{
            cacheRemove.delayMills
        }
        this.timer.delay(delayMills) {
            try {
                this.cacheManager.remove(key, region)
                if(logger.isDebugEnabled){
                    logger.debug("Cache removed by CacheRemove annotation " +
                            "( method:${method.declaringClass.simpleName}.${method.name}, cache key:$key, cache region:$region ).")
                }
            } catch (ex: CacheException) {
                logger.error(
                    "Remove cache fault ( method: ${method.declaringClass.simpleName}.${method.name} ).",
                    ex
                )
            }
        }
    }
}