package io.allink.receipt.api.testsupport

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.testcontainers.containers.PostgreSQLContainer
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Statement

/**
 * @file PostgresContainer.kt
 * @brief JUnit5 기반 PostgreSQL Testcontainers 베이스 클래스 및 JDBC 유틸
 * @author Devonshin
 * @date 2025-09-12
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class PostgresContainerBase {

  class KPostgres(image: String) : PostgreSQLContainer<KPostgres>(image)

  protected val container: KPostgres = KPostgres("postgres:16-alpine").apply {
    withDatabaseName("testdb")
    withUsername("test")
    withPassword("test")
    withReuse(true)
  }

  @BeforeAll
  fun startContainer() {
    container.start()
  }

  @AfterAll
  fun stopContainer() {
    // 재사용을 위해 stop 생략 가능하지만, 명시적으로 종료하지 않음
    // container.stop()
  }

  @AfterEach
  fun cleanup() {
    // 각 테스트 종료 시 사용자 테이블만 정리(존재할 경우)
    runJdbc { stmt ->
      stmt.execute("DROP TABLE IF EXISTS \"user\" CASCADE")
    }
  }

  protected fun runJdbc(block: (Statement) -> Unit) {
    DriverManager.getConnection(container.jdbcUrl, container.username, container.password).use { conn: Connection ->
      conn.createStatement().use { stmt ->
        block(stmt)
      }
    }
  }
}