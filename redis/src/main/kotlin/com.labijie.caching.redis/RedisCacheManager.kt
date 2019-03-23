package com.labijie.caching.redis

import com.labijie.caching.CacheException
import com.labijie.caching.ICacheManager
import com.labijie.caching.TimePolicy
import com.labijie.caching.redis.configuration.RedisCacheConfig
import io.lettuce.core.KeyValue
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisException
import io.lettuce.core.ScriptOutputType
import io.lettuce.core.api.StatefulRedisConnection
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
open class RedisCacheManager(private val redisConfig: RedisCacheConfig) : ICacheManager {

    private val NEW_LINE = System.lineSeparator()

    // KEYS[1] = = key
    // ARGV[1] = absolute-expiration - ticks as long (-1 for none)
    // ARGV[2] = sliding-expiration - ticks as long (-1 for none)
    // ARGV[3] = relative-expiration (long, in seconds, -1 for none) - Min(absolute-expiration - Now, sliding-expiration)
    // ARGV[4] = data - byte[]
    // ARGV[5] = serializer - type string
    // this order should not change LUA script depends on it
    private val SET_SCRIPT = "local result = 1 " + NEW_LINE +
            "redis.call('HMSET', KEYS[1], 'absexp', ARGV[1], 'sldexp', ARGV[2], 'data', ARGV[4], 'type', ARGV[5], 'ser', ARGV[6]) " + NEW_LINE +
            "if ARGV[3] ~= '-1' then" + NEW_LINE +
            "result = redis.call('EXPIRE', KEYS[1], ARGV[3]) " + NEW_LINE +
            " end " + NEW_LINE +
            "return result"

    private val ABSOLUTE_EXPIRATIONKEY = "absexp"
    private val SLIDING_EXPIRATION_KEY = "sldexp"
    private val DATA_KEY = "data"
    private val TYPE_KEY = "type"
    private val SERIALIZER_KEY = "ser"
    private val NOT_PRESENT: Long = -1

    companion object {
        val logger = LoggerFactory.getLogger(RedisCacheManager::class.java)
        const val NULL_REGION_NAME = "--"
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
            redisConfig.regions.first()
        } else {
            redisConfig.regions.firstOrNull { r -> r.name == name }
                ?: throw RedisCacheException("Cant found redis cache region '$name' that be configured")
        }
        val r = if (name.isBlank()) NULL_REGION_NAME else name
        val client: RedisClientInternal? = null
        val c = this.clients.getOrPut(r) {
            val c = RedisClient.create(config.url)
            val connection = c.connect()

            val n = if (region.isNullOrBlank()) "" else region
            RedisClientInternal(n, connection, c, config.serializer.ifBlank { JacksonCacheDataSerializer.NAME })
        }
        if (client != null && c !== client) {
            client.client.shutdown()
        }
        return c
    }

    private fun List<KeyValue<String, String>>.toMap(): Map<String, String> {
        return this.filter { it.hasValue() }.map {
            it.key to it.value
        }.toMap()
    }

    private fun <T : Any> deserializeData(serializerName: String, type: KClass<T>, data: String): T? {
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
                ABSOLUTE_EXPIRATIONKEY,
                SLIDING_EXPIRATION_KEY,
                DATA_KEY,
                TYPE_KEY,
                SERIALIZER_KEY
            ).toMap()
        } else {
            command.hmget(key, ABSOLUTE_EXPIRATIONKEY, SLIDING_EXPIRATION_KEY).toMap()
        }


        if (hashResult.isEmpty()) {
            return null
        }

        // TODO: Error handling
        if (hashResult.size >= 2) {
            this.refreshExpire(
                connection, key,
                hashResult.getValue(ABSOLUTE_EXPIRATIONKEY).toLongOrNull(),
                hashResult.getValue(SLIDING_EXPIRATION_KEY).toLongOrNull()
            )
        }

        if (hashResult.size >= 5) {
            val type = hashResult.getOrDefault(TYPE_KEY, "")
            val data = hashResult.getOrDefault(DATA_KEY, "")
            val serializer = hashResult.getOrDefault(SERIALIZER_KEY, "")

            return CacheHashData(type, data, serializer)
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
            if (absExpr != null && absExpr != NOT_PRESENT) {
                val relExpr = absExpr - System.currentTimeMillis()
                expr = if (relExpr <= sldExpr) relExpr else sldExpr
            } else {
                expr = sldExpr
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
            data::class.java.name,
            client.serializer
        )

        val command = client.connection.sync()
        val script = command.scriptLoad(SET_SCRIPT)
        val result = command.evalsha<Long?>(script, ScriptOutputType.INTEGER, arrayOf(key), *values)
        if (result == null) {
            logger.error("Put data to redis cache fault, lua script return null (  key: $key, region: $region ).")
        }
    }

    private fun <T : Any> getCore(key: String, valueType: KClass<T>?, region: String?): T? {
        this.validateKey(key)
        try {
            val client = this.getClient(region)
            val cacheHashData = this.getAndRefresh(client.connection, key, true)
            if (cacheHashData != null) {
                //考虑程序变更后类型可能已经不存在或更名。
                var clazz: KClass<*>? = valueType
                if (clazz == null) {
                    try {
                        clazz = Class.forName(cacheHashData.type).kotlin
                    } catch (cne: ClassNotFoundException) {
                        this.remove(key, region)
                        logger.warn("The specified type '${cacheHashData.type}' could not be found to deserialize the cached data, and the cache with key '$key' has been removed ( region: $region ).")
                        return null
                    }
                }

                //考虑数据结构更新后缓存反序列化的问题。
                return try {
                    @Suppress("UNCHECKED_CAST")
                    this.deserializeData(cacheHashData.serializer, clazz, cacheHashData.data) as? T
                } catch (ex: Throwable) {
                    logger.warn("The specified type '${cacheHashData.type}' could not be deserialize from cached data, and the cache with key '$key' has been removed ( region: $region ).")

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

    override fun <T : Any> get(key: String, valueType: KClass<T>, region: String?): T? {
        return this.getCore(key, valueType, region)
    }

    override fun get(key: String, region: String?): Any? {
        return this.getCore(key, null, region)
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