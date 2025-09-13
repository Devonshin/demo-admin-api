package io.allink.receipt.api.domain.code

import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.testsupport.PostgresContainerBase
import io.allink.receipt.api.testsupport.DbTestUtil
import io.allink.receipt.api.testsupport.R2dbcExposedInit
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @file ServiceCodeRepositoryIT.kt
 * @brief Testcontainers PostgreSQL + Exposed(v1 r2dbc) 기반 ServiceCodeRepository 통합 테스트(그룹별 조회)
 */
class ServiceCodeRepositoryIT : PostgresContainerBase() {

  private lateinit var repository: ServiceCodeRepository

  @BeforeEach
  fun setUp() {
    runJdbc { stmt ->
      DbTestUtil.dropCoreTables(stmt)
      DbTestUtil.createStoreTables(stmt)
      // service_code는 공용 유틸에 없으므로 여기서만 생성
      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "service_code" (
          service_code VARCHAR(30) PRIMARY KEY,
          service_group VARCHAR(10) NOT NULL,
          service_name VARCHAR(255) NOT NULL,
          price INT NULL,
          service_type VARCHAR(10) NULL,
          status VARCHAR(10) NULL
        )
        """.trimIndent()
      )
    }

    R2dbcExposedInit.init(
      host = container.host,
      port = container.getMappedPort(5432),
      database = container.databaseName,
      username = container.username,
      password = container.password
    )

    repository = ServiceCodeRepositoryImpl(ServiceCodeTable)
  }

  @Test
  fun `findAll_should_return_items_by_group`() = runBlocking {
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"service_code\" (service_code, service_group, service_name, price, service_type, status) VALUES ('EDIYA', 'FRANCHISE', '이디야', 0, NULL, 'ACTIVE')")
      stmt.execute("INSERT INTO \"service_code\" (service_code, service_group, service_name, price, service_type, status) VALUES ('NONG', 'BANK_CODE', '농협', NULL, NULL, 'ACTIVE')")
      stmt.execute("INSERT INTO \"service_code\" (service_code, service_group, service_name, price, service_type, status) VALUES ('KOCES', 'VEN_CODE', '케이오시스', NULL, NULL, 'ACTIVE')")
    }

    val franchises = TransactionUtil.withTransaction { repository.findAll(ServiceCodeGroup.FRANCHISE.name) }
    val banks = TransactionUtil.withTransaction { repository.findAll(ServiceCodeGroup.BANK_CODE.name) }
    val vendors = TransactionUtil.withTransaction { repository.findAll(ServiceCodeGroup.VEN_CODE.name) }

    assertThat(franchises.map { it.id }).containsExactly("EDIYA")
    assertThat(banks.map { it.id }).containsExactly("NONG")
    assertThat(vendors.map { it.id }).containsExactly("KOCES")
  }
}