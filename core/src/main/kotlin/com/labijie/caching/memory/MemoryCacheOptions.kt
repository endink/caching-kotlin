package com.labijie.caching.memory

import java.time.Duration

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
class MemoryCacheOptions {
    /**
     * 获取过期扫描的频率。
     * @return
     */
    var scanFrequency: Duration = Duration.ofSeconds(1)
    /**
     * 是否允许收缩内存。
     * @return
     */
    var isCompact: Boolean = false
}