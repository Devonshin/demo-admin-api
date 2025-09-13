package io.allink.receipt.api.domain.store

import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.PeriodFilter
import io.allink.receipt.api.domain.Sorter
import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.testsupport.PostgresContainerBase
import io.allink.receipt.api.testsupport.DbTestUtil
import io.allink.receipt.api.testsupport.R2dbcExposedInit
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

/**
 * @file StoreRepositoryIT.kt
 * @brief Testcontainers PostgreSQL + Exposed(v1 r2dbc) 기반 StoreRepository 통합 테스트
 * @author Devonshin
 * @date 2025-09-12
 */
class StoreRepositoryIT : PostgresContainerBase() {

  private lateinit var repository: StoreRepository

  @BeforeEach
  fun setUp() {
    // 공용 DDL 유틸로 핵심 테이블 초기화
    runJdbc { stmt ->
      DbTestUtil.dropCoreTables(stmt)
      DbTestUtil.createStoreTables(stmt)
    }

    // Exposed v1 r2dbc 초기화
    R2dbcExposedInit.init(
      host = container.host,
      port = container.getMappedPort(5432),
      database = container.databaseName,
      username = container.username,
      password = container.password
    )

    repository = StoreRepositoryImpl(StoreTable)
  }

  @Test
  fun `find_should_return_store`() = runBlocking {
    // given
    runJdbc { stmt ->
      stmt.execute(
        """
        INSERT INTO "store" (store_uid, store_name, franchise_code, business_no, ceo_name, tel, business_type, event_type, status, reg_date)
        VALUES ('s-1', '가맹점A', 'F-1', '111-11-11111', '대표', '021234567', '커피', '카페', 'NORMAL', '2025-06-01T12:00:00')
        """.trimIndent()
      )
    }

    // when
    val entity = TransactionUtil.withTransaction { repository.find("s-1") }

    // then
    assertThat(entity).isNotNull()
    assertThat(entity!!.id).isEqualTo("s-1")
    assertThat(entity.storeName).isEqualTo("가맹점A")
  }

  @Test
  fun `findAll_should_filter_by_name_and_period_and_sort`() = runBlocking {
    // given 3개 데이터
    fun ins(id: String, name: String, reg: String) {
      runJdbc { stmt ->
        stmt.execute(
          """
          INSERT INTO "store" (store_uid, store_name, reg_date)
          VALUES ('$id', '$name', '$reg')
          """.trimIndent()
        )
      }
    }
    ins("s-1", "가맹점A", "2025-06-01T00:00:00")
    ins("s-2", "가맹점B", "2025-06-02T00:00:00")
    ins("s-3", "이마트", "2025-06-03T00:00:00")

    val filter = StoreFilter(
      id = null,
      businessNo = null,
      name = "가맹",
      franchiseCode = null,
      period = PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-30T23:59:59")
      ),
      sort = listOf(Sorter(field = "name", direction = "asc")),
      page = Page(page = 1, pageSize = 10)
    )

    // when
    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    // then
    assertThat(page.totalCount).isEqualTo(2)
    assertThat(page.items.map { it.storeName }).containsExactly("가맹점A", "가맹점B")
  }

  @Test
  fun `findAll_with_agency_filter_should_return_only_that_agency`() = runBlocking {
    // given: 두 개 매장, 하나는 특정 대리점 UUID와 매핑
    val agencyId = UUID.randomUUID()
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"bz_agency\" (uuid, agency_name, business_no) VALUES ('$agencyId', '대리점1', '000-00-00000')")
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, bz_agency_uuid) VALUES ('s-10', 'A상점', '$agencyId')")
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, bz_agency_uuid) VALUES ('s-11', 'B상점', NULL)")
    }

    val filter = StoreFilter(
      id = null,
      businessNo = null,
      name = null,
      franchiseCode = null,
      period = PeriodFilter(LocalDateTime.parse("2025-01-01T00:00:00"), LocalDateTime.parse("2025-12-31T23:59:59")),
      sort = listOf(Sorter(field = "name", direction = "asc")),
      page = Page(page = 1, pageSize = 10)
    )

    // when
    val page = TransactionUtil.withTransaction { repository.findAll(filter, agencyId) }

    // then
    assertThat(page.totalCount).isEqualTo(1)
    assertThat(page.items.first().id).isEqualTo("s-10")
  }

  @Test
  fun `findByNameAndBzNo_should_match_exact_and_return_null_when_not_found`() = runBlocking {
    // given
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, business_no) VALUES ('s-100', 'A상점', '100-10-10000')")
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, business_no) VALUES ('s-101', 'B상점', '100-10-10000')")
    }

    // when
    val hit = TransactionUtil.withTransaction { repository.findByNameAndBzNo("A상점", "100-10-10000") }
    val miss = TransactionUtil.withTransaction { repository.findByNameAndBzNo("X상점", "100-10-10000") }

    // then
    assertThat(hit).isNotNull
    assertThat(hit!!.id).isEqualTo("s-100")
    assertThat(miss).isNull()
  }

  @Test
  fun `find_with_agencyId_should_enforce_agency_scope`() = runBlocking {
    // given
    val agencyId = UUID.randomUUID()
    val otherAgency = UUID.randomUUID()
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"bz_agency\" (uuid, agency_name, business_no) VALUES ('$agencyId', '대리점X', '123-45-67890')")
      stmt.execute("INSERT INTO \"bz_agency\" (uuid, agency_name, business_no) VALUES ('$otherAgency', '대리점Y', '987-65-43210')")
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, bz_agency_uuid) VALUES ('s-200', '에이전시상점', '$agencyId')")
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, bz_agency_uuid) VALUES ('s-201', '타에이전시상점', '$otherAgency')")
    }

    // when
    val allowed = TransactionUtil.withTransaction { repository.find("s-200", agencyId) }
    val denied = TransactionUtil.withTransaction { repository.find("s-201", agencyId) }

    // then
    assertThat(allowed).isNotNull
    assertThat(allowed!!.id).isEqualTo("s-200")
    assertThat(denied).isNull()
  }

  @Test
  fun `findAll_should_filter_by_franchise_and_businessNo_and_period_boundary`() = runBlocking {
    // given
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, franchise_code, business_no, reg_date) VALUES ('s-300', '경계상점1', 'FZ-1', '123-45-67890', '2025-06-01T00:00:00')")
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, franchise_code, business_no, reg_date) VALUES ('s-301', '경계상점2', 'FZ-1', '123-45-67890', '2025-06-02T00:00:00')")
      stmt.execute("INSERT INTO \"store\" (store_uid, store_name, franchise_code, business_no, reg_date) VALUES ('s-302', '경계상점3', 'FZ-2', '999-99-99999', '2025-06-02T00:00:01')")
    }

    val filter = StoreFilter(
      id = null,
      businessNo = "123-45-67890",
      name = null,
      franchiseCode = "FZ-1",
      period = PeriodFilter(
        from = LocalDateTime.parse("2025-06-01T00:00:00"),
        to = LocalDateTime.parse("2025-06-02T00:00:00")
      ),
      sort = listOf(Sorter(field = "regDate", direction = "asc")),
      page = Page(page = 1, pageSize = 10)
    )

    // when
    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    // then: 경계 포함(>= from, <= to)로 2건(s-300, s-301)만 반환
    assertThat(page.totalCount).isEqualTo(2)
    assertThat(page.items.map { it.id }).containsExactly("s-300", "s-301")
  }
}
