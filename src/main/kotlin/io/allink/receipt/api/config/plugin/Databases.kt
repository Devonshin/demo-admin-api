package io.allink.receipt.api.config.plugin

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import io.ktor.server.application.*
import io.ktor.server.config.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.endpoints.EndpointProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.endpoints.DynamoDbEndpointProvider
import software.amazon.awssdk.services.dynamodb.endpoints.internal.DefaultDynamoDbEndpointProvider
import java.net.URI

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

fun dynamoDbClient(env: String): DynamoDbClient {
  if (env == "local") {
    return localDynamoDbClient()
  }
  val credentials = StaticCredentialsProvider.create(
    AwsBasicCredentials.create("REDACTED_AWS_ACCESS_KEY_ID", "bHaIRiVWYrGieNHIVFrdctfE4s1+55xl/v8GMjbk")
  )
  return DynamoDbClient.builder()
    .region(Region.AP_NORTHEAST_2)
    .credentialsProvider(credentials)
    .build()
}


fun localDynamoDbClient(): DynamoDbClient {
  val credentials = StaticCredentialsProvider.create(
    AwsBasicCredentials.create("local", "local")
  )
  return DynamoDbClient.builder()
    .region(Region.AP_NORTHEAST_2)
    .endpointOverride(URI.create("http://localhost:8000"))
    .credentialsProvider(credentials)
    .build()
}