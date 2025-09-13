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
  jacoco
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
  implementation("org.jetbrains.exposed:exposed-java-time:$exposed_version")
  implementation("org.jetbrains.exposed:exposed-r2dbc:$exposed_version")

  implementation("org.postgresql:postgresql:42.7.2") // 최신 버전 확인 필요
  implementation("org.postgresql:r2dbc-postgresql:$postgres_version")
  implementation("io.insert-koin:koin-ktor:$koin_version")
  implementation("io.insert-koin:koin-logger-slf4j:$koin_version")
  implementation("io.ktor:ktor-server-netty")
  implementation("ch.qos.logback:logback-classic:$logback_version")
  implementation("org.codehaus.janino:janino:3.1.12")

  implementation("software.amazon.awssdk:sns:2.20.+")
  implementation("software.amazon.awssdk:dynamodb:2.20.+")
  implementation("software.amazon.awssdk:s3:2.20.+")

  implementation("com.github.4sh:retable:-SNAPSHOT")

  implementation("io.github.cdimascio:dotenv-kotlin:6.5.1")

  implementation("io.github.smiley4:ktor-openapi:5.0.2")
  implementation("io.github.smiley4:ktor-redoc:5.0.2")
  implementation("io.swagger.core.v3:swagger-core:2.2.30")
  implementation("io.ktor:ktor-client-core:${ktor_version}")
  implementation("io.ktor:ktor-client-cio-jvm:${ktor_version}")
  implementation("io.ktor:ktor-client-content-negotiation:${ktor_version}")
  implementation("io.ktor:ktor-serialization-kotlinx-json:${ktor_version}")

  // Test deps
  testImplementation("io.mockk:mockk:1.14.0")
  testImplementation("io.ktor:ktor-server-test-host")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlin_version")
  testImplementation("io.ktor:ktor-server-test-host-jvm")
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0-RC.2")
  testImplementation("org.assertj:assertj-core:3.11.1")
  testImplementation(kotlin("test"))
  testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
  // Ktor client mock for HTTP client tests
  testImplementation("io.ktor:ktor-client-mock:${ktor_version}")
  // Testcontainers for repository integration tests
  testImplementation("org.testcontainers:junit-jupiter:1.20.2")
  testImplementation("org.testcontainers:postgresql:1.20.2")
}

tasks.test {
  useJUnitPlatform()
  // Ensure tests run sequentially to avoid Exposed R2DBC global connection stomping across containers
  maxParallelForks = 1
  // Fork a fresh JVM per test class to isolate Exposed R2DBC default DB
  forkEvery = 1
  // Disable JUnit 5 parallel execution just in case
  systemProperty("junit.jupiter.execution.parallel.enabled", "false")
  finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
  dependsOn(tasks.test) // 테스트가 먼저 실행되어야 함
  reports {
    xml.required.set(true)
    html.required.set(true)
    csv.required.set(false)
  }
}

jacoco {
  toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
  dependsOn(tasks.test)
  reports {
    html.required.set(true)
  }
}

// 테스트 커버리지 검증 태스크
tasks.jacocoTestCoverageVerification {
  violationRules {
    rule {
      limit {
        minimum = "0.50".toBigDecimal() // 최소 50% 커버리지 요구
      }
    }
    rule {
      element = "CLASS"
      limit {
        counter = "BRANCH"
        value = "COVEREDRATIO"
        minimum = "0.40".toBigDecimal() // 브랜치 커버리지 40% 이상
      }
    }
  }
}

ktor {
  fatJar {
    archiveFileName.set("e-receipt-api-fat.jar")
  }
}