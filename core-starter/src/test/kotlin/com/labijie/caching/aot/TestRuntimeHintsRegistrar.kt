package com.labijie.caching.aot

import com.labijie.caching.AnnotationClassBeanTester
import com.labijie.caching.AnnotationInterfaceBeanTester
import com.labijie.caching.SpELEvaluatorTester
import com.labijie.caching.TransactionalHookTester
import com.labijie.caching.model.ArgumentObject
import com.labijie.caching.model.MethodObject
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.aot.hint.TypeReference

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/17
 */
class TestRuntimeHintsRegistrar : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {

        println("\n\nProcess TestRuntimeHintsRegistrar\n\n")

        hints.reflection().registerTypes(
            listOf(
                TypeReference.of(AnnotationClassBeanTester::class.java),
                TypeReference.of(TransactionalHookTester::class.java),
                TypeReference.of(SpELEvaluatorTester::class.java),
                TypeReference.of(AnnotationClassBeanTester::class.java),
                TypeReference.of(AnnotationInterfaceBeanTester::class.java),
            )
        ) {
            it.withMembers(*MemberCategory.entries.toTypedArray())
        }

        hints.reflection().registerType(ArgumentObject::class.java) {
            it.withMembers(*MemberCategory.entries.toTypedArray())
        }

        hints.reflection().registerType(MethodObject::class.java) {
            it.withMembers(*MemberCategory.entries.toTypedArray())
        }

        hints.resources().registerPattern("*.sql")
    }
}