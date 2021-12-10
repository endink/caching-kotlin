package com.labijie.caching.redis.codec

import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.ToByteBufEncoder
import io.netty.buffer.ByteBuf
import java.nio.ByteBuffer

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
internal class KeyValueCodec : RedisCodec<String, RedisValue>, ToByteBufEncoder<String, RedisValue> {
    private val keyCodec = RedisValueCodec.stringCodec
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

    override fun encodeKey(key: String?, target: ByteBuf?) {
        keyCodec.encodeKey(key, target)
    }

    override fun encodeValue(value: RedisValue?, target: ByteBuf?) {
        valueCodec.encodeKey(value, target)
    }

    override fun estimateSize(keyOrValue: Any?): Int {
        if(keyOrValue is String){
            keyCodec.estimateSize(keyOrValue)
        }
        if(keyOrValue is RedisValue){
            valueCodec.estimateSize(keyOrValue)
        }
        return 0
    }
}