package io.allink.receipt.api.domain.store.npoint

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
 * NPointStoreRepository 통합 테스트
 * - create/find/update 라운드 트립 및 상태 전이 검증
 * @author Devonshin
 * @date 2025-09-13
 */
class NPointStoreRepositoryIT : PostgresContainerBase() {

  private lateinit var repository: NPointStoreRepository

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

    repository = NPointStoreRepositoryImpl(NPointStoreTable)
  }

  @Test
  fun `create_should_insert_and_find`() = runBlocking {
    val now = LocalDateTime.parse("2025-06-02T00:00:00")
    val user = UUID.randomUUID()

    val created = TransactionUtil.withTransaction {
      repository.create(
        NPointStoreModel(
          id = "S-NP1",
          reservedPoints = 10,
          reviewPoints = 5,
          cumulativePoints = 0,
          regularPaymentAmounts = 9900,
          status = StatusCode.PENDING,
          serviceStartAt = now,
          pointRenewalType = PointRenewalType.AUTO_RENEWAL,
          regDate = now,
          regBy = user
        )
      )
    }

    val found = TransactionUtil.withTransaction { repository.find("S-NP1") }
    assertThat(found).isNotNull
    assertThat(found!!.id).isEqualTo(created.id)
    assertThat(found.regularPaymentAmounts).isEqualTo(9900)
    assertThat(found.status).isEqualTo(StatusCode.PENDING)
  }

  @Test
  fun `update_should_modify_fields`() = runBlocking {
    val now = LocalDateTime.parse("2025-06-02T00:00:00")
    val user = UUID.randomUUID()

    TransactionUtil.withTransaction {
      repository.create(
        NPointStoreModel(
          id = "S-NP2",
          reservedPoints = 0,
          reviewPoints = 0,
          cumulativePoints = 0,
          regularPaymentAmounts = 10000,
          status = StatusCode.PENDING,
          serviceStartAt = now,
          pointRenewalType = PointRenewalType.AUTO_RENEWAL,
          regDate = now,
          regBy = user
        )
      )
    }

    val modNow = LocalDateTime.parse("2025-06-03T00:00:00")
    val updater = UUID.randomUUID()

    val updatedRows = TransactionUtil.withTransaction {
      repository.update(
        NPointStoreModel(
          id = "S-NP2",
          reservedPoints = 1,
          reviewPoints = 10,
          cumulativePoints = 10,
          regularPaymentAmounts = 12000,
          status = StatusCode.DELETED,
          serviceStartAt = now,
          pointRenewalType = PointRenewalType.MANUAL_RENEWAL,
          regDate = now,
          modDate = modNow,
          regBy = user,
          modBy = updater
        )
      )
    }
    assertThat(updatedRows).isEqualTo(1)

    val found = TransactionUtil.withTransaction { repository.find("S-NP2") }
    assertThat(found).isNotNull
    assertThat(found!!.status).isEqualTo(StatusCode.DELETED)
    assertThat(found.regularPaymentAmounts).isEqualTo(12000)
    assertThat(found.modDate).isEqualTo(modNow)
  }
}