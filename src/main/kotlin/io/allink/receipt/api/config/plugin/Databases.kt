package io.allink.receipt.api.config.plugin

import io.ktor.server.application.*
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactoryOptions
import io.r2dbc.spi.ConnectionFactoryOptions.*
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabaseConfig


fun Application.configureDatabases() {
  val postgresConfig = environment.config.config("postgres")

  val host = postgresConfig.property("url").getString()
  val port = postgresConfig.property("port").getString().toInt()
  val user = postgresConfig.property("user").getString()
  val password = postgresConfig.property("password").getString()
  val database = postgresConfig.property("database").getString()

  val options = builder()
    .option(DRIVER, "postgresql")
    .option(HOST, host)
    .option(PORT, port)
    .option(USER, user)
    .option(PASSWORD, password)
    .option(DATABASE, database)
    .option(PROTOCOL, "postgresql")
    .build()

  val connectionFactory = ConnectionFactories.get(options)
  R2dbcDatabase.connect(connectionFactory, databaseConfig = R2dbcDatabaseConfig.invoke {
    useNestedTransactions = true
    connectionFactoryOptions = options
  })

}
