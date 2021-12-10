package com.labijie.caching.redis.codec

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
class RedisValue private constructor(private val value: ByteArray, val type : RedisValueType) {
    constructor(value: String): this(value.toByteArray(Charsets.UTF_8), RedisValueType.String)
    constructor(value: ByteArray): this(value, RedisValueType.ByteArray)

    fun readString(): String {
        return value.toString(Charsets.UTF_8)
    }

    fun readBytes(): ByteArray {
        return value
    }
}

enum class RedisValueType {
    String,
    ByteArray,
    Unknown
}

