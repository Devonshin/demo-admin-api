package io.allink.receipt.api.config.plugin

import io.allink.receipt.api.repository.TransactionUtil
import io.ktor.server.application.*
import io.r2dbc.pool.ConnectionPool
import io.r2dbc.pool.ConnectionPoolConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider.SSL_MODE
import io.r2dbc.postgresql.client.SSLMode
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase.Companion.connect
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig


fun Application.configureDatabases() {

  val ktorConfig = environment.config.config("ktor")
  val postgresConfig = environment.config.config("postgres")
  val env = ktorConfig.property("environment").getString()
  println("environment = $env")
  val host = postgresConfig.property("url").getString()
  val port = postgresConfig.property("port").getString().toInt()
  val user = postgresConfig.property("user").getString()
  val password = postgresConfig.property("password").getString()
  val database = postgresConfig.property("database").getString()

  val options =
    builder()
      .option(DRIVER, "postgresql")
      .option(HOST, host)
      .option(PORT, port)
      .option(USER, user)
      .option(PASSWORD, password)
      .option(DATABASE, database)
//      .option(SSL, true)
      .option(PROTOCOL, "postgresql")
      .option(SSL_MODE, SSLMode.REQUIRE)
//      .option(SSL_ROOT_CERT, "/etc/ssl/certs/ca-certificates.crt")
      .build()

  // Instead of relying on ServiceLoader via ConnectionFactories.get(options),
  // construct the PostgresqlConnectionFactory directly to avoid missing provider issues
  val pgConfig = PostgresqlConnectionConfiguration.builder()
    .host(host)
    .port(port)
    .username(user)
    .password(password)
    .database(database)
    .sslMode(SSLMode.REQUIRE) // 필요 시 조정
    .build()
  val connectionFactory = PostgresqlConnectionFactory(pgConfig)

  val poolConfig = ConnectionPoolConfiguration.builder(connectionFactory)
    .initialSize(5)
    .maxSize(20)
    .maxIdleTime(java.time.Duration.ofMinutes(30))
    .maxLifeTime(java.time.Duration.ofHours(2))
    .maxAcquireTime(java.time.Duration.ofSeconds(60))
    .build()
  val pooledFactory = ConnectionPool(poolConfig)
  val db = connect(pooledFactory, databaseConfig = R2dbcDatabaseConfig {
    connectionFactoryOptions = options
  })
  TransactionUtil.init(db)
}