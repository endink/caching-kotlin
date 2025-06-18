package com.labijie.caching

import com.labijie.caching.bean.TransactionalBean
import com.labijie.caching.configuration.CachingAutoConfiguration
import com.labijie.caching.configuration.TestConfiguration
import com.labijie.caching.configuration.TransactionalConfiguration
import com.labijie.caching.orm.TestEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertNotNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.UnexpectedRollbackException
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
//@EnableAutoConfiguration
@JdbcTest
@ContextConfiguration(classes = [CachingAutoConfiguration::class, TestConfiguration::class, TransactionalConfiguration::class])
class TransactionalHookTester {

    @Autowired
    private lateinit var transactionalBean: TransactionalBean

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate


    @Autowired
    private lateinit var cacheManager: ICacheManager

    @BeforeTest
    fun init() {
        cacheManager.clear()
        val stream = this::class.java.getResourceAsStream("/CreateTestTable.sql")
        assertNotNull(stream, "Unable to found CreateTestTable.sql resource.")
        val sql = stream.readBytes().toString(Charsets.UTF_8)
        jdbcTemplate.execute(sql)
    }

    @Test
    fun cacheGet() {
        val data = TestEntity()
        transactionalBean.insert(data)

        Assertions.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "test"))

        val data2 = transactionalBean.getCached(data.id)
        val data3 = transactionalBean.getCached(data.id)

        Assertions.assertEquals(data, data2)
        Assertions.assertEquals(data, data3)

        val cached = cacheManager.get(data.id.toString(), TestEntity::class)
        Assertions.assertEquals(data, cached)
    }

    @Test
    fun cacheRemove() {
        val data = TestEntity()
        transactionalBean.insert(data)

        Assertions.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "test"))

        transactionalBean.getCached(data.id)
        val cached = cacheManager.get(data.id.toString(), TestEntity::class)
        Assertions.assertEquals(data, cached)

        val count = transactionalBean.removeCacheDelay2s(data.id)

        Assertions.assertEquals(1, count)
        var existed = cacheManager.get(data.id.toString(), TestEntity::class)

        Assertions.assertNotNull(existed)

        Thread.sleep(3000)
        existed = cacheManager.get(data.id.toString(), TestEntity::class)
        Assertions.assertNull(existed)

        val result = transactionalBean.getCached(data.id)
        Assertions.assertNull(result)
    }

    @Test
    fun noTransaction() {
        val data = TestEntity()
        transactionalBean.insert(data)

        Assertions.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "test"))

        transactionalBean.getCached(data.id)
        val cached = cacheManager.get(data.id.toString(), TestEntity::class)
        Assertions.assertEquals(data, cached)

        transactionalBean.noTransaction(data.id)
        Thread.sleep(3000)
        val existed = cacheManager.get(data.id.toString(), TestEntity::class)
        Assertions.assertNull(existed)
    }

    @Test
    fun transactionRollback() {
        val data = TestEntity()
        transactionalBean.insert(data)

        Assertions.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "test"))

        val query = transactionalBean.getCached(data.id)
        assertNotNull(query)

        val cached = cacheManager.get<TestEntity>(data.id.toString())
        Assertions.assertEquals(data, cached)

        try {
            //remove cache
            transactionalBean.transactionRollback(data.id)
        }catch (ex:UnexpectedRollbackException){

        }
        Thread.sleep(3000)
        val existed = cacheManager.get<TestEntity>(data.id.toString())
        Assertions.assertEquals(null, existed)
    }
}