package com.labijie.caching

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
class StringUtils {
    fun tryParseLong(longValue: String): Optional<Long> {
        try {
            return Optional.ofNullable(java.lang.Long.parseLong(longValue))
        } catch (e: NumberFormatException) {
            return Optional.ofNullable(null)
        }

    }

    fun tryParseInt(longValue: String): Optional<Int> {
        try {
            return Optional.ofNullable(Integer.parseInt(longValue))
        } catch (e: NumberFormatException) {
            return Optional.ofNullable(null)
        }

    }

    fun tryParseBoolean(booleanValue: String?): Optional<Boolean> {
        return if (booleanValue != null && (booleanValue.equals(
                "true",
                ignoreCase = true
            ) || booleanValue.equals("false", ignoreCase = true))
        ) {
            Optional.ofNullable(java.lang.Boolean.parseBoolean(booleanValue))
        } else Optional.ofNullable(null)
    }

    @Throws(IOException::class)
    fun unzip(compressedStr: String?): String? {
        if (compressedStr == null) {
            return null
        }

        val compressed = Base64.getDecoder().decode(compressedStr)
        ByteArrayOutputStream().use { out ->
            ByteArrayInputStream(compressed).use { `in` ->
                GZIPInputStream(`in`).use { ginzip ->

                    val buffer = ByteArray(1024)
                    var offset = ginzip.read(buffer)
                    while (offset != -1) {
                        out.write(buffer, 0, offset)
                        offset = ginzip.read(buffer)
                    }
                    return out.toString("UTF-8")
                }
            }
        }
    }

    @Throws(IOException::class)
    fun gzip(content: String?): String? {
        if (content.isNullOrBlank()) {
            return content
        }

        ByteArrayOutputStream().use { out ->
            GZIPOutputStream(out).use { gzip -> gzip.write(content.toByteArray(charset("UTF-8"))) }
            return Base64.getEncoder().encodeToString(out.toByteArray())
        }
    }
}