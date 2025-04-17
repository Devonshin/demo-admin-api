package io.allink.receipt.api.config.plugin

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database

fun Application.configureDatabases() {
  val postgresConfig = environment.config.config("postgres")
  val dbConnection = Database.connect(dataSource(postgresConfig))
  monitor.subscribe(ApplicationStopped) {
    runBlocking {
      try {
        dbConnection.connector().close()
        println("Database connection done.")
      } catch (e: Exception) {
        println("Error closing database connection: ${e.message}")
      }
    }
  }
}

fun dataSource(config: ApplicationConfig): HikariDataSource {
  val url = config.property("url").getString()
  val user = config.property("user").getString()
  val password = config.property("password").getString()
  val hConfig = HikariConfig()
  hConfig.jdbcUrl = url
  hConfig.username = user
  hConfig.password = password
  hConfig.setDriverClassName(config.property("driverClassName").getString())
  hConfig.maximumPoolSize = config.property("hikari.maximum-pool-size").getString().toInt()
  hConfig.isAutoCommit = config.property("hikari.auto-commit").getString().toBoolean()
  hConfig.transactionIsolation = IsolationLevel.TRANSACTION_REPEATABLE_READ.name
  hConfig.leakDetectionThreshold = 15000
  hConfig.validate()
  return HikariDataSource(hConfig)
}
