package io.github.alessandrojean.tankobon.infrastructure.datasource

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.alessandrojean.tankobon.infrastructure.configuration.TankobonProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
class DataSourcesConfiguration(
  private val tankobonProperties: TankobonProperties,
) {

  @Bean("sqliteDataSource")
  @Primary
  fun sqliteDataSource(): DataSource {
    val sqliteUdfDataSource = DataSourceBuilder.create()
      .driverClassName("org.sqlite.JDBC")
      .url("jdbc:sqlite:${tankobonProperties.database.file}")
      .type(SqliteUdfDataSource::class.java)
      .build()

    sqliteUdfDataSource.setEnforceForeignKeys(true)

    val poolSize = when {
      tankobonProperties.database.file.contains(":memory:") -> 1
      else -> Runtime.getRuntime().availableProcessors()
    }

    return HikariDataSource(
      HikariConfig().apply {
        dataSource = sqliteUdfDataSource
        poolName = "SqliteUdfPool"
        maximumPoolSize = poolSize
      }
    )
  }
}