package com.labijie.caching

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-02
 */
enum class EvictionReason {
    /**
     * 未过期
     */
    None,

    /**
     * 被移除（通过 remove 方法）
     */
    Removed,

    /**
     * 被替换成新项。
     */
    Replaced,

    /**
     * 正常过期（超过有效期）。
     */
    Expired,

    /**
     * 令牌过期
     */
    TokenExpired,

    /**
     * 内存回收
     */
    Capacity
}