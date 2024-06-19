/**
 * @author Anders Xiao
 * @date 2024-06-19
 */
package com.labijie.caching.kryo

import com.esotericsoftware.kryo.Kryo
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


object KryoUtils {
    fun Kryo.registerBaseTypes() {
        this.register(BigDecimal::class.java, 9)
        this.register(BigInteger::class.java, 10)
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
        this.register(ConcurrentHashMap::class.java, 22)
        this.register(Vector::class.java, 23)
        this.register(StringBuffer::class.java, 24)

        this.register(ByteArray::class.java, 25)
        this.register(CharArray::class.java, 26)
        this.register(IntArray::class.java, 27)
        this.register(FloatArray::class.java, 28)
        this.register(DoubleArray::class.java, 29)
        this.register(BooleanArray::class.java, 29)
        this.register(ShortArray::class.java, 31)
        this.register(LongArray::class.java, 32)
        this.register(Array<String>::class.java,33)
        this.register(Array<Any>::class.java, 34)

        this.register(LinkedHashMap::class.java, 40)
        this.register(LinkedHashSet::class.java, 41)
        this.register(Collections.EMPTY_LIST.javaClass, 42)
        this.register(Collections.EMPTY_MAP.javaClass,43)
        this.register(Collections.EMPTY_SET.javaClass,44)
        this.register(listOf(null as Any?).javaClass.javaClass,45)
        this.register(setOf(null as Any?).javaClass, 47)
        this.register(setOf(null as Any?).javaClass, 48)
        this.register(MutableCollection::class.java, 50)
        this.register(ConcurrentSkipListMap::class.java, 51)
        this.register(TreeMap::class.java, 52)
        this.register(MutableMap::class.java, 53)
        this.register(mutableListOf<Any>().javaClass, 54)

        this.register(Collections.singletonMap(null as Any?, null as Any?).javaClass,56)
        this.register(Collections.singletonList(null as Any?).javaClass,57)
        this.register(Collections.singleton(null as Any?).javaClass,58)
        this.register(Collections.unmodifiableSortedMap(sortedMapOf<String, Any?>()).javaClass,59)
        this.register(Collections.unmodifiableCollection(listOf(null as Any?)).javaClass,60)
        this.register(Collections.unmodifiableMap(mapOf<Any?, Any?>()).javaClass,61)
        this.register(Collections.unmodifiableSet(setOf(null as Any?)).javaClass,62)
        this.register(Collections.unmodifiableList(listOf(null as Any?)).javaClass,63)

        this.register(TimeZone::class.java, 70)
        this.register(Calendar::class.java, 71)
        this.register(Locale::class.java, 72)
        this.register(Charset::class.java, 73)
        this.register(URL::class.java, 74)
        this.register(Duration::class.java, DurationSerializer, 75)
        this.register(LocalDateTime::class.java, 76)
        this.register(Instant::class.java, 77)
    }
}