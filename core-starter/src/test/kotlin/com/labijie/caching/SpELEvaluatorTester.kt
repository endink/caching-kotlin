package com.labijie.caching

import com.labijie.caching.expression.SpELContext
import com.labijie.caching.expression.SpELEvaluator
import com.labijie.caching.model.ArgumentObject
import com.labijie.caching.model.MethodObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.reflect.jvm.javaMethod

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
class SpELEvaluatorTester {

    @Test
    fun primitiveArgMethodTest(){

        val method = MethodObject::primitiveArgMethod

        val intValue:Int = 232435432
        val stringValue:String = "OKOKOK"
        val decimalValue: BigDecimal = BigDecimal("9999999.1234567")

        val context = SpELContext(method.javaMethod!!, arrayOf(intValue, stringValue, decimalValue))
        val evaluator = SpELEvaluator(context)

        Assertions.assertEquals(intValue.toString(), evaluator.evaluate("#intValue"))
        Assertions.assertEquals(stringValue, evaluator.evaluate("#stringValue"))
        Assertions.assertEquals(decimalValue.toString(), evaluator.evaluate("#decimalValue"))
    }

    @Test
    fun objectArgMethodTest(){

        val method = MethodObject::objectArgMethod

        val args = ArgumentObject()

        val context = SpELContext(method.javaMethod!!, arrayOf(args))
        val evaluator = SpELEvaluator(context)

        Assertions.assertEquals(args.longValue.toString(), evaluator.evaluate("#obj.longValue"))
        Assertions.assertEquals(args.stringValue, evaluator.evaluate("#obj.stringValue"))
    }

    @Test
    fun compositeArgMethodTest(){

        val method = MethodObject::compositeArgMethod

        val args = ArgumentObject()
        val intValue = 100

        val context = SpELContext(method.javaMethod!!, arrayOf(intValue, args, null))
        val evaluator = SpELEvaluator(context)

        Assertions.assertEquals(intValue.toString(), evaluator.evaluate("#arg1"))
        Assertions.assertEquals((intValue * 10).toString(), evaluator.evaluate("#arg1 * 10"))
        Assertions.assertEquals(args.stringValue, evaluator.evaluate("#arg2.stringValue"))
        Assertions.assertEquals("", evaluator.evaluate("#arg3"))

        val value = evaluator.evaluate("'aaa' + (#arg1 * 10 - 1)")
        Assertions.assertEquals("aaa${(intValue * 10 - 1)}", value)
    }
}