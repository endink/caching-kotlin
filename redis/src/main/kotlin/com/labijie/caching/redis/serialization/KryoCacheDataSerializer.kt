package com.labijie.caching.redis.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.serializers.DefaultSerializers
import com.fasterxml.jackson.databind.type.TypeFactory
import com.labijie.caching.CacheException
import com.labijie.caching.redis.CacheDataDeserializationException
import com.labijie.caching.redis.ICacheDataSerializer
import com.labijie.caching.redis.serialization.kryo.DateSerializer
import com.labijie.caching.redis.serialization.kryo.URISerializer
import com.labijie.caching.redis.serialization.kryo.UUIDSerializer
import com.labijie.caching.redis.serialization.kryo.PooledKryo
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import java.math.BigDecimal
import java.math.BigInteger
import java.net.URI
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-09-08
 */
class KryoCacheDataSerializer(val kryoOptions: KryoOptions) : ICacheDataSerializer {

    companion object {
        const val NAME = "kryo"
        private val LOGGER = LoggerFactory.getLogger(JacksonCacheDataSerializer::class.java)
    }

    private val kryo: PooledKryo

    init {
        kryo = object : PooledKryo(kryoOptions.poolSize, kryoOptions.writeBufferSizeBytes) {
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
                    this.register(BigDecimal::class.java, DefaultSerializers.BigDecimalSerializer(), 9)
                    this.register(BigInteger::class.java, DefaultSerializers.BigIntegerSerializer(), 10)
                    this.register(BitSet::class.java, 11)
                    this.register(URI::class.java, URISerializer, 12)
                    this.register(UUID::class.java, UUIDSerializer, 13)
                    this.register(HashMap::class.java, 14)
                    this.register(ArrayList::class.java, 15)
                    this.register(LinkedList::class.java, 16)
                    this.register(HashSet::class.java, 17)
                    this.register(TreeSet::class.java, 18)
                    this.register(Hashtable::class.java, 19)
                    this.register(Date::class.java, DateSerializer, 20)
                    this.register(Calendar::class.java, 21)
                    this.register(ConcurrentHashMap::class.java, 22)
                    this.register(Vector::class.java, 23)
                    this.register(StringBuffer::class.java, 24)
                    this.register(ByteArray::class.java, 25)
                    this.register(CharArray::class.java, 26)
                    this.register(IntArray::class.java, 27)
                    this.register(FloatArray::class.java, 28)
                    this.register(DoubleArray::class.java, 29)
                    this.register(DoubleArray::class.java, 30)
                    this.register(ShortArray::class.java, 31)

                    this.register(LinkedHashMap::class.java, 40)
                    this.register(LinkedHashSet::class.java, 41)

                    kryoOptions.registeredClasses.forEach {
                        if (it.key <= 100) {
                            throw CacheException("Kryo register class id must be greater than 100 ( start with 101 )")
                        }
                        this.register(it.value.java, it.key)
                    }
                }
            }
        }
    }


    override fun deserializeData(type: Type, data: ByteArray): Any? {
        val clazz = TypeFactory.defaultInstance().constructType(type).rawClass
        val javaType = when (clazz) {
            List::class.java, MutableList::class.java -> ArrayList::class.java
            Map::class.java, MutableMap::class.java -> HashMap::class.java
            Set::class.java, MutableSet::class.java -> HashSet::class.java
            Collection::class.java, MutableCollection::class.java -> ArrayList::class.java
            Iterable::class.java, MutableIterable::class.java->ArrayList::class.java
            else -> clazz
        }
        if (javaType.isInterface) {
            throw CacheDataDeserializationException("Interface type was unsupported when use kryo serializer.")
        }
        return kryo.deserialize(data, javaType)
    }

    override fun serializeData(data: Any): ByteArray {
        return kryo.serialize(data)
    }

    override val name: String = NAME
}