package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.common.Constant
import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.testsupport.PostgresContainerBase
import io.allink.receipt.api.testsupport.R2dbcExposedInit
import io.allink.receipt.api.testsupport.DbTestUtil
import io.allink.receipt.api.util.AES256Util
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

/**
 * @file IssueReceiptRepositoryFindAllIT.kt
 * @brief IssueReceiptRepository.findAll 통합 테스트(필터/정렬/페이징/암복호화)
 * @author Devonshin
 * @date 2025-09-12
 */
class IssueReceiptRepositoryFindAllIT : PostgresContainerBase() {

  private lateinit var repository: IssueReceiptRepository

  @BeforeEach
  fun setUp() {
    runJdbc { stmt ->
      // 코어 테이블 정리 및 기본 스키마 생성
      DbTestUtil.dropCoreTables(stmt)
      // receipt/user 등은 유틸에 없으므로 직접 관리
      stmt.execute("DROP TABLE IF EXISTS \"receipt_issue\" CASCADE")
      stmt.execute("DROP TABLE IF EXISTS \"user\" CASCADE")

      // store는 공용 유틸 사용
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
        CREATE TABLE IF NOT EXISTS "receipt_issue" (
          issued_receipt_uid VARCHAR(36) PRIMARY KEY,
          tag_id VARCHAR(36) NULL,
          store_uid VARCHAR(36) NULL,
          issue_date TIMESTAMP NOT NULL,
          user_uid VARCHAR(36) NULL,
          receipt_type VARCHAR(20) NOT NULL,
          receipt_amount INT NOT NULL,
          origin_issue_id VARCHAR(36) NULL
        )
        """.trimIndent()
      )

      // Ensure schema/path and table existence before R2DBC/Exposed init
      stmt.execute("SET search_path TO public")
      // Throws if the table is not visible; aids in early diagnosis
      stmt.execute("SELECT 1 FROM \"receipt_issue\" LIMIT 1")
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
  fun `Find all should filter by store name phone and sort`() = runBlocking {
    // given
    val encPhoneA = AES256Util.encrypt("01011112222", Constant.AES256_KEY)
    val encNickA = AES256Util.encrypt("길동", Constant.AES256_KEY)

    val encPhoneB = AES256Util.encrypt("01099998888", Constant.AES256_KEY)
    val encNickB = AES256Util.encrypt("철수", Constant.AES256_KEY)

    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, business_no, franchise_code, ceo_name) VALUES ('S-1', '매장A', '111', 'F-1', '대표A')")
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, business_no, franchise_code, ceo_name) VALUES ('S-2', '마트B', '222', 'F-2', '대표B')")

      stmt.execute("INSERT INTO \"user\" (uuid, name, phone, nickname) VALUES ('U-1', '홍길동', '$encPhoneA', '$encNickA')")
      stmt.execute("INSERT INTO \"user\" (uuid, name, phone, nickname) VALUES ('U-2', '김철수', '$encPhoneB', '$encNickB')")

      stmt.execute(
        """
        INSERT INTO "receipt_issue" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount)
        VALUES ('R-1', 'S-1', '2025-06-02 00:00:00', 'U-1', 'PAY', 1000)
        """.trimIndent()
      )
      stmt.execute(
        """
        INSERT INTO "receipt_issue" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount)
        VALUES ('R-2', 'S-2', '2025-06-01 00:00:00', 'U-2', 'PAY', 2000)
        """.trimIndent()
      )
    }

    val filter = ReceiptFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      phone = "01011112222", // 복호 비교 대상
      userId = null,
      userName = null,
      userNickName = null,
      tagUid = null,
      storeId = null,
      businessNo = null,
      storeName = "매장", // starts with
      franchiseCode = null,
      sort = listOf(io.allink.receipt.api.domain.Sorter(field = "issueDate", direction = "desc")),
      page = io.allink.receipt.api.domain.Page(page = 1, pageSize = 10)
    )

    // when
    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    // then
    assertThat(page.totalCount).isEqualTo(1)
    assertThat(page.items.first().id).isEqualTo("R-1")
    assertThat(page.items.first().store.storeName).isEqualTo("매장A")
  }

  @Test
  fun `Find all should filter by user nick name`() = runBlocking {
    // given
    val encPhoneA = AES256Util.encrypt("01033334444", Constant.AES256_KEY)
    val encNickA = AES256Util.encrypt("길동", Constant.AES256_KEY)

    val encPhoneB = AES256Util.encrypt("01055556666", Constant.AES256_KEY)
    val encNickB = AES256Util.encrypt("철수", Constant.AES256_KEY)

    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-10', '가게10')")
      stmt.execute("INSERT INTO \"user\" (uuid, name, phone, nickname) VALUES ('U-10', '홍길동', '$encPhoneA', '$encNickA')")
      stmt.execute("INSERT INTO \"user\" (uuid, name, phone, nickname) VALUES ('U-11', '김철수', '$encPhoneB', '$encNickB')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-10', 'S-10', '2025-06-05 00:00:00', 'U-10', 'PAY', 1000)")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-11', 'S-10', '2025-06-04 00:00:00', 'U-11', 'PAY', 2000)")
    }

    val filter = ReceiptFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      phone = null,
      userId = null,
      userName = null,
      userNickName = "길동",
      tagUid = null,
      storeId = null,
      businessNo = null,
      storeName = null,
      franchiseCode = null,
      sort = listOf(io.allink.receipt.api.domain.Sorter(field = "issueDate", direction = "desc")),
      page = io.allink.receipt.api.domain.Page(page = 1, pageSize = 10)
    )

    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    assertThat(page.totalCount).isEqualTo(1)
    assertThat(page.items.first().id).isEqualTo("R-10")
  }

  @Test
  fun `Find all should filter by user name prefix`() = runBlocking {
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-20', '가게20')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-20', '홍길동')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-21', '김철수')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-20', 'S-20', '2025-06-02 00:00:00', 'U-20', 'PAY', 1000)")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-21', 'S-20', '2025-06-01 00:00:00', 'U-21', 'PAY', 2000)")
    }

    val filter = ReceiptFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      phone = null,
      userId = null,
      userName = "홍",
      userNickName = null,
      tagUid = null,
      storeId = null,
      businessNo = null,
      storeName = null,
      franchiseCode = null,
      sort = listOf(io.allink.receipt.api.domain.Sorter(field = "issueDate", direction = "desc")),
      page = io.allink.receipt.api.domain.Page(page = 1, pageSize = 10)
    )

    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    assertThat(page.totalCount).isEqualTo(1)
    assertThat(page.items.first().id).isEqualTo("R-20")
  }

  @Test
  fun `Find all should filter by franchise code and business no`() = runBlocking {
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, franchise_code, business_no) VALUES ('S-30', 'A', 'F-1', '111')")
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, franchise_code, business_no) VALUES ('S-31', 'B', 'F-2', '222')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-30', '사용자1')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-31', '사용자2')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-30', 'S-30', '2025-06-02 00:00:00', 'U-30', 'PAY', 1000)")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-31', 'S-31', '2025-06-01 00:00:00', 'U-31', 'PAY', 2000)")
    }

    val filter = ReceiptFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      phone = null,
      userId = null,
      userName = null,
      userNickName = null,
      tagUid = null,
      storeId = null,
      businessNo = "111",
      storeName = null,
      franchiseCode = "F-1",
      sort = listOf(io.allink.receipt.api.domain.Sorter(field = "issueDate", direction = "desc")),
      page = io.allink.receipt.api.domain.Page(page = 1, pageSize = 10)
    )

    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    assertThat(page.totalCount).isEqualTo(1)
    assertThat(page.items.first().id).isEqualTo("R-30")
  }

  @Test
  fun `Find all should filter by tag uid and store id`() = runBlocking {
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-40', 'A')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-40', '사용자1')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, tag_id, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-40', 'T-1', 'S-40', '2025-06-02 00:00:00', 'U-40', 'PAY', 1000)")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, tag_id, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-41', 'T-2', 'S-40', '2025-06-01 00:00:00', 'U-40', 'PAY', 2000)")
    }

    val filter = ReceiptFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      phone = null,
      userId = null,
      userName = null,
      userNickName = null,
      tagUid = "T-2",
      storeId = "S-40",
      businessNo = null,
      storeName = null,
      franchiseCode = null,
      sort = listOf(io.allink.receipt.api.domain.Sorter(field = "issueDate", direction = "desc")),
      page = io.allink.receipt.api.domain.Page(page = 1, pageSize = 10)
    )

    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    assertThat(page.totalCount).isEqualTo(1)
    assertThat(page.items.first().id).isEqualTo("R-41")
  }

  @Test
  fun `Find all should paginate with sort desc`() = runBlocking {
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-50', 'A')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-50', '사용자1')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-50', 'S-50', '2025-06-05 00:00:00', 'U-50', 'PAY', 1000)")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-51', 'S-50', '2025-06-04 00:00:00', 'U-50', 'PAY', 2000)")
    }

    val filterPage1 = ReceiptFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      phone = null,
      userId = null,
      userName = null,
      userNickName = null,
      tagUid = null,
      storeId = null,
      businessNo = null,
      storeName = null,
      franchiseCode = null,
      sort = listOf(io.allink.receipt.api.domain.Sorter(field = "issueDate", direction = "desc")),
      page = io.allink.receipt.api.domain.Page(page = 1, pageSize = 1)
    )

    val filterPage2 = filterPage1.copy(page = io.allink.receipt.api.domain.Page(page = 2, pageSize = 1))

    val page1 = TransactionUtil.withTransaction { repository.findAll(filterPage1) }
    val page2 = TransactionUtil.withTransaction { repository.findAll(filterPage2) }

    assertThat(page1.items.first().id).isEqualTo("R-50")
    assertThat(page2.items.first().id).isEqualTo("R-51")
  }

  @Test
  fun `Find all should ignore unknown sort and use valid one`() = runBlocking {
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-60', '상점60')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-60', '사용자60')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-60A', 'S-60', '2025-06-10 09:00:00', 'U-60', 'PAY', 1000)")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-60B', 'S-60', '2025-06-10 09:00:00', 'U-60', 'PAY', 2000)")
    }

    val filter = ReceiptFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      sort = listOf(
        io.allink.receipt.api.domain.Sorter(field = "unknown", direction = "desc"),
        io.allink.receipt.api.domain.Sorter(field = "receiptAmount", direction = "desc")
      ),
      page = io.allink.receipt.api.domain.Page(page = 1, pageSize = 10),
      phone = null, userId = null, userName = null, userNickName = null, tagUid = null, storeId = null, businessNo = null, storeName = null, franchiseCode = null
    )

    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    assertThat(page.items.first().id).isEqualTo("R-60B")
  }

  @Test
  fun `Find all should apply multi sort receipt amount desc then issue date asc`() = runBlocking {
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-61', '상점61')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-61', '사용자61')")
      // same amount 3000, different issueDate
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-61A', 'S-61', '2025-06-10 08:00:00', 'U-61', 'PAY', 3000)")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-61B', 'S-61', '2025-06-10 07:00:00', 'U-61', 'PAY', 3000)")
      // higher amount 4000 should come before the above regardless of date
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-61C', 'S-61', '2025-06-10 09:00:00', 'U-61', 'PAY', 4000)")
    }

    val filter = ReceiptFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      sort = listOf(
        io.allink.receipt.api.domain.Sorter(field = "receiptAmount", direction = "desc"),
        io.allink.receipt.api.domain.Sorter(field = "issueDate", direction = "asc")
      ),
      page = io.allink.receipt.api.domain.Page(page = 1, pageSize = 10),
      phone = null, userId = null, userName = null, userNickName = null, tagUid = null, storeId = null, businessNo = null, storeName = null, franchiseCode = null
    )

    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    // order should be: R-61C (4000), then among 3000 amounts the earlier date first -> R-61B, R-61A
    assertThat(page.items.map { it.id }).containsExactly("R-61C", "R-61B", "R-61A")
  }

  @Test
  fun `Find all should return empty when page out of range`() = runBlocking {
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-62', '상점62')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-62', '사용자62')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-62A', 'S-62', '2025-06-10 08:00:00', 'U-62', 'PAY', 1000)")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-62B', 'S-62', '2025-06-10 09:00:00', 'U-62', 'PAY', 2000)")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-62C', 'S-62', '2025-06-10 10:00:00', 'U-62', 'PAY', 3000)")
    }

    val filter = ReceiptFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      sort = listOf(io.allink.receipt.api.domain.Sorter(field = "issueDate", direction = "desc")),
      page = io.allink.receipt.api.domain.Page(page = 5, pageSize = 2),
      phone = null, userId = null, userName = null, userNickName = null, tagUid = null, storeId = null, businessNo = null, storeName = null, franchiseCode = null
    )

    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    assertThat(page.totalCount).isEqualTo(3)
    assertThat(page.totalPages).isEqualTo(2)
    assertThat(page.items).isEmpty()
  }

  @Test
  fun `Find all should include period bounds`() = runBlocking {
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-63', '상점63')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-63', '사용자63')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-63A', 'S-63', '2025-06-01T00:00:00', 'U-63', 'PAY', 1000)")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-63B', 'S-63', '2025-06-30T23:59:59', 'U-63', 'PAY', 2000)")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-63C', 'S-63', '2025-05-31T23:59:59', 'U-63', 'PAY', 3000)")
    }

    val filter = ReceiptFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      sort = listOf(io.allink.receipt.api.domain.Sorter(field = "issueDate", direction = "asc")),
      page = io.allink.receipt.api.domain.Page(page = 1, pageSize = 10),
      phone = null, userId = null, userName = null, userNickName = null, tagUid = null, storeId = null, businessNo = null, storeName = null, franchiseCode = null
    )

    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    // Only R-63A and R-63B should be included, R-63C is out of range
    assertThat(page.items.map { it.id }).containsExactly("R-63A", "R-63B")
  }

  @Test
  fun `Find all should filter by user id only`() = runBlocking {
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-70', '상점70')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-70', '사용자70')")
      stmt.execute("INSERT INTO \"user\" (uuid, name) VALUES ('U-71', '사용자71')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-70A', 'S-70', '2025-06-10 10:00:00', 'U-70', 'PAY', 1000)")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-70B', 'S-70', '2025-06-10 11:00:00', 'U-71', 'PAY', 2000)")
    }

    val filter = ReceiptFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      userId = "U-70",
      sort = listOf(io.allink.receipt.api.domain.Sorter(field = "issueDate", direction = "desc")),
      page = io.allink.receipt.api.domain.Page(page = 1, pageSize = 10)
    )

    val page = TransactionUtil.withTransaction { repository.findAll(filter) }
    assertThat(page.totalCount).isEqualTo(1)
    assertThat(page.items.first().id).isEqualTo("R-70A")
  }

  @Test
  fun `findAll_should_return_empty_when_phone_not_matched`() = runBlocking {
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name) VALUES ('S-71', '상점71')")
      stmt.execute("INSERT INTO \"user\" (uuid, name, phone) VALUES ('U-72', '사용자72', 'ENCRYPTED-ANYTHING')")
      stmt.execute("INSERT INTO \"receipt_issue\" (issued_receipt_uid, store_uid, issue_date, user_uid, receipt_type, receipt_amount) VALUES ('R-71A', 'S-71', '2025-06-10 10:00:00', 'U-72', 'PAY', 1000)")
    }

    val filter = ReceiptFilter(
      period = io.allink.receipt.api.domain.PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      phone = "010-THIS-NOT-MATCH", // 암호화 후 비교에서 미일치
      sort = listOf(io.allink.receipt.api.domain.Sorter(field = "issueDate", direction = "desc")),
      page = io.allink.receipt.api.domain.Page(page = 1, pageSize = 10)
    )

    val page = TransactionUtil.withTransaction { repository.findAll(filter) }
    assertThat(page.totalCount).isEqualTo(0)
    assertThat(page.items).isEmpty()
  }
}
