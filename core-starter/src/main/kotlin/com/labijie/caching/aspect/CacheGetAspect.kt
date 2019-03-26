package com.labijie.caching.aspect

import com.labijie.caching.CacheException
import com.labijie.caching.ICacheManager
import com.labijie.caching.CacheOperation
import com.labijie.caching.ICacheScopeHolder
import com.labijie.caching.annotation.Cache
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
@Aspect
class CacheGetAspect(private val cacheManager: ICacheManager, cacheScopeHolder: ICacheScopeHolder) :
    CacheAspectBase(cacheScopeHolder) {

    companion object {
        val logger = LoggerFactory.getLogger(CacheGetAspect::class.java)!!
    }

    @Pointcut("@annotation(com.labijie.caching.annotation.Cache)")
    private fun cacheMethod() {
    }

    @Around("cacheMethod()")
    fun aroundScope(joinPoint: ProceedingJoinPoint): Any? {
        val method = (joinPoint.signature as MethodSignature).method
        val cacheUsed = (method.returnType != Void::class.java) && cacheScopeHolder.cacheRequired(CacheOperation.Get)

        var keyAndRegion: Pair<String, String>? = null
        val cacheAnnotation: Cache? = if (cacheUsed) getCacheSettings(method) else null
        if (cacheUsed && cacheAnnotation != null) {
            try {
                keyAndRegion = parseKeyAndRegion(cacheAnnotation.key, cacheAnnotation.region, method, joinPoint.args)
                val value = this.cacheManager.get(keyAndRegion.first, keyAndRegion.second)
                if (value != null) {
                    if (method.returnType.isAssignableFrom(value::class.java)) {
                        if(!logger.isDebugEnabled){
                            logger.debug("Get data from cache, skip the method block code ( method:${method.declaringClass.name}.${method.name}, cache key:${keyAndRegion.first}, cache region:${keyAndRegion.second} ).")
                        }
                        return value
                    } else {
                        val errorMessage =
                            "A value of a type incompatible return type was obtained from the cache. ${System.lineSeparator()}" +
                                    "cached type: ${value::class.java.simpleName}', method return type: ${method.returnType.simpleName}, " +
                                    "method: ${method.declaringClass.simpleName}.${method.name}."
                        throw CacheException(errorMessage)
                    }
                }
            } catch (ex: CacheException) {
                if (cacheAnnotation.ignoreCacheError) logger.error("Get cache fault.", ex) else throw ex
            }
        }

        val returnValue = joinPoint.proceed(joinPoint.args)

        if (cacheUsed && returnValue != null && keyAndRegion != null && cacheAnnotation != null) {
            try {
                this.cacheManager.set(
                    keyAndRegion.first,
                    data = returnValue,
                    expireMills = cacheAnnotation.expireMills,
                    timePolicy = cacheAnnotation.timePolicy,
                    region = keyAndRegion.second
                )
            } catch (ex: CacheException) {
                if (cacheAnnotation.ignoreCacheError) logger.error("Set cache fault ( method: ${method.declaringClass.simpleName}.${method.name} ).", ex) else throw ex
            }
        }

        if(!cacheUsed && logger.isDebugEnabled){
            logger.debug("Cache scope prevent cache get operation. ( method:${method.declaringClass.simpleName}.${method.name}).")
        }
        return returnValue
    }

    private fun getCacheSettings(method: Method): Cache? {
        return method.annotations.first {
            it.annotationClass == Cache::class
        } as Cache
    }
}