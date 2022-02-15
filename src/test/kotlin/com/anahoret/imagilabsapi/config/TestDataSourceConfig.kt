package com.anahoret.imagilabsapi.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

@Configuration
class TestDataSourceConfig {

    companion object {

        @JvmStatic
        private val kPostgreSQLContainer = PostgreSQLContainer("postgres:12.3-alpine")
            .withExposedPorts(5432)
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .apply { start() }

    }

    @Bean
    @ConditionalOnMissingBean(PostgreSQLContainer::class)
    fun postgreSQLContainer(): PostgreSQLContainer<*> {
        return kPostgreSQLContainer
    }

    @Bean
    @ConditionalOnMissingBean(DataSource::class)
    fun testDataSource(postgreSQLContainer: PostgreSQLContainer<*>): DataSource {
        val config = HikariConfig().apply {
            driverClassName = postgreSQLContainer.driverClassName
            jdbcUrl = postgreSQLContainer.jdbcUrl
            username = postgreSQLContainer.username
            password = postgreSQLContainer.password
        }
        return HikariDataSource(config)
    }

}
