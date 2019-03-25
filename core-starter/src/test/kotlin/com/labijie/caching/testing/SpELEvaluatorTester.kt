package com.labijie.caching.testing

import com.labijie.caching.expression.SpELContext
import com.labijie.caching.expression.SpELEvaluator
import com.labijie.caching.testing.model.ArgumentObject
import com.labijie.caching.testing.model.MethodObject
import org.junit.Assert
import org.junit.Test
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

        Assert.assertEquals(intValue.toString(), evaluator.evaluate("#intValue"))
        Assert.assertEquals(stringValue, evaluator.evaluate("#stringValue"))
        Assert.assertEquals(decimalValue.toString(), evaluator.evaluate("#decimalValue"))
    }

    @Test
    fun objectArgMethodTest(){

        val method = MethodObject::objectArgMethod

        val args = ArgumentObject()

        val context = SpELContext(method.javaMethod!!, arrayOf(args))
        val evaluator = SpELEvaluator(context)

        Assert.assertEquals(args.longValue.toString(), evaluator.evaluate("#obj.longValue"))
        Assert.assertEquals(args.stringValue, evaluator.evaluate("#obj.stringValue"))
    }

    @Test
    fun compositeArgMethodTest(){

        val method = MethodObject::compositeArgMethod

        val args = ArgumentObject()
        val intValue = 100

        val context = SpELContext(method.javaMethod!!, arrayOf(intValue, args, null))
        val evaluator = SpELEvaluator(context)

        Assert.assertEquals(intValue.toString(), evaluator.evaluate("#arg1"))
        Assert.assertEquals((intValue * 10).toString(), evaluator.evaluate("#arg1 * 10"))
        Assert.assertEquals(args.stringValue, evaluator.evaluate("#arg2.stringValue"))
        Assert.assertEquals("", evaluator.evaluate("#arg3"))

        val value = evaluator.evaluate("'aaa' + (#arg1 * 10 - 1)")
        Assert.assertEquals("aaa${(intValue * 10 - 1)}", value)
    }
}