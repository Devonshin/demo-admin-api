package io.allink.receipt.api.config.plugin

import io.ktor.server.config.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

/**
 * Package: io.allink.receipt.api.config.plugin
 * Created: Devonshin
 * Date: 23/05/2025
 */

private val logger: Logger = LoggerFactory.getLogger("io.allink.receipt.api.config.plugin.AwsConfig")
fun configureAwsDynamoDb(config: ApplicationConfig): DynamoDbClient {
  val awsConfig = config.config("aws")
  val env = config.config("ktor").property("environment").getString()
  logger.info("Using $env")
  if (env == "local" || env == "test") {
    return localDynamoDbClient()
  }

//  val region = awsConfig.property("region").getString()
  val accessKeyId = ${?AWS_ACCESS_KEY_ID}.property("accessKeyId").getString()
  val secretKey = ${?AWS_SECRET_KEY}.property("secretKey").getString()

  logger.info("Connecting to AWS DynamoDB with accessKeyId $accessKeyId")

  val credentials = StaticCredentialsProvider.create(
    AwsBasicCredentials.create(accessKeyId, secretKey)
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

fun s3Client(config: ApplicationConfig): S3Client {
  val accessKeyId = ${?AWS_ACCESS_KEY_ID}.property("aws.accessKeyId").getString()
  val secretKey = ${?AWS_SECRET_KEY}.property("aws.secretKey").getString()

  logger.info("Connecting to AWS S3 with accessKeyId $accessKeyId")
  return S3Client.builder()
    .region(Region.AP_NORTHEAST_2)
    .credentialsProvider(
      StaticCredentialsProvider.create(
        AwsBasicCredentials.create(accessKeyId, secretKey)
      )
    )
    .build()
}

fun s3Presigner(config: ApplicationConfig): S3Presigner {
  return S3Presigner.builder()
    .region(Region.AP_NORTHEAST_2)
    .credentialsProvider(
      StaticCredentialsProvider.create(
        AwsBasicCredentials.create(
          config.property("aws.accessKeyId").getString(),
          config.property("aws.secretKey").getString()
        )
      )
    )
    .build()
}