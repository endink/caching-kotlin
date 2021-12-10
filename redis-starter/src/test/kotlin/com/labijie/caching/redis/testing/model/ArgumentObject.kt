package com.labijie.caching.redis.testing.model

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
    var mapValues = mutableMapOf<String, Long>(
        "1231231" to 23123123,
        "333333" to 1111
    )
}