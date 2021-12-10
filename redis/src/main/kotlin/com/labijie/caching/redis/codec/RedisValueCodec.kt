package com.labijie.caching.redis.codec

import io.lettuce.core.codec.ByteArrayCodec
import io.lettuce.core.codec.RedisCodec
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.codec.ToByteBufEncoder
import io.netty.buffer.ByteBuf
import java.nio.ByteBuffer

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
class RedisValueCodec : RedisCodec<RedisValue, RedisValue>, ToByteBufEncoder<RedisValue, RedisValue>  {
    companion object {
        val byteArrayCodec = ByteArrayCodec()
        val stringCodec = StringCodec(Charsets.UTF_8)
    }


    override fun decodeKey(bytes: ByteBuffer?): RedisValue {
        val array = byteArrayCodec.decodeKey(bytes)
        return RedisValue(array)
    }

    override fun decodeValue(bytes: ByteBuffer?): RedisValue {
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

    override fun encodeKey(key: RedisValue?, target: ByteBuf?) {
        if(key != null){
            when(key.type){
                RedisValueType.String->stringCodec.encodeKey(key.readString(), target)
                RedisValueType.ByteArray->byteArrayCodec.encodeKey(key.readBytes(), target)
                else->byteArrayCodec.encodeKey(key.readBytes(), target)
            }
        }else{
            byteArrayCodec.encodeKey(null, target)
        }
    }

    override fun encodeValue(value: RedisValue?, target: ByteBuf?) {
        if(value != null){
            when(value.type){
                RedisValueType.String->stringCodec.encodeValue(value.readString(), target)
                RedisValueType.ByteArray->byteArrayCodec.encodeValue(value.readBytes(), target)
                else->byteArrayCodec.encodeValue(value.readBytes(), target)
            }
        }else{
            byteArrayCodec.encodeValue(null, target)
        }
    }

    override fun estimateSize(keyOrValue: Any?): Int {
        val obj = keyOrValue as? RedisValue
        if(obj != null){
            return when(obj.type){
                RedisValueType.String->stringCodec.estimateSize(obj.readString())
                RedisValueType.ByteArray->byteArrayCodec.estimateSize(obj.readBytes())
                else->byteArrayCodec.estimateSize(obj.readBytes())
            }
        }
        return 0
    }


}