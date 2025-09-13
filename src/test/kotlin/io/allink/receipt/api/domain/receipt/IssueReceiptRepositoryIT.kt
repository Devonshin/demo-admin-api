package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.testsupport.PostgresContainerBase
import io.allink.receipt.api.testsupport.DbTestUtil
import io.allink.receipt.api.testsupport.R2dbcExposedInit
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @file IssueReceiptRepositoryIT.kt
 * @brief Testcontainers PostgreSQL + Exposed(v1 r2dbc) 기반 IssueReceiptRepository 통합 테스트(단일 조회)
 * @author Devonshin
 * @date 2025-09-12
 */
class IssueReceiptRepositoryIT : PostgresContainerBase() {

  private lateinit var repository: IssueReceiptRepository

  @BeforeEach
  fun setUp() {
    runJdbc { stmt ->
      DbTestUtil.dropCoreTables(stmt)
      DbTestUtil.createStoreTables(stmt)

      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "user" (
          uuid VARCHAR(36) PRIMARY KEY,
          name VARCHAR(255) NULL,
          phone VARCHAR(50) NULL,
          nickname VARCHAR(255) NULL
        )
        """.trimIndent()
      )
      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "merchant_tag" (
          tag_id VARCHAR(36) PRIMARY KEY,
          device_id VARCHAR(50) NULL
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
        CREATE TABLE IF NOT EXISTS "advertisement" (
          uuid UUID PRIMARY KEY,
          merchant_group_id VARCHAR(36) NULL,
          title VARCHAR(255) NULL
        )
        """.trimIndent()
      )
      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "receipt_issue" (
          issued_receipt_uid VARCHAR(36) PRIMARY KEY,
          tag_id VARCHAR(36) NULL,
          store_uid VARCHAR(36) NULL,
          issue_date TIMESTAMP NOT NULL,
          user_uid VARCHAR(36) NULL,
          receipt_type VARCHAR(20) NOT NULL,
          receipt_amount INT NOT NULL,
          origin_issue_id VARCHAR(36) NULL,
          advertisement_uuid UUID NULL
        )
        """.trimIndent()
      )
      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "kakao_bill" (
          receipt_uuid VARCHAR(36) NOT NULL,
          envelop_id VARCHAR(100) NOT NULL,
          response_code VARCHAR(30) NOT NULL,
          reg_date TIMESTAMP NOT NULL,
          partner_req_uuid VARCHAR(36) PRIMARY KEY,
          user_id VARCHAR(36) NOT NULL
        )
        """.trimIndent()
      )
      stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS "naver_bill" (
          receipt_uuid VARCHAR(36) NOT NULL,
          naver_doc_id VARCHAR(100) NOT NULL,
          response_code VARCHAR(30) NOT NULL,
          reg_date TIMESTAMP NOT NULL,
          partner_req_uuid VARCHAR(36) PRIMARY KEY,
          user_id VARCHAR(36) NOT NULL
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

    repository = IssueReceiptRepositoryImpl(IssueReceiptTable)
  }

