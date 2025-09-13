package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.testsupport.PostgresContainerBase
import io.allink.receipt.api.testsupport.DbTestUtil
import io.allink.receipt.api.testsupport.R2dbcExposedInit
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * @file MerchantTagRepositoryIT.kt
 * @brief Testcontainers PostgreSQL + Exposed(v1 r2dbc) 기반 MerchantTagRepository 통합 테스트
 * @author Devonshin
 * @date 2025-09-12
 */
class MerchantTagRepositoryIT : PostgresContainerBase() {

  private lateinit var repository: MerchantTagRepository

  @BeforeEach
  fun setUp() {
    runJdbc { stmt ->
      DbTestUtil.dropCoreTables(stmt)
      DbTestUtil.createStoreTables(stmt)


      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "merchant_group" (
          merchant_group_id VARCHAR(36) PRIMARY KEY,
          token_key VARCHAR(255) NOT NULL,
          remote_ips TEXT NOT NULL,
          service_start_at TIMESTAMP NOT NULL,
          service_end_at TIMESTAMP NOT NULL,
          status VARCHAR(10) NOT NULL,
          receipt_width INT NOT NULL,
          authorities VARCHAR(255) NOT NULL,
          reg_date TIMESTAMP NOT NULL,
          mod_date TIMESTAMP NOT NULL,
          receipt_type VARCHAR(20) NOT NULL,
          merchant_group_name VARCHAR(50) NOT NULL
        )
        """.trimIndent()
      )

      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "merchant_tag" (
          tag_id VARCHAR(36) PRIMARY KEY,
          store_uid VARCHAR(36) NULL,
          merchant_group_id VARCHAR(36) NULL,
          merchant_store_id VARCHAR(36) NULL,
          tag_name VARCHAR(50) NULL,
          device_id VARCHAR(50) NULL,
          reg_date TIMESTAMP NOT NULL,
          mod_date TIMESTAMP NULL,
          reg_by UUID NOT NULL,
          mod_by UUID NULL
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

    repository = MerchantTagRepositoryImpl(MerchantTagTable)
  }

  @Test
  fun `find_should_join_store_and_group`() = runBlocking {
    // given
    runJdbc { stmt ->
      stmt.execute("""
        INSERT INTO "store" (store_uid, store_name, business_no, franchise_code, ceo_name, tel, business_type, event_type, status, reg_date)
        VALUES ('S-1', '매장1', '111', 'EDIYA', '대표', '02123', '커피', '카페', 'NORMAL', '2025-06-01T00:00:00')
      """.trimIndent())
      stmt.execute("""
        INSERT INTO "merchant_group" (merchant_group_id, token_key, remote_ips, service_start_at, service_end_at, status, receipt_width, authorities, reg_date, mod_date, receipt_type, merchant_group_name)
        VALUES ('EDIYA', 'key', '0.0.0.0/0', '2025-01-01T00:00:00', '2030-01-01T00:00:00', 'ACTIVE', 80, 'ALL', '2025-01-01T00:00:00', '2025-01-01T00:00:00', 'MERCHANT_RECEIPT', '이디야')
      """.trimIndent())
      stmt.execute("""
        INSERT INTO "merchant_tag" (tag_id, store_uid, merchant_group_id, tag_name, device_id, reg_date, reg_by)
        VALUES ('T-1', 'S-1', 'EDIYA', '태그1', 'D-1', '2025-06-02T00:00:00', '00000000-0000-0000-0000-000000000000')
      """.trimIndent())
    }

    // when
    val entity = TransactionUtil.withTransaction { repository.find("T-1") }

    // then
    assertThat(entity).isNotNull()
    assertThat(entity!!.id).isEqualTo("T-1")
    assertThat(entity.store?.storeName).isEqualTo("매장1")
    assertThat(entity.deviceId).isEqualTo("D-1")
  }
}