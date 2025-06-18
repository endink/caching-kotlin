package com.labijie.caching

import com.labijie.caching.configuration.CachingAutoConfiguration
import com.labijie.caching.configuration.TestConfiguration
import com.labijie.caching.configuration.TransactionalConfiguration
import com.labijie.caching.expression.SpELContext
import com.labijie.caching.expression.SpELEvaluator
import com.labijie.caching.model.ArgumentObject
import com.labijie.caching.model.MethodObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.aot.DisabledInAotMode
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import kotlin.reflect.jvm.javaMethod
import kotlin.test.assertNotNull

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [TestConfiguration::class])
class SpELEvaluatorTester {

    @Test
    fun primitiveArgMethodTest(){

        val method = MethodObject::class.java.methods.find { it.name == MethodObject::primitiveArgMethod.name }

        assertNotNull(method)
        val intValue:Int = 232435432
        val stringValue:String = "OKOKOK"
        val decimalValue: BigDecimal = BigDecimal("9999999.1234567")

        val context = SpELContext(method, arrayOf(intValue, stringValue, decimalValue))
        val evaluator = SpELEvaluator(context)

        Assertions.assertEquals(intValue.toString(), evaluator.evaluate("#intValue"))
        Assertions.assertEquals(stringValue, evaluator.evaluate("#stringValue"))
        Assertions.assertEquals(decimalValue.toString(), evaluator.evaluate("#decimalValue"))
    }

    @Test
    fun objectArgMethodTest(){

        val method = MethodObject::class.java.methods.find { it.name == MethodObject::objectArgMethod.name }

        assertNotNull(method)

        val args = ArgumentObject()

        val context = SpELContext(method, arrayOf(args))
        val evaluator = SpELEvaluator(context)

        Assertions.assertEquals(args.longValue.toString(), evaluator.evaluate("#obj.longValue"))
        Assertions.assertEquals(args.stringValue, evaluator.evaluate("#obj.stringValue"))
    }

    @Test
    fun compositeArgMethodTest(){

        val method = MethodObject::class.java.methods.find { it.name == MethodObject::compositeArgMethod.name }

        assertNotNull(method)
        val args = ArgumentObject()
        val intValue = 100

        val context = SpELContext(method, arrayOf(intValue, args, null))
        val evaluator = SpELEvaluator(context)

        Assertions.assertEquals(intValue.toString(), evaluator.evaluate("#arg1"))
        Assertions.assertEquals((intValue * 10).toString(), evaluator.evaluate("#arg1 * 10"))
        Assertions.assertEquals(args.stringValue, evaluator.evaluate("#arg2.stringValue"))
        Assertions.assertEquals("", evaluator.evaluate("#arg3"))

        val value = evaluator.evaluate("'aaa' + (#arg1 * 10 - 1)")
        Assertions.assertEquals("aaa${(intValue * 10 - 1)}", value)
    }
}