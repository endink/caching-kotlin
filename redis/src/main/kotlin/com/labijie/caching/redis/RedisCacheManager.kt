package com.labijie.caching.redis

import com.labijie.caching.CacheException
import com.labijie.caching.CacheSerializationUnsupportedException
import com.labijie.caching.ICacheItem
import com.labijie.caching.ICacheManager
import com.labijie.caching.TimePolicy
import com.labijie.caching.redis.codec.KeyValueCodec
import com.labijie.caching.redis.codec.RedisValue
import com.labijie.caching.redis.configuration.RedisCacheConfig
import com.labijie.caching.redis.serialization.JacksonCacheDataSerializer
import io.lettuce.core.*
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import io.lettuce.core.cluster.SlotHash
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection
import io.lettuce.core.masterreplica.MasterReplica
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KType


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

    val SET_MULTI_SCRIPT = """
        local result = 0
        local count = table.getn(KEYS)
        for i = 1, count do
          local baseIndex = (i - 1) * 5
          redis.call('HMSET', KEYS[i],
            'absexp', ARGV[baseIndex + 1],
            'sldexp', ARGV[baseIndex + 2],
            'data', ARGV[baseIndex + 4],
            'ser', ARGV[baseIndex + 5])
          if ARGV[baseIndex + 3] ~= "-1" then
            redis.call('EXPIRE', KEYS[i], ARGV[baseIndex + 3])
          end
          result = result + 1
        end
        return result
        """.trimIndent()

    val DELETE_KEYS_SCRIPT = """
            local deleted = 0
            for i, key in ipairs(KEYS) do
                if redis.call("EXISTS", key) == 1 then
                    deleted = deleted + redis.call("UNLINK", key)
                end
            end
            return deleted
        """.trimIndent()


    companion object {
        private const val ABSOLUTE_EXPIRATION_KEY = "absexp"
        private const val SLIDING_EXPIRATION_KEY = "sldexp"
        private const val DATA_KEY = "data"
        private const val SERIALIZER_KEY = "ser"


        private const val NOT_PRESENT: Long = -1

        private val NEW_LINE = System.lineSeparator()
        private val logger by lazy {
            LoggerFactory.getLogger(RedisCacheManager::class.java)
        }
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

        private fun String.toRedisValue(): RedisValue {
            return RedisValue(this)
        }

        private fun ByteArray.toRedisValue(): RedisValue {
            return RedisValue(this)
        }

        fun Any.toRedisBytes(): ByteArray = when (this) {
            is String -> this.toByteArray(Charsets.UTF_8)
            is ByteArray -> this
            else -> this.toString().toByteArray(Charsets.UTF_8) // ← ⚠️ 对 binary 是错的
        }
    }

    private val clients = ConcurrentHashMap<String, RedisClientInternal>()

    fun getClient(region: String?, serializerName: String?): RedisClientInternal {

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
        val r = name.ifBlank { NULL_REGION_NAME }
        val serializer = if(serializerName.isNullOrBlank()) config.serializer.ifBlank { redisConfig.defaultSerializer }.ifBlank { JacksonCacheDataSerializer.NAME } else serializerName

        var client: RedisClientInternal? = null
        val c = this.clients.getOrPut(r) {
            val (cli, conn) = createClientAndConnection(config.url)

            val n = if (region.isNullOrBlank()) "" else region
            val newClient = RedisClientInternal(
                n,
                conn,
                cli,
                serializer)
            client = newClient
            newClient
        }
        if (c !== client) {
            client?.close()
        }
        return c
    }

    private fun createClientAndConnection(url: String): Pair<RedisClient, StatefulRedisConnection<String, RedisValue>> {
        if (url.isBlank()) {
            throw RedisException("Redis url cant not be null or empty string.")
        }
        val codec = KeyValueCodec()
        val urls = url.split(",")
        if (urls.size <= 1) {
            val c = RedisClient.create(url)
            val connection = c.connect(codec)
            return Pair(c, connection)
        } else {
            val redisUrls = urls.map {
                RedisURI.create(it.trim())
            }
            val client = RedisClient.create()
            val connection = MasterReplica.connect(
                client, codec,
                redisUrls
            )
            connection.readFrom = ReadFrom.REPLICA_PREFERRED
            return Pair(client, connection)
        }
    }

    private fun List<KeyValue<String, String>>.toMap(): Map<String, String> {
        return this.filter { it.hasValue() }.associate {
            it.key to it.value
        }
    }

    private fun deserializeData(serializerName: String, type: Type,  data: ByteArray): Any? {
        val ser = CacheDataSerializerRegistry.getSerializer(serializerName)
        val data = ser.deserializeData(type, data)
        return data
    }

    private fun deserializeData(serializerName: String, type: KType,  data: ByteArray): Any? {
        val ser = CacheDataSerializerRegistry.getSerializer(serializerName)
        return ser.deserializeData(type, data)
    }


    private fun validateKey(key: String) {
        if (key.isBlank()) {
            throw CacheException("Cache key cant not be null or empty string")
        }
    }

    private fun getAndRefresh(
        connection: StatefulRedisConnection<String, RedisValue>,
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


        val valueMap = values.associate { it.key to it.value }


        // TODO: Error handling
        if (valueMap.size >= 2) {
            this.refreshExpire(
                connection, key,
                valueMap.getOrDefault(ABSOLUTE_EXPIRATION_KEY, null)?.readString()?.toLongOrNull(),
                valueMap.getOrDefault(SLIDING_EXPIRATION_KEY, null)?.readString()?.toLongOrNull()
            )
        }

        if (valueMap.size >= 4) {
            val data = valueMap.getOrDefault(DATA_KEY, null)?.readBytes()
            val serializer = valueMap.getOrDefault(SERIALIZER_KEY, null)?.readString()

            return CacheHashData(data, serializer.orEmpty())
        }
        return null
    }

    private fun refreshExpire(
        connection: StatefulRedisConnection<String, RedisValue>,
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

    protected fun removeCore(connection: StatefulRedisConnection<String, RedisValue>, key: String, region: String?): Long {
        if(key.isBlank()) {
            return 0
        }

        return try {
            val r = connection.sync().del(key)
            if(logger.isDebugEnabled) {
                logger.debug("Redis key '${key}' delete, return : $r")
            }
            r
        } catch (ex: RedisException) {
            throw ex.wrap("Delete redis key fault ( key: $key, region: $region ).")
        }
    }

    override fun setMulti(
        keyAndValues: Map<String, ICacheItem>,
        expireMills: Long?,
        timePolicy: TimePolicy,
        region: String?,
        serializer: String?
    ) {
        val useSliding = timePolicy == TimePolicy.Sliding
        setMultiCore(keyAndValues, region, expireMills, useSliding, serializer)
    }



    private fun setMultiCore(
        keyAndValues: Map<String, ICacheItem>,
        region: String?,
        timeoutMills: Long?,
        useSlidingExpiration: Boolean,
        serializer: String?
    ) {
        keyAndValues.keys.forEach { validateKey(it) }
        keyAndValues.values.forEach {
                data->
            if (data::class.java == Any::class.java) {
                throw CacheException("Cache data can not be ${Any::class.java.simpleName} as set.")
            }
        }

        val client = this.getClient(region,serializer)
        val creationTime = System.currentTimeMillis()

        val isCluster = client.connection is StatefulRedisClusterConnection<*, *>

        // 按槽位分组键值对
        val slotGroups = if (isCluster) {
            keyAndValues.entries.groupBy { (key, _) ->
                SlotHash.getSlot(key) // Lettuce 的槽位计算
            }
        } else {
            // 单机模式 - 所有键在同一个"槽位"
            mapOf(0 to keyAndValues.entries.toList())
        }

        slotGroups.forEach { (_, entries) ->
            val keys = entries.map { it.key }.toTypedArray()

            val values = entries.flatMap { (_, value) ->
                listOf(
                    (if (!useSlidingExpiration && timeoutMills != null) creationTime + timeoutMills else NOT_PRESENT).toString().toRedisValue(),
                    (if (useSlidingExpiration && timeoutMills != null) timeoutMills else NOT_PRESENT).toString().toRedisValue(),
                    (if (timeoutMills != null) timeoutMills / 1000 else NOT_PRESENT).toString().toRedisValue(),
                    client.serializer.serializeData(value.getData(), value.getKotlinType()).toRedisValue(),
                    client.serializer.name.toRedisValue()
                )
            }.toTypedArray()

            val command = client.connection.sync()
            val result = if(isCluster) {
                command.eval<Long?>(SET_MULTI_SCRIPT, ScriptOutputType.INTEGER, keys, *values) ?: 0
            }else {
                val script = command.scriptLoad(SET_MULTI_SCRIPT)
                command.evalsha<Long?>(script, ScriptOutputType.INTEGER, keys, *values) ?: 0
            }

            if (result < 1L) {
                logger.error("Put multi data to redis cache fault, script result: $result (  keys: ${keys.joinToString(", ")}, region: $region ).")
            }
        }
    }

    protected fun setCore(
        key: String,
        region: String?,
        data: Any?,
        kotlinType: KType?,
        timeoutMills: Long?,
        useSlidingExpiration: Boolean,
        serializer: String?
    ) {
        val client = this.getClient(region, serializer)
        if (data == null) {
            this.removeCore(client.connection, key, region)
            return
        }
        val creationTime = System.currentTimeMillis()

        val values = arrayOf(
            (if (!useSlidingExpiration && timeoutMills != null) creationTime + timeoutMills else NOT_PRESENT).toString().toRedisValue(),
            (if (useSlidingExpiration && timeoutMills != null) timeoutMills else NOT_PRESENT).toString().toRedisValue(),
            (if (timeoutMills != null) timeoutMills / 1000 else NOT_PRESENT).toString().toRedisValue(),
            client.serializer.serializeData(data, kotlinType).toRedisValue(),
            client.serializer.name.toRedisValue()
        )

        val isCluster = client.connection is StatefulRedisClusterConnection<*, *>
        val command = client.connection.sync()

        val result = if(isCluster) {
            command.eval<Long?>(SET_SCRIPT, ScriptOutputType.INTEGER, arrayOf(key), *values)
        }else {
            val script = command.scriptLoad(SET_SCRIPT)
            command.evalsha<Long?>(script, ScriptOutputType.INTEGER, arrayOf(key), *values)
        }

        if (result != 1L) {
            logger.error("Put data to redis cache fault, script result: ${result?.toString() ?: "<null>"} (  key: $key, region: $region ).")
        }
    }


    private fun getCore(key: String, region: String?, javaType: Type? = null, kotlinType: KType? = null): Any? {
        this.validateKey(key)
        try {
            val client = this.getClient(region, null)
            val cacheHashData = this.getAndRefresh(client.connection, key, getData = true)
            if (cacheHashData?.data != null) {

                //考虑数据结构更新后缓存反序列化的问题。
                return try {
                    if(kotlinType != null) {
                        this.deserializeData(cacheHashData.serializer, kotlinType, cacheHashData.data!!)
                    }
                    else if (javaType != null) {
                        this.deserializeData(cacheHashData.serializer, javaType, cacheHashData.data!!)
                    } else {
                        throw IllegalArgumentException("Java type and kotlin type should have at least one for cache data deserialization.")
                    }

                } catch (ex: Throwable) {
                    logger.error("The specified type '${kotlinType?.classifier?.javaClass ?: javaType}' could not be deserialize (${cacheHashData.serializer}) from cached data, and the cache with key '$key' has been removed ( region: $region ).")

                    if (ex is VirtualMachineError || ex is IllegalArgumentException || ex is CacheSerializationUnsupportedException) {
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
        return this.getCore(key, region, javaType = valueType)
    }


    override fun get(key: String, valueType: KType, region: String?): Any? {
        return this.getCore(key, region, kotlinType = valueType)
    }

    override fun set(
        key: String,
        data: Any,
        kotlinType: KType?,
        expireMills: Long?,
        timePolicy: TimePolicy,
        region: String?,
        serializer: String?
    ) {
        validateKey(key)
        if (data::class.java == Any::class.java) {
            throw CacheException("Cache data can not be ${Any::class.java.simpleName} as set.")
        }
        try {
            this.setCore(key, region, data, kotlinType, expireMills, timePolicy == TimePolicy.Sliding, serializer)
        } catch (ex: RedisException) {
            throw  ex.wrap("Set cache data fault ( key: $key, region: $region ).")
        }
    }


    private fun executeDeleteScript(isCluster: Boolean, connection: RedisCommands<String, RedisValue>, keys: Collection<String>): Long {
        return if(isCluster) {
            connection.eval<Long?>(
                DELETE_KEYS_SCRIPT,
                ScriptOutputType.INTEGER,
                keys.toTypedArray()
            ) ?: 0L
        } else {
            val script = connection.scriptLoad(DELETE_KEYS_SCRIPT)
            connection.evalsha<Long?>(
                script,
                ScriptOutputType.INTEGER,
                keys.toTypedArray()
            ) ?: 0L
        }
    }

    override fun removeMulti(keys: Iterable<String>, region: String?): Int {
        val validKeys = keys.filter { it.isNotBlank() }
        if (validKeys.isEmpty()) return 0

        val client = this.getClient(region, null)
        val connection = client.connection.sync()

        val isCluster = connection is StatefulRedisClusterConnection<*, *>
        // 按哈希槽分组键
        val slotGroups = validKeys.groupBy { key ->
            if (isCluster) {
                SlotHash.getSlot(key) // Lettuce 的槽位计算
            } else {
                0 // 单机模式使用固定槽位
            }
        }

        var totalDeleted = 0L
        slotGroups.values.forEach { groupKeys ->
            if (groupKeys.size == 1) {
                // 单键直接删除
                connection.del(groupKeys.first())
                totalDeleted++
            } else {
                // 每组键执行脚本
                totalDeleted += executeDeleteScript(isCluster,connection, groupKeys)
            }
        }

        if (logger.isDebugEnabled) {
            logger.debug("Deleted $totalDeleted/${validKeys.size} keys: ${validKeys.joinToString()}")
        }

        return totalDeleted.toInt()
    }

    override fun remove(key: String, region: String?): Boolean {
        this.validateKey(key)
        try {
            val client = this.getClient(region, null)
            val count = this.removeCore(client.connection, key, region)
            return count > 0
        } catch (ex: RedisException) {
            throw ex.wrap("Remove cache data fault ( key: $key, region: $region ).")
        }
    }

    override fun refresh(key: String, region: String?): Boolean {
        this.validateKey(key)
        try {
            val client = this.getClient(region, null)
            this.getAndRefresh(client.connection, key, false)
            return true
        } catch (ex: RedisException) {
            throw ex.wrap("Refresh cache fault ( key: $key, region: $region).")
        }
    }

    override fun clearRegion(region: String) {
        try {
            val client = this.getClient(region, null)
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