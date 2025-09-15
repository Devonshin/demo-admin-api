package io.allink.receipt.api.domain.admin

import io.allink.receipt.api.repository.TransactionUtil
import io.allink.receipt.api.testsupport.PostgresContainerBase
import io.allink.receipt.api.testsupport.DbTestUtil
import io.allink.receipt.api.testsupport.R2dbcExposedInit
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * @file AdminRepositoryIT.kt
 * @brief Testcontainers PostgreSQL + Exposed(v1 r2dbc) 기반 AdminRepository 통합 테스트(CRUD/조회)
 */
class AdminRepositoryIT : PostgresContainerBase() {

  private lateinit var repository: AdminRepository

  @BeforeEach
  fun setUp() {
    // 1) Prepare schema via JDBC first (ensure the table exists before R2DBC/Exposed usage)
    runJdbc { stmt ->
      DbTestUtil.dropCoreTables(stmt)
      DbTestUtil.createStoreTables(stmt)
    }

    // 2) Initialize R2DBC/Exposed after DDL
    R2dbcExposedInit.init(
      host = container.host,
      port = container.getMappedPort(5432),
      database = container.databaseName,
      username = container.username,
      password = container.password
    )

    // 3) Repository under test
    repository = AdminRepositoryImpl(AdminTable)
  }

  @Test
  fun `Should create find update delete and query`() = runBlocking {
    // create
    val created = TransactionUtil.withTransaction {
      repository.create(
        AdminModel(
          id = null,
          loginId = "admin",
          password = null,
          fullName = "관리자",
          role = MasterRole(),
          phone = "01011112222",
          email = null,
          status = AdminStatus.ACTIVE,
          regBy = UUID.randomUUID(),
          modBy = null,
          agencyUuid = null
        )
      )
    }

    assertThat(created.id).isNotNull()

    // find by id
    val found = TransactionUtil.withTransaction { repository.find(created.id!!) }
    assertThat(found).isNotNull()
    assertThat(found!!.fullName).isEqualTo("관리자")

    // find by phone
    val byPhone = TransactionUtil.withTransaction { repository.findByPhone("01011112222") }
    assertThat(byPhone?.id).isEqualTo(created.id)

    // update
    val updatedCount = TransactionUtil.withTransaction {
      repository.update(created.copy(fullName = "관리자2"))
    }
    assertThat(updatedCount).isEqualTo(1)
    val afterUpdate = TransactionUtil.withTransaction { repository.find(created.id!!) }
    assertThat(afterUpdate?.fullName).isEqualTo("관리자2")

    // findAllByAgencyId (none)
    val agencyAdmins = TransactionUtil.withTransaction { repository.findAllByAgencyId(UUID.randomUUID()).toList() }
    assertThat(agencyAdmins).isEmpty()

    // delete
    val deleted = TransactionUtil.withTransaction { repository.delete(created.id!!) }
    assertThat(deleted).isEqualTo(1)
    val afterDelete = TransactionUtil.withTransaction { repository.find(created.id!!) }
    assertThat(afterDelete).isNull()
  }

  @Test
  fun `Find by user uuid should return same entity`() = runBlocking {
    val created = TransactionUtil.withTransaction {
      repository.create(
        AdminModel(
          id = null,
          loginId = "admin2",
          password = null,
          fullName = "관리자A",
          role = MasterRole(),
          phone = "01022223333",
          email = null,
          status = AdminStatus.ACTIVE,
          regBy = UUID.randomUUID(),
          modBy = null,
          agencyUuid = null
        )
      )
    }
    val byUuid = TransactionUtil.withTransaction { repository.findByUserUuid(created.id!!) }
    assertThat(byUuid).isNotNull()
    assertThat(byUuid!!.id).isEqualTo(created.id)
    assertThat(byUuid.fullName).isEqualTo("관리자A")
  }
}
