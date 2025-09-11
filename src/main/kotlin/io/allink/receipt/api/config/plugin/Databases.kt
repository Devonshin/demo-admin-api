package io.allink.receipt.api.config.plugin

import io.ktor.server.application.*
import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider.SSL_MODE
import io.r2dbc.postgresql.PostgresqlConnectionFactoryProvider.SSL_ROOT_CERT
import io.r2dbc.postgresql.client.SSLMode
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
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
    if (env == "production") {
      builder()
        .option(DRIVER, "postgresql")
        .option(HOST, host)
        .option(PORT, port)
        .option(USER, user)
        .option(PASSWORD, password)
        .option(DATABASE, database)
        .option(SSL, true)
        .option(PROTOCOL, "postgresql")
        .option(SSL_MODE, SSLMode.VERIFY_FULL)           // 인증서 검증 활성화
        .option(
          SSL_ROOT_CERT,
          "cert/rds-ca-cert.pem"
        )
        .build()
    } else {
      builder()
        .option(DRIVER, "postgresql")
        .option(HOST, host)
        .option(PORT, port)
        .option(USER, user)
        .option(PASSWORD, password)
        .option(DATABASE, database)
        .option(PROTOCOL, "postgresql")
        .build()
    }

  val connectionFactory = ConnectionFactories.get(options)
  R2dbcDatabase.connect(connectionFactory, databaseConfig = R2dbcDatabaseConfig {
    useNestedTransactions = true
    connectionFactoryOptions = options
  })

}
