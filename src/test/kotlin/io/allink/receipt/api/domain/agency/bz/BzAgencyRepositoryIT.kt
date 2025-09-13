package io.allink.receipt.api.domain.agency.bz

import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.testsupport.PostgresContainerBase
import io.allink.receipt.api.testsupport.R2dbcExposedInit
import io.allink.receipt.api.testsupport.DbTestUtil
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

/**
 * @file BzAgencyRepositoryIT.kt
 * @brief Testcontainers PostgreSQL + Exposed(v1 r2dbc) 기반 BzAgencyRepository 통합 테스트(필터/매핑/상세)
 */
class BzAgencyRepositoryIT : PostgresContainerBase() {

  private lateinit var repository: BzAgencyRepository

  @BeforeEach
  fun setUp() {
    runJdbc { stmt ->
      // 공용 드롭: 코어 테이블 일괄 삭제
      DbTestUtil.dropCoreTables(stmt)

      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "bz_agency" (
          uuid UUID PRIMARY KEY,
          agency_name VARCHAR(255) NULL,
          business_no VARCHAR(20) NULL,
          addr1 VARCHAR(255) NULL,
          addr2 VARCHAR(255) NULL,
          tel VARCHAR(20) NULL,
          ceo_name VARCHAR(50) NULL,
          ceo_phone VARCHAR(20) NULL,
          application_file_path VARCHAR(255) NULL,
          bz_file_path VARCHAR(255) NULL,
          id_file_path VARCHAR(255) NULL,
          bank_file_path VARCHAR(255) NULL,
          is_receipt_alliance BOOLEAN NULL,
          infra_ratio INT NULL,
          reward_base_ratio INT NULL,
          reward_commission_ratio INT NULL,
          reward_package_ratio INT NULL,
          advertisement_ratio INT NULL,
          is_coupon_adv BOOLEAN NULL,
          coupon_adv_ratio INT NULL,
          tag_deposit INT NULL,
          agency_deposit INT NULL,
          settlement_bank VARCHAR(50) NULL,
          bank_account_name VARCHAR(50) NULL,
          bank_account_no VARCHAR(50) NULL,
          status VARCHAR(20) NOT NULL,
          reg_date TIMESTAMP NOT NULL,
          reg_by UUID NOT NULL,
          mod_date TIMESTAMP NULL,
          mod_by UUID NULL
        )
        """.trimIndent()
      )

      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "admin" (
          uuid UUID PRIMARY KEY,
          login_id VARCHAR(100) NULL,
          password VARCHAR(255) NULL,
          full_name VARCHAR(50) NOT NULL,
          role VARCHAR(20) NOT NULL,
          phone VARCHAR(15) NOT NULL,
          email VARCHAR(100) NULL,
          status VARCHAR(20) NOT NULL,
          reg_date TIMESTAMP NOT NULL,
          mod_date TIMESTAMP NULL,
          reg_by UUID NULL,
          mod_by UUID NULL,
          agency_uuid UUID NULL
        )
        """.trimIndent()
      )

      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "login_info" (
          login_uuid UUID PRIMARY KEY,
          user_uuid UUID NOT NULL,
          verification_code TEXT NOT NULL,
          expire_date TIMESTAMP NOT NULL,
          status VARCHAR(20) NOT NULL,
          login_date TIMESTAMP NULL
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

    repository = BzAgencyRepositoryImpl(BzAgencyTable)
  }

  @Test
  fun `findAllByFilter_should_map_and_paginate`() = runBlocking {
    val agencyId = UUID.randomUUID()
    val staffId = UUID.randomUUID()
    // seed data
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"bz_agency\" (uuid, agency_name, business_no, status, reg_date, reg_by) VALUES ('$agencyId', '에이전시A', '111-22-33333', 'ACTIVE', '2025-06-02T00:00:00', '$staffId')")
      stmt.execute("INSERT INTO \"admin\" (uuid, full_name, role, phone, status, reg_date, agency_uuid) VALUES ('$staffId', '담당자', 'MASTER', '010', 'ACTIVE', '2025-06-02T00:00:00', '$agencyId')")
      stmt.execute("INSERT INTO \"login_info\" (login_uuid, user_uuid, verification_code, expire_date, status, login_date) VALUES ('${UUID.randomUUID()}', '$staffId', '000000', '2025-06-02T00:10:00', 'ACTIVE', '2025-06-02T00:15:00')")
    }

    val page = TransactionUtil.withTransaction {
      repository.findAllByFilter(
        BzAgencyFilter(
          agencyName = "에이",
          businessNo = null,
          status = AgencyStatus.ACTIVE,
          sort = listOf(io.allink.receipt.api.domain.Sorter("latestLoginAt", "desc")),
        ).copy(
          sort = listOf(io.allink.receipt.api.domain.Sorter("latestLoginAt", "desc"))
        )
      )
    }

    assertThat(page.totalCount).isEqualTo(1)
    val item = page.items.first()
    assertThat(item.id).isEqualTo(agencyId)
    assertThat(item.agencyName).isEqualTo("에이전시A")
    assertThat(item.latestLoginAt).isEqualTo(LocalDateTime.parse("2025-06-02T00:15:00"))
  }

  @Test
  fun `find_should_return_agency_with_staffs`() = runBlocking {
    val agencyId = UUID.randomUUID()
    val staffId = UUID.randomUUID()
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"bz_agency\" (uuid, agency_name, business_no, status, reg_date, reg_by) VALUES ('$agencyId', '에이전시B', '222-33-44444', 'ACTIVE', '2025-06-02T00:00:00', '$staffId')")
      stmt.execute("INSERT INTO \"admin\" (uuid, full_name, role, phone, status, reg_date, agency_uuid) VALUES ('$staffId', '담당자B', 'MASTER', '010', 'ACTIVE', '2025-06-02T00:00:00', '$agencyId')")
    }
    val model = TransactionUtil.withTransaction { repository.find(agencyId) }
    assertThat(model).isNotNull()
    assertThat(model!!.staffs?.size).isEqualTo(1)
    assertThat(model.staffs?.first()?.fullName).isEqualTo("담당자B")
  }
}