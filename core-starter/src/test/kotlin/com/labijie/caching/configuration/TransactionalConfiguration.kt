package com.labijie.caching.configuration

import com.labijie.caching.bean.TransactionalBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder



/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
@Configuration
class TransactionalConfiguration {

    @Bean
    fun h2DataSource(): DataSource {
        return EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("CreateTestTable.sql")
            .build()
    }

    @Bean
    fun transactionalBean(): TransactionalBean {
        return TransactionalBean()
    }
}