package com.labijie.caching.testing.bean

import com.labijie.caching.annotation.Cache
import com.labijie.caching.annotation.CacheRemove
import com.labijie.caching.testing.orm.TestEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
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

    @Transactional
    @Cache("#id", 30000)
    fun getCached(id: Long): TestEntity? {
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
    }

    @Transactional
    fun insert(data: TestEntity): Int {
        return this.jdbcTemplate.update(
            "insert into test (id, name, data_type) values (?, ?, ?)",
            data.id, data.name, data.dataType
        )
    }

    @Transactional
    @CacheRemove("#key", delayMills = 2000)
    fun removeCacheDelay2s(id: Long): Int {
        return jdbcTemplate.update("delete from test where id = ?", id)
    }
}