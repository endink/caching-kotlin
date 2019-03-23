package com.labijie.caching.testing.model

import java.util.*
import kotlin.random.Random

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-23
 */
data class ArgumentObject(
    var stringValue: String = UUID.randomUUID().toString(),
    var longValue: Long = Random.nextLong()
) {
}