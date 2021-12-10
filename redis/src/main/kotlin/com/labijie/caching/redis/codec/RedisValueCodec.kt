package com.labijie.caching.redis.codec

import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import java.nio.ByteBuffer

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
class RedisValueCodec : RedisCodec<RedisValue, RedisValue> {
    companion object {
        val byteArrayCodec = ByteArrayCodec()
        val stringCodec = StringCodec(Charsets.UTF_8)
    }


    override fun decodeKey(bytes: ByteBuffer?): RedisValue {
        val array = byteArrayCodec.decodeKey(bytes)
        return RedisValue(array)
    }

    override fun decodeValue(bytes: ByteBuffer?): RedisValue? {
        return decodeKey(bytes)
    }

    override fun encodeKey(key: RedisValue?): ByteBuffer {
        return if(key != null){
            when(key.type){
                RedisValueType.String->stringCodec.encodeKey(key.readString())
                RedisValueType.ByteArray->byteArrayCodec.encodeKey(key.readBytes())
                else->byteArrayCodec.encodeKey(key.readBytes())
            }
        }else{
            byteArrayCodec.encodeKey(null)
        }
    }

    override fun encodeValue(value: RedisValue?): ByteBuffer {
        return if(value != null){
            when(value.type){
                RedisValueType.String->stringCodec.encodeValue(value.readString())
                RedisValueType.ByteArray->byteArrayCodec.encodeValue(value.readBytes())
                else->byteArrayCodec.encodeValue(value.readBytes())
            }
        }else{
            byteArrayCodec.encodeValue(null)
        }
    }
}