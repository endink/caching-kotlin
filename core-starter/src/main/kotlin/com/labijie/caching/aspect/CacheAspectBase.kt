package com.labijie.caching.aspect

import com.labijie.caching.ICacheScopeHolder
import com.labijie.caching.expression.SpELEvaluator
import com.labijie.caching.expression.SpELContext
import java.lang.reflect.Method

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
abstract class CacheAspectBase(protected val cacheScopeHolder: ICacheScopeHolder) {

    protected fun parseKeyAndRegion(
        keyExpression: String,
        regionExpression: String,
        method: Method,
        methodArgs:Array<Any> = arrayOf(),
        returnValue:Any? = null
    ): Pair<String, String> {
        val context = SpELContext(method, methodArgs, returnValue)
        val evaluator = SpELEvaluator(context)
        val key = evaluator.evaluate(keyExpression)
        val region = evaluator.evaluate(regionExpression)
        return Pair(key, region)
    }
}