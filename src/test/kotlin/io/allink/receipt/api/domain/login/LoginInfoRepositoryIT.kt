package io.allink.receipt.api.domain.login

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
class LoginInfoRepositoryIT : PostgresContainerBase() {

  private lateinit var repository: LoginInfoRepository

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

    repository = LoginInfoRepositoryImpl(LoginInfoTable)
  }

  @Test
  fun `Create find update delete should work`() = runBlocking {
    val user = UUID.randomUUID()
    val now = LocalDateTime.parse("2025-06-02T00:00:00")
    val model = LoginInfoModel(
      id = null,
      userUuid = user,
      verificationCode = "123456",
      expireDate = now.plusMinutes(5),
      status = LoginStatus.PENDING,
      loginDate = null,
    )

    val created = TransactionUtil.withTransaction { repository.create(model) }
    assertThat(created.id).isNotNull()

    val found = TransactionUtil.withTransaction { repository.find(created.id!!) }
    assertThat(found).isNotNull()
    assertThat(found!!.verificationCode).isEqualTo("123456")
    assertThat(found.status).isEqualTo(LoginStatus.PENDING)

    val updated = found.copy(status = LoginStatus.ACTIVE, loginDate = now)
    val updCount = TransactionUtil.withTransaction { repository.update(updated) }
    assertThat(updCount).isEqualTo(1)

    val found2 = TransactionUtil.withTransaction { repository.find(created.id!!) }
    assertThat(found2!!.status).isEqualTo(LoginStatus.ACTIVE)
    assertThat(found2.loginDate).isEqualTo(now)

    val delCount = TransactionUtil.withTransaction { repository.delete(created.id!!) }
    assertThat(delCount).isEqualTo(1)

    val found3 = TransactionUtil.withTransaction { repository.find(created.id!!) }
    assertThat(found3).isNull()
  }
}
