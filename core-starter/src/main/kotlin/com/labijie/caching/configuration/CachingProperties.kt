package com.labijie.caching.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 *
 * @Author: Anders Xiao
 * @Date: 2021/12/10
 * @Description:
 */
@ConfigurationProperties("infra.caching")
data class CachingProperties(var provider: String? = null)