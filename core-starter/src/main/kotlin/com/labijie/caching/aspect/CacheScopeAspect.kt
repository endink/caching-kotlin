package com.labijie.caching.aspect

import com.labijie.caching.CacheScopeObject
import com.labijie.caching.ICacheScopeHolder
import com.labijie.caching.annotation.SuppressCache
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.core.Ordered

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-22
 */
@Aspect
class CacheScopeAspect(private val cacheScopeHolder: ICacheScopeHolder) : Ordered {
    override fun getOrder(): Int {
        return -1
    }


    @Pointcut("@annotation(com.labijie.caching.annotation.SuppressCache)")
    private fun cacheScopeMethod(){}

    @Around("cacheScopeMethod()")
    fun aroundScope(joinPoint: ProceedingJoinPoint): Any? {
        val method = (joinPoint.signature as MethodSignature).method

        val cacheScope = method.annotations.first {
            it.annotationClass == SuppressCache::class
        } as SuppressCache

        CacheScopeObject(cacheScopeHolder, *cacheScope.operations).use {
            return joinPoint.proceed(joinPoint.args)
        }
    }
}