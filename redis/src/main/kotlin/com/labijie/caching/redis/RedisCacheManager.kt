package com.labijie.caching.redis

import com.labijie.caching.CacheException
import com.labijie.caching.ICacheManager
import com.labijie.caching.TimePolicy
import com.labijie.caching.redis.configuration.RedisCacheConfig
import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import io.lettuce.core.*
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.codec.StringCodec
import io.lettuce.core.masterreplica.MasterReplica
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap


/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
open class RedisCacheManager(private val redisConfig: RedisCacheConfig) : ICacheManager {


    // KEYS[1] = = key
    // ARGV[1] = absolute-expiration - ticks as long (-1 for none)
    // ARGV[2] = sliding-expiration - ticks as long (-1 for none)
    // ARGV[3] = relative-expiration (long, in seconds, -1 for none) - Min(absolute-expiration - Now, sliding-expiration)
    // ARGV[4] = data - byte[]
    // ARGV[5] = serializer - type string
    // this order should not change LUA script depends on it
    private val SET_SCRIPT = "local result = 1 " + NEW_LINE +
            "redis.call('HMSET', KEYS[1], 'absexp', ARGV[1], 'sldexp', ARGV[2], 'data', ARGV[4], 'ser', ARGV[5]) " + NEW_LINE +
            "if ARGV[3] ~= '-1' then" + NEW_LINE +
            "result = redis.call('EXPIRE', KEYS[1], ARGV[3]) " + NEW_LINE +
            " end " + NEW_LINE +
            "return result"


    companion object {
        private const val ABSOLUTE_EXPIRATION_KEY = "absexp"
        private const val SLIDING_EXPIRATION_KEY = "sldexp"
        private const val DATA_KEY = "data"
        private const val SERIALIZER_KEY = "ser"

        private val ABSOLUTE_EXPIRATION_ARRAY_KEY = ABSOLUTE_EXPIRATION_KEY.toByteArray(Charsets.UTF_8)
        private val SLIDING_EXPIRATION_ARRAY_KEY = SLIDING_EXPIRATION_KEY.toByteArray(Charsets.UTF_8)
        private val DATA_ARRAY_KEY = DATA_KEY.toByteArray((Charsets.UTF_8))
        private val SERIALIZER_ARRAY_KEY = SERIALIZER_KEY.toByteArray(Charsets.UTF_8)

        private const val NOT_PRESENT: Long = -1

        private val NEW_LINE = System.lineSeparator()
        private val logger = LoggerFactory.getLogger(RedisCacheManager::class.java)!!
        const val NULL_REGION_NAME = "--"

        private fun Long.toByteArray(): ByteArray {
            val b = ByteArray(8)
            b[7] = (this and 0xff).toByte()
            b[6] = (this shr 8 and 0xff).toByte()
            b[5] = (this shr 16 and 0xff).toByte()
            b[4] = (this shr 24 and 0xff).toByte()
            b[3] = (this shr 32 and 0xff).toByte()
            b[2] = (this shr 40 and 0xff).toByte()
            b[1] = (this shr 48 and 0xff).toByte()
            b[0] = (this shr 56 and 0xff).toByte()
            return b
        }

        private fun ByteArray.toLong(): Long {
            return (this[0].toLong() and 0xff shl 56
                    or (this[1].toLong() and 0xff shl 48)
                    or (this[2].toLong() and 0xff shl 40)
                    or (this[3].toLong() and 0xff shl 32)
                    or (this[4].toLong() and 0xff shl 24)
                    or (this[5].toLong() and 0xff shl 16)
                    or (this[6].toLong() and 0xff shl 8)
                    or (this[7].toLong() and 0xff shl 0))
        }
    }

    private val clients = ConcurrentHashMap<String, RedisClientInternal>()

    fun getClient(region: String? = null): RedisClientInternal {
        if (region == NULL_REGION_NAME) {
            throw RedisCacheException("Cache region name can not be '--'.")
        }
        if (redisConfig.regions.isEmpty()) {
            throw RedisCacheException("At least one redis cache region to be configured")
        }
        val name = if (region.isNullOrBlank()) redisConfig.defaultRegion.trim() else region.trim()
        val config = if (name.isBlank()) {
            redisConfig.regions.values.first()
        } else {
            redisConfig.regions.getOrDefault(name, null)
                ?: throw RedisCacheException("Cant found redis cache region '$name' that be configured")
        }
        val r = if (name.isBlank()) NULL_REGION_NAME else name
        val client: RedisClientInternal? = null
        val c = this.clients.getOrPut(r) {
            val (cli, conn) = createClientAndConnection(config.url)

            val n = if (region.isNullOrBlank()) "" else region
            RedisClientInternal(
                n,
                conn,
                cli,
                config.serializer.ifBlank { redisConfig.defaultSerializer }.ifBlank { JacksonCacheDataSerializer.NAME })
        }
        if (client != null && c !== client) {
            client.close()
        }
        return c
    }

