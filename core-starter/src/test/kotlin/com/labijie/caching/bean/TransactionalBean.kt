package com.labijie.caching.bean

import com.labijie.caching.annotation.Cache
import com.labijie.caching.annotation.CacheRemove
import com.labijie.caching.orm.TestEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.EmptyResultDataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.lang.RuntimeException
import java.sql.ResultSet

/**
 * Created with IntelliJ IDEA.
 * @author Anders Xiao
 * @date 2019-03-25
 */
@Component
class TransactionalBean {

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @Transactional
    @Cache("#id", 30000)
    fun getCached(id: Long): TestEntity? {
        try {
            return this.jdbcTemplate.queryForObject(
                "select * from test where id = ?",
                RowMapper<TestEntity> { rs, rowNum ->
                    TestEntity().apply {
                        this.id = rs.getLong("id")
                        this.name = rs.getString("name")
                        this.dataType = rs.getInt("data_type")
                    }
                }, id
            )
        }catch (e: EmptyResultDataAccessException){
            return null
        }
    }

    @Transactional
    fun insert(data: TestEntity): Int {
        return this.jdbcTemplate.update(
            "insert into test (id, name, data_type) values (?, ?, ?)",
            data.id, data.name, data.dataType
        )
    }

    @Transactional
    @CacheRemove("#id", delayMills = 2000)
    fun removeCacheDelay2s(id: Long): Int {
        return jdbcTemplate.update("delete from test where id = ?", id)
    }

    private fun innerRollback(){
        this.transactionTemplate.execute {
            it.setRollbackOnly()
        }
    }

    @CacheRemove(key = "#id", delayMills = 1000)
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    fun noTransaction(id:Long){
        this.innerRollback()
    }

    @CacheRemove(key = "#id", delayMills = 1000)
    @Transactional
    fun transactionRollback(id:Long){
        this.innerRollback()
    }
}