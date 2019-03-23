package com.labijie.caching.expression

import com.labijie.caching.CacheException
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.expression.EvaluationException
import org.springframework.expression.spel.SpelCompilerMode
import org.springframework.expression.spel.SpelParserConfiguration
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
class SpELEvaluator(private val spelContext: SpELContext) {
    companion object {
        const val ReturnValueName = "_RETURN"
        private val parameterNameDiscoverer = DefaultParameterNameDiscoverer()
        private val expressionParser = SpelExpressionParser(SpelParserConfiguration(SpelCompilerMode.MIXED, null))
    }

    private val parameterNames = parameterNameDiscoverer.getParameterNames(spelContext.method) ?: arrayOf()

    @Throws(CacheException::class)
    fun evaluate(expression: String): String {
        if(expression.isBlank()){
            return ""
        }
        val context = StandardEvaluationContext()
        parameterNames.forEachIndexed { index, name ->
            context.setVariable(name, spelContext.methodArgs[index])
        }
        context.setVariable(ReturnValueName, spelContext.returnValue)

        try {
            val exp = expressionParser.parseExpression(expression)
            return exp.getValue(context)?.toString() ?: ""
        }catch (e:EvaluationException){
           throw CacheException("Evaluate spring expression fault ( expression: $expression, method: ${spelContext.method.declaringClass.simpleName}.${spelContext.method.name} ).", e)
        }
    }
}
