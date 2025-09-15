package io.allink.receipt.api.testsupport

import io.allink.receipt.api.repository.TransactionUtil
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider.SSL_MODE
import io.r2dbc.postgresql.client.SSLMode
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
    val pgConfig = PostgresqlConnectionConfiguration.builder()
      .host(host)
      .port(port)
      .username(username)
      .password(password)
      .database(database)
      .build()

    val connectionFactory = PostgresqlConnectionFactory(pgConfig)
    val db = R2dbcDatabase.connect(connectionFactory, databaseConfig = R2dbcDatabaseConfig {
      connectionFactoryOptions = builder()
        .option(DRIVER, "postgresql")
        .option(HOST, host)
        .option(PORT, port)
        .option(USER, username)
        .option(PASSWORD, password)
        .option(DATABASE, database)
//      .option(SSL, true)
        .option(PROTOCOL, "postgresql")
//        .option(SSL_MODE, SSLMode.REQUIRE)
//      .option(SSL_ROOT_CERT, "/etc/ssl/certs/ca-certificates.crt")
        .build()
    })
    TransactionUtil.init(db)
  }
}