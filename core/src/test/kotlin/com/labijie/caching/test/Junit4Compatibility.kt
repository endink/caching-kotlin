package com.labijie.caching.test

import org.junit.jupiter.api.Assertions

object Assert {
    fun assertNull(message: String, actual: Any?){
        Assertions.assertNull(actual, message)
    }

    fun assertEquals(message:String, excepted: Any? , actual: Any?){
        Assertions.assertEquals(excepted, actual, message)
    }

    fun assertNotNull(message: String, actual: Any?){
        Assertions.assertNotNull(actual, message)
    }

    fun assertTrue(message:String, actual: Boolean){
        Assertions.assertTrue(actual, message)
    }
}