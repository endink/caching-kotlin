package com.labijie.caching.testing.orm

import java.util.*
import kotlin.random.Random

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
data class TestEntity(
    var id:Long = System.currentTimeMillis(),
    var name:String = UUID.randomUUID().toString().substring(0, 20),
    var dataType:Int = Random.nextInt()) {
}