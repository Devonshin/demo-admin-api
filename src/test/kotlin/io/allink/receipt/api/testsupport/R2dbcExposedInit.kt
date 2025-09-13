package io.allink.receipt.api.testsupport

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig

/**
 * @file R2dbcExposedInit.kt
 * @brief Testcontainers 기반 R2DBC Exposed(v1) 초기화 유틸
 * @author Devonshin
 * @date 2025-09-12
 */
object R2dbcExposedInit {
  fun init(host: String, port: Int, database: String, username: String, password: String) {
    val options = ConnectionFactoryOptions.builder()
      .option(DRIVER, "postgresql")
      .option(HOST, host)
      .option(PORT, port)
      .option(USER, username)
      .option(PASSWORD, password)
      .option(DATABASE, database)
      .option(PROTOCOL, "postgresql")
      .build()

    val connectionFactory = ConnectionFactories.get(options)
    R2dbcDatabase.connect(connectionFactory, databaseConfig = R2dbcDatabaseConfig {
      useNestedTransactions = true
      connectionFactoryOptions = options
    })
  }
}