    private fun createClientAndConnection(url: String): Pair<RedisClient, StatefulRedisConnection<String, String>> {
        if (url.isBlank()) {
            throw RedisException("Redis url cant not be null or empty string.")
        }
        val urls = url.split(",")
        if (urls.size <= 1) {
            val c = RedisClient.create(url)
            val connection = c.connect(StringCodec())
            return Pair(c, connection)
        } else {
            val redisUrls = urls.map {
                RedisURI.create(it.trim())
            }
            val client = RedisClient.create()
            val connection = MasterReplica.connect(
                client, StringCodec(),
                redisUrls
            )
            connection.readFrom = ReadFrom.REPLICA_PREFERRED
            return Pair(client, connection)
        }
    }

    private fun List<KeyValue<String, String>>.toMap(): Map<String, String> {
        return this.filter { it.hasValue() }.map {
            it.key to it.value
        }.toMap()
    }

    private fun deserializeData(serializerName: String, type: Type, data: String): Any? {
        val ser = CacheDataSerializerRegistry.getSerializer(serializerName)
        return ser.deserializeData(type, data)
    }


    private fun serializeData(serializerName: String, data: Any): String {
        val ser = CacheDataSerializerRegistry.getSerializer(serializerName)
        return ser.serializeData(data)
    }

    private fun validateKey(key: String) {
        if (key.isBlank()) {
            throw CacheException("Cache key cant not be null or empty string")
        }
    }

    private fun getAndRefresh(
        connection: StatefulRedisConnection<String, String>,
        key: String,
        getData: Boolean
    ): CacheHashData? {
        val command = connection.sync()
        val hashResult = if (getData) {
            command.hmget(
                key,
                ABSOLUTE_EXPIRATION_KEY,
                SLIDING_EXPIRATION_KEY,
                DATA_KEY,
                SERIALIZER_KEY
            )
        } else {
            command.hmget(
                key,
                ABSOLUTE_EXPIRATION_KEY,
                SLIDING_EXPIRATION_KEY
            )
        }

        val values = hashResult?.filter { it.hasValue() }
        if (values.isNullOrEmpty()) {
            return null
        }


        val valueMap = values.map { it.key to it.value }.toMap()


        // TODO: Error handling
        if (valueMap.size >= 2) {
            this.refreshExpire(
                connection, key,
                valueMap.getOrDefault(ABSOLUTE_EXPIRATION_KEY, null)?.toLong(),
                valueMap.getOrDefault(SLIDING_EXPIRATION_KEY, null)?.toLong()
            )
        }

        if (valueMap.size >= 4) {
            val data = valueMap.getOrDefault(DATA_KEY, null)
            val serializer = valueMap.getOrDefault(SERIALIZER_KEY, null).orEmpty()

            return CacheHashData(data, serializer)
        }
        return null
    }

    private fun refreshExpire(
        connection: StatefulRedisConnection<String, String>,
        key: String,
        absExpr: Long?,
        sldExpr: Long?
    ) {
        // Note Refresh has no effect if there is just an absolute expiration (or neither).
        val expr: Long?
        if (sldExpr != null && sldExpr != NOT_PRESENT) {
            expr = if (absExpr != null && absExpr != NOT_PRESENT) {
                val relExpr = absExpr - System.currentTimeMillis()
                if (relExpr <= sldExpr) relExpr else sldExpr
            } else {
                sldExpr
            }
            connection.sync().expire(key, expr / 1000)
        }
    }

    protected fun removeCore(connection: StatefulRedisConnection<String, String>, key: String, region: String?) {
        try {
            connection.sync().del(key)
        } catch (ex: RedisException) {
            throw ex.wrap("Delete key fault ( key: $key, region: $region ).")
        }

    }

