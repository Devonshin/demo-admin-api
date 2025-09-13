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
 * @author Devonshin
 * @date 2025-09-13
 */
class NPointStoreServiceRepositoryIT : PostgresContainerBase() {

  private lateinit var repository: NPointStoreServiceRepository

  @BeforeEach
  fun setUp() {
    runJdbc { stmt ->
      DbTestUtil.dropCoreTables(stmt)
      DbTestUtil.createStoreTables(stmt)
      // service_code 테이블 생성 및 기본 데이터
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
      stmt.execute("INSERT INTO \"service_code\" (service_code, service_group, service_name, status) VALUES ('REVIEWPT','POINT','리뷰포인트','ACTIVE')")
    }

    R2dbcExposedInit.init(
      host = container.host,
      port = container.getMappedPort(5432),
      database = container.databaseName,
      username = container.username,
      password = container.password
    )

    repository = NPointStoreServiceRepositoryImpl(NPointStoreServiceTable)
  }

  @Test
  fun `findAllStoreService by storeUid should return active items grouped by seq`() = runBlocking {
    val now = LocalDateTime.parse("2025-06-02T00:00:00")
    val user = UUID.randomUUID()
    runJdbc { stmt ->
      // 두 개의 ACTIVE 서비스(동일 seq)
      stmt.execute("INSERT INTO \"n_point_store_service\" (store_service_seq, store_uid, service_code, service_charge, status, reg_date, reg_by) VALUES (1,'S-1','REVIEWPT',1000,'ACTIVE','$now','$user')")
      stmt.execute("INSERT INTO \"n_point_store_service\" (store_service_seq, store_uid, service_code, service_charge, status, reg_date, reg_by) VALUES (1,'S-1','REVIEWPT',2000,'ACTIVE','$now','$user')")
      // PENDING은 필터링되어야 함
      stmt.execute("INSERT INTO \"n_point_store_service\" (store_service_seq, store_uid, service_code, service_charge, status, reg_date, reg_by) VALUES (2,'S-1','REVIEWPT',3000,'PENDING','$now','$user')")
    }

    val map = TransactionUtil.withTransaction { repository.findAllStoreService("S-1") }
    assertThat(map).containsKey(1)
    assertThat(map[1]).isNotNull
    assertThat(map[1]!!.size).isEqualTo(2)
    assertThat(map.keys).doesNotContain(2)
  }

  @Test
  fun `cancelAllStoreService should inactivate non-active and non-expired`() = runBlocking {
    val now = LocalDateTime.parse("2025-06-02T00:00:00")
    val user = UUID.randomUUID()
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"n_point_store_service\" (store_service_seq, store_uid, service_code, service_charge, status, reg_date, reg_by) VALUES (1,'S-2','REVIEWPT',1000,'PENDING','$now','$user')")
      stmt.execute("INSERT INTO \"n_point_store_service\" (store_service_seq, store_uid, service_code, service_charge, status, reg_date, reg_by) VALUES (2,'S-2','REVIEWPT',1000,'ACTIVE','$now','$user')")
      stmt.execute("INSERT INTO \"n_point_store_service\" (store_service_seq, store_uid, service_code, service_charge, status, reg_date, reg_by) VALUES (3,'S-2','REVIEWPT',1000,'EXPIRED','$now','$user')")
    }

    val updated = TransactionUtil.withTransaction { repository.cancelAllStoreService("S-2") }
    assertThat(updated).isEqualTo(1) // PENDING 1건만 INACTIVE로 변경
  }

  @Test
  fun `findAllStoreService by seq and storeUid should return items`() = runBlocking {
    val now = LocalDateTime.parse("2025-06-02T00:00:00")
    val user = UUID.randomUUID()
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"n_point_store_service\" (store_service_seq, store_uid, service_code, service_charge, status, reg_date, reg_by) VALUES (9,'S-3','REVIEWPT',1000,'ACTIVE','$now','$user')")
    }

    val list = TransactionUtil.withTransaction { repository.findAllStoreService(9, "S-3") }
    assertThat(list).isNotEmpty
    assertThat(list.first().status).isEqualTo(StatusCode.ACTIVE)
  }

  @Test
  fun `find by composite id should return joined service`() = runBlocking {
    val now = LocalDateTime.parse("2025-06-02T00:00:00")
    val user = UUID.randomUUID()
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"n_point_store_service\" (store_service_seq, store_uid, service_code, service_charge, status, reg_date, reg_by) VALUES (7,'S-9','REVIEWPT',1500,'ACTIVE','$now','$user')")
    }

    val id = NPointStoreServiceId(storeServiceSeq = 7, storeUid = "S-9", serviceCode = "REVIEWPT")
    val found = TransactionUtil.withTransaction { repository.find(id) }
    assertThat(found).isNotNull
    assertThat(found!!.id?.storeServiceSeq).isEqualTo(7)
    assertThat(found.service!!.serviceName).isEqualTo("리뷰포인트")
  }

  @Test
  fun `update should modify fields`() = runBlocking {
    val now = LocalDateTime.parse("2025-06-02T00:00:00")
    val user = UUID.randomUUID()
    runJdbc { stmt ->
      stmt.execute("INSERT INTO \"n_point_store_service\" (store_service_seq, store_uid, service_code, service_charge, reward_deposit, reward_point, service_commission, status, reg_date, reg_by) VALUES (8,'S-8','REVIEWPT',1000,0,0,0,'ACTIVE','$now','$user')")
    }

    val id = NPointStoreServiceId(storeServiceSeq = 8, storeUid = "S-8", serviceCode = "REVIEWPT")
    val affected = TransactionUtil.withTransaction {
      repository.update(
        NPointStoreServiceModel(
          id = id,
          service = io.allink.receipt.api.domain.code.ServiceCodeModel(
            id = "REVIEWPT", serviceGroup = "POINT", serviceName = "리뷰포인트", price = null, status = io.allink.receipt.api.domain.code.ServiceCodeStatus.ACTIVE, serviceType = null
          ),
          serviceCharge = 2000,
          rewardDeposit = 100,
          rewardPoint = 50,
          serviceCommission = 10,
          status = StatusCode.ACTIVE,
          regDate = now,
          regBy = user
        )
      )
    }
    assertThat(affected).isEqualTo(1)

    val list = TransactionUtil.withTransaction { repository.findAllStoreService(8, "S-8") }
    assertThat(list).isNotEmpty
    assertThat(list.first().serviceCharge).isEqualTo(2000)
    assertThat(list.first().rewardDeposit).isEqualTo(100)
  }
}
