val exposed_version: String by project
//val h2_version: String by project
val koin_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val hikaricp_version: String by project
val ktor_version: String by project

plugins {
  kotlin("jvm") version "2.1.20"
  id("io.ktor.plugin") version "3.1.2"
  id("org.jetbrains.kotlin.plugin.serialization") version "2.1.20"
}

group = "io.allink"
version = "0.0.1"

application {
  mainClass = "io.ktor.server.netty.EngineMain"

  val isDevelopment: Boolean = project.ext.has("development")
  applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
  mavenCentral()
  maven { url = uri("https://jitpack.io") }
}

dependencies {
  implementation("io.ktor:ktor-server-core:$ktor_version")
  implementation("io.ktor:ktor-server-auth:$ktor_version")
  implementation("io.ktor:ktor-server-auth-jwt:$ktor_version")
  implementation("io.ktor:ktor-server-csrf:$ktor_version")
  implementation("io.ktor:ktor-server-double-receive:$ktor_version")
  implementation("io.ktor:ktor-server-request-validation:$ktor_version")
  implementation("io.ktor:ktor-server-sse:$ktor_version")
  implementation("io.ktor:ktor-server-host-common:$ktor_version")
  implementation("io.ktor:ktor-server-status-pages:$ktor_version")
  implementation("io.ktor:ktor-server-cors:$ktor_version")
  implementation("io.ktor:ktor-server-compression:$ktor_version")
  implementation("io.ktor:ktor-server-swagger:$ktor_version")
  implementation("io.ktor:ktor-server-call-logging:$ktor_version")
  implementation("io.ktor:ktor-server-call-id:$ktor_version")
  implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
  implementation("io.ktor:ktor-serialization-kotlinx-json")
  implementation("dev.hayden:khealth:3.0.2")
  implementation("org.jetbrains.exposed:exposed-core:$exposed_version")
//  implementation("org.jetbrains.exposed:exposed-jdbc:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-r2dbc:$exposed_version")
//  implementation("com.zaxxer:HikariCP:$hikaricp_version")

//  implementation("com.h2database:h2:$h2_version")
  implementation("org.postgresql:postgresql:42.7.2") // 최신 버전 확인 필요
  implementation("org.postgresql:r2dbc-postgresql:$postgres_version")
  implementation("io.insert-koin:koin-ktor:$koin_version")
  implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
  implementation("io.ktor:ktor-server-netty")
  implementation("ch.qos.logback:logback-classic:$logback_version")
  implementation("ch.qos.logback:logback-classic:1.4.12")
  implementation("org.codehaus.janino:janino:3.1.12")

  implementation("software.amazon.awssdk:sns:2.20.+")
  implementation("software.amazon.awssdk:dynamodb:2.20.+")
  implementation("software.amazon.awssdk:s3:2.20.+")

  implementation("com.github.4sh:retable:-SNAPSHOT")

  implementation("io.github.smiley4:ktor-openapi:5.0.2")
  implementation("io.github.smiley4:ktor-redoc:5.0.2")
  implementation("io.swagger.core.v3:swagger-core:2.2.30")


  testImplementation("io.mockk:mockk:1.14.0")
  testImplementation("io.ktor:ktor-server-test-host")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlin_version")
  testImplementation("io.ktor:ktor-server-test-host-jvm")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0-RC.2")
  testImplementation("org.assertj:assertj-core:3.11.1")
  testImplementation(kotlin("test"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
}

tasks.test {
  useJUnitPlatform()
}

ktor {
  fatJar {
    archiveFileName.set("e-receipt-api-fat.jar")
  }
}