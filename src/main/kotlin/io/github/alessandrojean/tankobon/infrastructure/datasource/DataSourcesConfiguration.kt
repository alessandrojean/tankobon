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
    val extraPragmas = tankobonProperties.database.pragmas.let {
      if (it.isEmpty()) ""
      else "?" + it.entries.joinToString("&") { (key, value) -> "$key=$value" }
    }

    val sqliteUdfDataSource = DataSourceBuilder.create()
      .driverClassName("org.sqlite.JDBC")
      .url("jdbc:sqlite:${tankobonProperties.database.file}")
      .type(SqliteUdfDataSource::class.java)
      .build()

    sqliteUdfDataSource.setEnforceForeignKeys(true)
    with(tankobonProperties.database) {
      journalMode?.let { sqliteUdfDataSource.setJournalMode(it.name) }
      busyTimeout?.let { sqliteUdfDataSource.config.busyTimeout = it.toMillis().toInt() }
    }

    val poolSize = when {
      tankobonProperties.database.file.contains(":memory:") -> 1
      tankobonProperties.database.poolSize != null -> tankobonProperties.database.poolSize!!
      else -> Runtime.getRuntime().availableProcessors()
        .coerceAtMost(tankobonProperties.database.maxPoolSize)
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