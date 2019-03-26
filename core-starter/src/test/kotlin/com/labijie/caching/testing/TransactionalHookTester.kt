package com.labijie.caching.testing

import com.labijie.caching.ICacheManager
import com.labijie.caching.configuration.CachingAutoConfiguration
import com.labijie.caching.testing.bean.SimpleScopedBean
import com.labijie.caching.testing.bean.SimpleTestingBean
import com.labijie.caching.testing.bean.TransactionalBean
import com.labijie.caching.testing.configuration.TransactionalConfiguration
import com.labijie.caching.testing.configuration.TestConfiguration
import com.labijie.caching.testing.orm.TestEntity
import org.junit.Assert
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.core.io.ResourceLoader
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.transaction.UnexpectedRollbackException
import java.lang.RuntimeException
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
    private lateinit var transactionalBean: TransactionalBean

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate


    @Autowired
    private lateinit var cacheManager: ICacheManager

    @BeforeTest
    fun init() {
        cacheManager.clear()
        val sql =
            this::class.java.classLoader.getResourceAsStream("CreateTestTable.sql").readBytes().toString(Charsets.UTF_8)
        jdbcTemplate.execute(sql)
    }

    @Test
    fun cacheGet() {
        val data = TestEntity()
        transactionalBean.insert(data)

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "test"))

        val data2 = transactionalBean.getCached(data.id)
        val data3 = transactionalBean.getCached(data.id)

        Assert.assertEquals(data, data2)
        Assert.assertEquals(data, data3)

        val cached = cacheManager.get(data.id.toString(), TestEntity::class)
        Assert.assertEquals(data, cached)
    }

    @Test
    fun cacheRemove() {
        val data = TestEntity()
        transactionalBean.insert(data)

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "test"))

        transactionalBean.getCached(data.id)
        val cached = cacheManager.get(data.id.toString(), TestEntity::class)
        Assert.assertEquals(data, cached)

        val count = transactionalBean.removeCacheDelay2s(data.id)

        Assert.assertEquals(1, count)
        var existed = cacheManager.get(data.id.toString(), TestEntity::class)

        Assert.assertNotNull(existed)

        Thread.sleep(3000)
        existed = cacheManager.get(data.id.toString(), TestEntity::class)
        Assert.assertNull(existed)

        val result = transactionalBean.getCached(data.id)
        Assert.assertNull(result)
    }

    @Test
    fun noTransaction() {
        val data = TestEntity()
        transactionalBean.insert(data)

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "test"))

        transactionalBean.getCached(data.id)
        val cached = cacheManager.get(data.id.toString(), TestEntity::class)
        Assert.assertEquals(data, cached)

        transactionalBean.noTransaction(data.id)
        Thread.sleep(3000)
        val existed = cacheManager.get(data.id.toString(), TestEntity::class)
        Assert.assertNull(existed)
    }

    @Test
    fun transactionRollback() {
        val data = TestEntity()
        transactionalBean.insert(data)

        Assert.assertEquals(1, JdbcTestUtils.countRowsInTable(jdbcTemplate, "test"))

        transactionalBean.getCached(data.id)
        val cached = cacheManager.get(data.id.toString(), TestEntity::class)
        Assert.assertEquals(data, cached)

        try {
            transactionalBean.transactionRollback(data.id)
        }catch (ex:UnexpectedRollbackException){

        }
        Thread.sleep(3000)
        val existed = cacheManager.get(data.id.toString(), TestEntity::class)
        Assert.assertEquals(cached, existed)
    }
}