  @Test
  fun `findByIdAndUserId_should_return_joined_model`() = runBlocking {
    // given: 기본 데이터 삽입
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-1', '매장1')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-1', '홍길동')")
      stmt.execute("INSERT INTO \"merchant_tag\" (tag_id, device_id) VALUES ('T-1', 'D-1')")
      stmt.execute("INSERT INTO \"advertisement\" (uuid, merchant_group_id, title) VALUES ('00000000-0000-0000-0000-000000000001', 'EDIYA', '광고타이틀')")
      stmt.execute(
        """
        INSERT INTO "receipt_issue" (issued_receipt_uid, tag_id, store_uid, issue_date, user_uid, receipt_type, receipt_amount, origin_issue_id, advertisement_uuid)
        VALUES ('R-1', 'T-1', 'S-1', '2025-06-02T00:00:00', 'U-1', 'PAY', 1000, NULL, '00000000-0000-0000-0000-000000000001')
        """.trimIndent()
      )
    }

    // when
    val entity = TransactionUtil.withTransaction { repository.findByIdAndUserId("U-1", "R-1") }

    // then
    assertThat(entity).isNotNull()
    assertThat(entity!!.id).isEqualTo("R-1")
    assertThat(entity.store?.storeName).isEqualTo("매장1")
    assertThat(entity.tag?.deviceId).isEqualTo("D-1")
  }

  @Test
  fun `findByIdAndUserId_should_attach_kakao_edoc_when_exists`() = runBlocking {
    // given
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-K1', '매장K')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-K1', '홍길동')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-K1', 'S-K1', '2025-06-02T00:00:00', 'U-K1', 'PAY', 1000)")
      stmt.execute("INSERT INTO \"kakao_bill\" (receipt_uuid, envelop_id, response_code, reg_date, partner_req_uuid, user_id) VALUES ('R-K1', 'EVK-1', '200', '2025-06-02T00:01:00', 'PK-1', 'U-K1')")
    }

    val entity = TransactionUtil.withTransaction { repository.findByIdAndUserId("U-K1", "R-K1") }

    assertThat(entity).isNotNull()
    assertThat(entity!!.edoc).isNotNull()
    assertThat(entity.edoc!!.id).isEqualTo("kakao")
    assertThat(entity.edoc!!.envelopId).isEqualTo("EVK-1")
  }

  @Test
  fun `findByIdAndUserId_should_attach_naver_edoc_when_kakao_absent`() = runBlocking {
    // given
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-N1', '매장N')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-N1', '김철수')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-N1', 'S-N1', '2025-06-02T00:00:00', 'U-N1', 'PAY', 1000)")
      stmt.execute("INSERT INTO \"naver_bill\" (receipt_uuid, naver_doc_id, response_code, reg_date, partner_req_uuid, user_id) VALUES ('R-N1', 'EVN-1', 'OK', '2025-06-02T00:01:00', 'PN-1', 'U-N1')")
    }

    val entity = TransactionUtil.withTransaction { repository.findByIdAndUserId("U-N1", "R-N1") }

    assertThat(entity).isNotNull()
    assertThat(entity!!.edoc).isNotNull()
    assertThat(entity.edoc!!.id).isEqualTo("naver")
    assertThat(entity.edoc!!.envelopId).isEqualTo("EVN-1")
  }

  @Test
  fun `findByIdAndUserId_should_prefer_kakao_when_both_exist`() = runBlocking {
    // given: 동일 receipt/user에 kakao/naver 문서 둘 다 존재
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-BOTH', '매장BOTH')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-BOTH', '사용자BOTH')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-BOTH', 'S-BOTH', '2025-06-02T00:00:00', 'U-BOTH', 'PAY', 1000)")
      stmt.execute("INSERT INTO \"kakao_bill\" (receipt_uuid, envelop_id, response_code, reg_date, partner_req_uuid, user_id) VALUES ('R-BOTH', 'EVK-BOTH', '200', '2025-06-02T00:01:00', 'PK-BOTH', 'U-BOTH')")
      stmt.execute("INSERT INTO \"naver_bill\" (receipt_uuid, naver_doc_id, response_code, reg_date, partner_req_uuid, user_id) VALUES ('R-BOTH', 'EVN-BOTH', 'OK', '2025-06-02T00:02:00', 'PN-BOTH', 'U-BOTH')")
    }

    val entity = TransactionUtil.withTransaction { repository.findByIdAndUserId("U-BOTH", "R-BOTH") }

    assertThat(entity).isNotNull()
    assertThat(entity!!.edoc).isNotNull()
    // Kakao 우선
    assertThat(entity.edoc!!.id).isEqualTo("kakao")
    assertThat(entity.edoc!!.envelopId).isEqualTo("EVK-BOTH")
  }

  @Test
  fun `findByIdAndUserId_should_have_null_edoc_when_no_sources_exist`() = runBlocking {
    // given: kakao/naver 어떠한 문서도 없음
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-NONE', '매장NONE')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-NONE', '사용자NONE')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-NONE', 'S-NONE', '2025-06-02T00:00:00', 'U-NONE', 'PAY', 1000)")
    }

    val entity = TransactionUtil.withTransaction { repository.findByIdAndUserId("U-NONE", "R-NONE") }

    assertThat(entity).isNotNull()
    assertThat(entity!!.edoc).isNull()
  }
}
