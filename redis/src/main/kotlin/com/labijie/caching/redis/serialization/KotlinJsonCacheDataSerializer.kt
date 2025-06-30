package com.labijie.caching.redis.serialization

import com.labijie.caching.CacheSerializationUnsupportedException
import com.labijie.caching.redis.CacheDataSerializationException
import com.labijie.caching.redis.ICacheDataSerializer
import com.labijie.caching.redis.customization.IKotlinCacheDataSerializerCustomizer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2025/6/24
 */
class KotlinJsonCacheDataSerializer @JvmOverloads constructor(customizers: Iterable<IKotlinCacheDataSerializerCustomizer> = emptyList()) : ICacheDataSerializer {
    companion object {
        const val NAME = "kotlin-json"
    }

    private val wellKnownTypes: Map<KType, KSerializer<Any?>>

    init {

        val maps = mutableMapOf<KType, KSerializer<Any?>>()
        customizers.forEach {
            it.customSerializers().forEach {
                    kv->
                maps[kv.key] = kv.value
            }
        }
        wellKnownTypes = maps
    }

    override val name: String
        get() = NAME

    override fun deserializeData(type: Type, data: ByteArray): Any? {
        throw CacheSerializationUnsupportedException("KotlinJsonCacheDataSerializer only support set data with KType.")
    }


    private val json by lazy {
        Json {
            if(wellKnownTypes.isNotEmpty()) {
                this.serializersModule = SerializersModule {
                    wellKnownTypes.forEach { type ->
                        @Suppress("UNCHECKED_CAST")
                        (type.key.classifier as? KClass<*>)?.let {
                            contextual(it as KClass<Any>, type.value as KSerializer<Any>)
                        }
                    }
                }
            }
        }
    }

    override fun serializeData(data: Any, kotlinType: KType?): ByteArray {
        val type = kotlinType ?: throw CacheSerializationUnsupportedException("KotlinJsonCacheDataSerializer only support set data with KType.")
        try {
            val ser = wellKnownTypes[type] ?: serializer(type)
            return json.encodeToString(ser, data).toByteArray(Charsets.UTF_8)
        }catch (e: Throwable) {
            throw CacheDataSerializationException("Could not serialize data (kotlin json serializer)", e)
        }
    }

    override fun deserializeData(type: KType, data: ByteArray): Any? {
        try {
            val ser = wellKnownTypes[type] ?: serializer(type)
            return json.decodeFromString(ser, data.toString(Charsets.UTF_8))
        }catch (e: Throwable) {
            throw CacheDataSerializationException("Could not deserialize data (kryo serializer)", e)
        }

    }
}