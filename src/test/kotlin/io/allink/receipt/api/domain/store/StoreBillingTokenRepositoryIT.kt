package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.testsupport.DbTestUtil
import io.allink.receipt.api.testsupport.PostgresContainerBase
import io.allink.receipt.api.testsupport.R2dbcExposedInit
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

/**
 * @author Devonshin
 * @date 2025-09-13
 */
class StoreBillingTokenRepositoryIT : PostgresContainerBase() {

  private lateinit var repository: StoreBillingTokenRepository

  @BeforeEach
  fun setUp() {
    runJdbc { stmt ->
      DbTestUtil.dropCoreTables(stmt)
      DbTestUtil.createStoreTables(stmt)
    }

    R2dbcExposedInit.init(
      host = container.host,
      port = container.getMappedPort(5432),
      database = container.databaseName,
      username = container.username,
      password = container.password
    )

    repository = StoreBillingTokenRepositoryImpl(StoreBillingTokenTable)
  }

  @Test
  fun `findAllActiveByBusinessNo should return only ACTIVE tokens`() = runBlocking {
    val now = LocalDateTime.parse("2025-06-02T00:00:00")
    val user = UUID.randomUUID()
    val u1 = UUID.randomUUID()
    val u2 = UUID.randomUUID()
    val u3 = UUID.randomUUID()

    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store_billing_token\" (token_uuid, business_no, token, token_info, status, reg_date, reg_by) VALUES ('$u1', '111-11-11111', 'TOK-A', 'INFO', 'ACTIVE', '$now', '$user')")
      stmt.execute("INSERT INTO \"store_billing_token\" (token_uuid, business_no, token, token_info, status, reg_by) VALUES ('$u2', '111-11-11111', 'TOK-B', NULL, 'INACTIVE', '$user')")
      stmt.execute("INSERT INTO \"store_billing_token\" (token_uuid, business_no, token, token_info, status, reg_date, reg_by) VALUES ('$u3', '222-22-22222', 'TOK-C', NULL, 'ACTIVE', '$now', '$user')")
    }

    val list = TransactionUtil.withTransaction { repository.findAllActiveByBusinessNo("111-11-11111") }

    assertThat(list).isNotNull
    assertThat(list!!.map { it.token }).containsExactly("TOK-A")
  }

  @Test
  fun `findAllByBusinessNo should return all tokens and empty on blank`() = runBlocking {
    val now = LocalDateTime.parse("2025-06-02T00:00:00")
    val user = UUID.randomUUID()
    val u1 = UUID.randomUUID()
    val u2 = UUID.randomUUID()

    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store_billing_token\" (token_uuid, business_no, token, token_info, status, reg_date, reg_by) VALUES ('$u1', '333-33-33333', 'TK-1', NULL, 'ACTIVE', '$now', '$user')")
      stmt.execute("INSERT INTO \"store_billing_token\" (token_uuid, business_no, token, token_info, status, reg_by) VALUES ('$u2', '333-33-33333', 'TK-2', 'I', 'ACTIVE', '$user')")
    }

    val list = TransactionUtil.withTransaction { repository.findAllByBusinessNo("333-33-33333") }
    assertThat(list).isNotNull
    assertThat(list!!.size).isEqualTo(2)
    assertThat(list.map { it.token }.toSet()).isEqualTo(setOf("TK-1", "TK-2"))

    val emptyList = TransactionUtil.withTransaction { repository.findAllByBusinessNo("") }
    assertThat(emptyList).isEmpty()
  }

  @Test
  fun `find by id should map fields`() = runBlocking {
    val now = LocalDateTime.parse("2025-06-02T00:00:00")
    val user = UUID.randomUUID()
    val u1 = UUID.randomUUID()

    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"store_billing_token\" (token_uuid, business_no, token, token_info, status, reg_date, reg_by) VALUES ('$u1', '444-44-44444', 'TK-X', 'META', 'ACTIVE', '$now', '$user')")
    }

    val entity = TransactionUtil.withTransaction { repository.find(u1) }
    assertThat(entity).isNotNull
    assertThat(entity!!.id).isEqualTo(u1)
    assertThat(entity.businessNo).isEqualTo("444-44-44444")
    assertThat(entity.token).isEqualTo("TK-X")
    assertThat(entity.tokenInfo).isEqualTo("META")
    assertThat(entity.status).isEqualTo(StatusCode.ACTIVE)
  }
}