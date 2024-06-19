package com.labijie.caching.redis.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.DefaultSerializers.*
import com.fasterxml.jackson.databind.type.TypeFactory
import com.labijie.caching.CacheException
import com.labijie.caching.kryo.*
import com.labijie.caching.kryo.DateSerializer
import com.labijie.caching.kryo.KryoUtils.registerBaseTypes
import com.labijie.caching.kryo.URISerializer
import com.labijie.caching.kryo.UUIDSerializer
import com.labijie.caching.redis.ICacheDataSerializer
import com.labijie.caching.redis.serialization.kryo.*
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.net.URL
import java.nio.charset.Charset
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListMap

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-08
 */
open class KryoCacheDataSerializer(private val kryoSerializer: IKryoSerializer?, val kryoOptions: KryoOptions) : ICacheDataSerializer {

    companion object {
        const val NAME = "kryo"
    }


    private val kryo: IKryoSerializer by lazy {
        kryoSerializer ?: createDefaultSerializer(kryoOptions)
    }

    protected open fun createDefaultSerializer(options: KryoOptions): IKryoSerializer {
        return object : PooledKryo(kryoOptions.poolSize, kryoOptions.writeBufferSizeBytes) {
            override fun createKryo(): Kryo {
                return Kryo().apply {
                    this.isRegistrationRequired = kryoOptions.isRegistrationRequired
                    this.warnUnregisteredClasses = kryoOptions.warnUnregisteredClasses
                    /* kryo default
                    register(int.class, new IntSerializer());
                    register(String.class, new StringSerializer());
                    register(float.class, new FloatSerializer());
                    register(boolean.class, new BooleanSerializer());
                    register(byte.class, new ByteSerializer());
                    register(char.class, new CharSerializer());
                    register(short.class, new ShortSerializer());
                    register(long.class, new LongSerializer());
                    register(double.class, new DoubleSerializer());

                     */
                    registerBaseTypes()



                    kryoOptions.getRegistry().forEach {
                        if (it.id <= 100) {
                            throw CacheException("Kryo register class id must be greater than 100 ( start with 101 )")
                        }
                        if (it.serializer != null) {
                            this.register(it.clazz, it.serializer, it.id)
                        } else {
                            this.register(it.clazz, it.id)
                        }
                    }
                }
            }
        }
    }


    open override fun deserializeData(type: Type, data: ByteArray): Any {
        val clazz = TypeFactory.defaultInstance().constructType(type).rawClass
        val javaType = when (clazz) {
            List::class.java -> ArrayList::class.java
            Map::class.java -> HashMap::class.java
            Set::class.java -> HashSet::class.java
            Collection::class.java -> ArrayList::class.java
            Iterable::class.java -> ArrayList::class.java
            else -> clazz
        }
        return kryo.deserialize(data, javaType)
    }

    open override fun serializeData(data: Any): ByteArray {
        return kryo.serialize(data)
    }

    override val name: String = NAME
}