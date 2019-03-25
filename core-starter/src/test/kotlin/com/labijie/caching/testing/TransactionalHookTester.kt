package com.labijie.caching.testing

import com.labijie.caching.ICacheManager
import com.labijie.caching.configuration.CachingAutoConfiguration
import com.labijie.caching.testing.bean.SimpleScopedBean
import com.labijie.caching.testing.bean.SimpleTestingBean
import com.labijie.caching.testing.configuration.TransactionalConfiguration
import com.labijie.caching.testing.configuration.TestConfiguration
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.jdbc.JdbcTestUtils
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
@RunWith(SpringRunner::class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJdbcTest
@ContextConfiguration(classes = [CachingAutoConfiguration::class, TestConfiguration::class, TransactionalConfiguration::class])
class TransactionalHookTester {

    @Autowired
    private lateinit var simple: SimpleTestingBean

    @Autowired
    private lateinit var scopedBean: SimpleScopedBean

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate


    @Autowired
    private lateinit var cacheManager: ICacheManager

    @BeforeTest
    fun init() {
        cacheManager.clear()
        jdbcTemplate.execute("create table test\n" +
                "(\n" +
                "id bigint primary key not null,\n" +
                "name varchar(20) null,\n" +
                "type int not null\n" +
                ")")
    }

    @AfterTest
    fun clear(){
        JdbcTestUtils.dropTables(jdbcTemplate, "test")
    }

    @Test
    fun empty(){

    }
}