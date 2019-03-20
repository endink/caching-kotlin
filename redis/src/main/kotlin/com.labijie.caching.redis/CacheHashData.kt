package com.labijie.caching.redis

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-20
 */
internal class CacheHashData @JvmOverloads constructor(
    var type: String,
    var data: String,
    var serializer: String
)