    protected fun setCore(
        key: String,
        region: String?,
        data: Any?,
        timeoutMills: Long?,
        useSlidingExpiration: Boolean
    ) {
        val client = this.getClient(region)
        if (data == null) {
            this.removeCore(client.connection, key, region)
            return
        }
        val creationTime = System.currentTimeMillis()

        val values = arrayOf(
            (if (!useSlidingExpiration && timeoutMills != null) creationTime + timeoutMills else NOT_PRESENT).toString(),
            (if (useSlidingExpiration && timeoutMills != null) timeoutMills else NOT_PRESENT).toString(),
            (if (timeoutMills != null) timeoutMills / 1000 else NOT_PRESENT).toString(),
            this.serializeData(client.serializer, data),
            client.serializer
        )

        val command = client.connection.sync()

        val script = command.scriptLoad(SET_SCRIPT)
        val result = command.evalsha<Long?>(script, ScriptOutputType.INTEGER, arrayOf(key), *values)

        if (result != 1L) {
            logger.error("Put data to redis cache fault, script result: ${result?.toString() ?: "<null>"} (  key: $key, region: $region ).")
        }

        //事务不能支持多线程： https://github.com/lettuce-io/lettuce-core/issues/67
//        val timeoutSeconds = (if (timeoutMills != null) timeoutMills / 1000 else NOT_PRESENT)
//        if (timeoutSeconds != NOT_PRESENT) {
//            val result = command.multi()
//            if (result != "OK") {
//                logger.error("Put data to redis cache fault, redis multi return '$result' (  key: $key, region: $region ).")
//            }
//        }
 //       val keyArray = key.toByteArray(Charsets.UTF_8)
//        command.hmset(
//            keyArray, mapOf(
//                ABSOLUTE_EXPIRATION_ARRAY_KEY to (if (!useSlidingExpiration && timeoutMills != null) creationTime + timeoutMills else NOT_PRESENT).toByteArray(),
//                SLIDING_EXPIRATION_ARRAY_KEY to (if (useSlidingExpiration && timeoutMills != null) timeoutMills else NOT_PRESENT).toByteArray(),
//                DATA_ARRAY_KEY to this.serializeData(client.serializer, data),
//                SERIALIZER_ARRAY_KEY to client.serializer.toByteArray(Charsets.UTF_8)
//            )
//        )
//        if (timeoutSeconds != NOT_PRESENT) {
//            command.expire(keyArray, timeoutSeconds)
//        }
//
//        val result = if (timeoutSeconds != NOT_PRESENT) {
//            command.exec()
//        } else {
//            null
//        }


    }

    private fun getCore(key: String, region: String?, type: Type): Any? {
        this.validateKey(key)
        try {
            val client = this.getClient(region)
            val cacheHashData = this.getAndRefresh(client.connection, key, true)
            if (cacheHashData?.data != null) {

                //考虑数据结构更新后缓存反序列化的问题。
                return try {
                    this.deserializeData(cacheHashData.serializer, type, cacheHashData.data!!)
                } catch (ex: Throwable) {
                    logger.warn("The specified type '$type' could not be deserialize from cached data, and the cache with key '$key' has been removed ( region: $region ).")

                    if (ex is OutOfMemoryError || ex is StackOverflowError) {
                        throw ex
                    }
                    this.removeCore(client.connection, key, region)
                    null
                }

            }
        } catch (e: RedisException) {
            throw e.wrap("Failed to get data with key '$key' from cache region '$region'")
        }

        return null
    }

    override fun get(key: String, valueType: Type, region: String?): Any? {
        return this.getCore(key, region, valueType)
    }

    override fun set(
        key: String,
        data: Any,
        expireMills: Long?,
        timePolicy: TimePolicy,
        region: String?
    ) {
        validateKey(key)
        if (data::class.java == Any::class.java) {
            logger.warn("Cache put operation is ignored because the class '${data::class.java.simpleName}' cannot be deserialized.")
            return
        }
        try {
            this.setCore(key, region, data, expireMills, timePolicy == TimePolicy.Sliding)
        } catch (ex: RedisException) {
            throw  ex.wrap("Set cache data fault ( key: $key, region: $region ).")
        }
    }

    override fun remove(key: String, region: String?) {
        this.validateKey(key)
        try {
            val client = this.getClient(region)
            this.removeCore(client.connection, key, region)
        } catch (ex: RedisException) {
            throw ex.wrap("Remove cache data fault ( key: $key, region: $region ).")
        }
    }

    override fun refresh(key: String, region: String?): Boolean {
        this.validateKey(key)
        try {
            val client = this.getClient(region)
            this.getAndRefresh(client.connection, key, false)
            return true
        } catch (ex: RedisException) {
            throw ex.wrap("Refresh cache fault ( key: $key, region: $region).")
        }
    }

    override fun clearRegion(region: String) {
        try {
            val client = this.getClient(region)
            client.connection.sync().flushdb()
        } catch (ex: RedisException) {
            throw ex.wrap("Clear cache region '$region' fault .")
        }
    }

    override fun clear() {
        this.clients.keys.asSequence().forEach {
            this.clearRegion(if (it == NULL_REGION_NAME) "" else it)
        }
    }
}