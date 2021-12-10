package com.labijie.caching.redis.codec

import io.lettuce.core.codec.RedisCodec
import java.nio.ByteBuffer

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
internal class KeyValueCodec : RedisCodec<String, RedisValue> {
    private val keyCodec: RedisCodec<String, *> = RedisValueCodec.stringCodec
    private val valueCodec = RedisValueCodec()


    override fun decodeKey(bytes: ByteBuffer?): String {
        return keyCodec.decodeKey(bytes)
    }

    override fun decodeValue(bytes: ByteBuffer?): RedisValue? {
        return valueCodec.decodeValue(bytes)
    }

    override fun encodeKey(key: String?): ByteBuffer {
        return keyCodec.encodeKey(key)
    }

    override fun encodeValue(value: RedisValue): ByteBuffer {
        return valueCodec.encodeValue(value)
    }
}