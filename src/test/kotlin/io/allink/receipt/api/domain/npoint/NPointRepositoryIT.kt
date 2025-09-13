package io.allink.receipt.api.domain.npoint

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
 * @file NPointRepositoryIT.kt
 * @brief Testcontainers PostgreSQL + Exposed(v1 r2dbc) 기반 NPointRepository 통합 테스트(필터/정렬/페이징/매핑)
 * @author Devonshin
 * @date 2025-09-12
 */
class NPointRepositoryIT : PostgresContainerBase() {

  private lateinit var repository: NPointRepository

  @BeforeEach
  fun setUp() {
    runJdbc { stmt ->
      DbTestUtil.dropCoreTables(stmt)
      DbTestUtil.createStoreTables(stmt)

      // Create base tables (user etc.)
      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "user" (
          uuid VARCHAR(36) PRIMARY KEY,
          name VARCHAR(255) NULL,
          phone VARCHAR(255) NULL,
          gender VARCHAR(5) NULL,
          birthday VARCHAR(20) NULL,
          nickname VARCHAR(255) NULL
        )
        """.trimIndent()
      )

      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "n_point_user_review" (
          receipt_uuid VARCHAR(36) PRIMARY KEY,
          user_uuid VARCHAR(36) NOT NULL,
          store_uid VARCHAR(36) NOT NULL,
          status VARCHAR(10) NOT NULL,
          review_url VARCHAR(255) NULL,
          reg_date TIMESTAMP NOT NULL,
          mod_date TIMESTAMP NULL,
          points INT NULL,
          expire_date TIMESTAMP NULL
        )
        """.trimIndent()
      )

      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "n_point_pay_waiting" (
          seq BIGINT PRIMARY KEY,
          receipt_uuid VARCHAR(36) NOT NULL,
          provide_case VARCHAR(10) NOT NULL,
          reg_date TIMESTAMP NOT NULL
        )
        """.trimIndent()
      )

      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "user_event_point" (
          waiting_seq BIGINT PRIMARY KEY,
          user_uuid VARCHAR(36) NOT NULL,
          event_session_id VARCHAR(36) NOT NULL,
          transaction_id VARCHAR(36) NOT NULL,
          receipt_uuid VARCHAR(36) NOT NULL,
          points INT NULL,
          advertisement_title VARCHAR(255) NOT NULL,
          reg_date TIMESTAMP NOT NULL
        )
        """.trimIndent()
      )

      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "n_point_tx_history" (
          waiting_seq BIGINT PRIMARY KEY,
          tx_no VARCHAR(36) NOT NULL,
          reg_date TIMESTAMP NOT NULL,
          result_code VARCHAR(10) NOT NULL
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

    repository = NPointRepositoryImpl(NPointWaitingTable)
  }

  @Test
  fun `findAll_should_map_and_filter`() = runBlocking {
    // given base data
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-1', '매장A')")
      // phone/nickname are encrypted in repo logic; store plain and use filter that matches encrypted value
      val encPhone = io.allink.receipt.api.util.AES256Util.encrypt("01011112222", io.allink.receipt.api.common.Constant.AES256_KEY)
      val encNick = io.allink.receipt.api.util.AES256Util.encrypt("길동", io.allink.receipt.api.common.Constant.AES256_KEY)
      stmt.execute("INSERT INTO \"user\" (uuid, name, phone, gender, birthday, nickname) VALUES ('U-1', '홍길동', '$encPhone', 'M', '1990-01-01', '$encNick')")

      stmt.execute("INSERT INTO \"n_point_user_review\" (receipt_uuid, user_uuid, store_uid, status, reg_date) VALUES ('R-1', 'U-1', 'S-1', 'WAIT', '2025-06-02T00:00:00')")

      // waiting
      stmt.execute("INSERT INTO \"n_point_pay_waiting\" (seq, receipt_uuid, provide_case, reg_date) VALUES (1, 'R-1', 'EVENT', '2025-06-02T00:00:00')")

      // event and tx history
      stmt.execute("INSERT INTO \"user_event_point\" (waiting_seq, user_uuid, event_session_id, transaction_id, receipt_uuid, points, advertisement_title, reg_date) VALUES (1, 'U-1', 'ES-1', 'TR-1', 'R-1', 100, '광고', '2025-06-02T00:01:00')")
      stmt.execute("INSERT INTO \"n_point_tx_history\" (waiting_seq, tx_no, reg_date, result_code) VALUES (1, 'TX-1', '2025-06-02T00:02:00', 'OK')")
    }

    val filter = NPointFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      phone = "01011112222",
      userName = "홍",
      sort = listOf(io.allink.receipt.api.domain.Sorter(field = "storeName", direction = "asc")),
      page = io.allink.receipt.api.domain.Page(page = 1, pageSize = 10)
    )

    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    assertThat(page.totalCount).isEqualTo(1)
    val item = page.items.first()
    assertThat(item.status).isEqualTo("지급완료")
    assertThat(item.point).isEqualTo(100)
    assertThat(item.user.name).isEqualTo("홍길동")
    assertThat(item.store.storeName).isEqualTo("매장A")
  }
}