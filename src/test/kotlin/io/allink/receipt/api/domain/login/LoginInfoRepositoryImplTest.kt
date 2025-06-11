package io.allink.io.allink.receipt.admin.domain.login

import io.allink.io.allink.receipt.admin.config.TestConfigLoader.loadTestConfig
import io.allink.receipt.api.config.plugin.dataSource
import io.allink.receipt.api.domain.login.LoginInfoModel
import io.allink.receipt.api.domain.login.LoginInfoRepositoryImpl
import io.allink.receipt.api.domain.login.LoginInfoTable
import io.allink.receipt.api.domain.login.LoginStatus
import io.allink.receipt.api.util.DateUtil.Companion.nowLocalDateTime
import io.ktor.server.testing.*
import kotlinx.serialization.Contextual
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.v1.r2dbc.Database
import org.junit.jupiter.api.*
import java.util.*
import kotlin.test.assertEquals

/**
 * Package: io.allink.io.allink.receipt.admin.domain.login
 * Created: Devonshin
 * Date: 14/04/2025
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LoginInfoRepositoryImplTest {

  private lateinit var loginInfoRepository: LoginInfoRepositoryImpl
  private lateinit var database: Database

  @BeforeAll
  fun init() {
    val config = loadTestConfig()
    database = Database.connect(dataSource(config!!.config("postgres")))

  }

  @BeforeEach
  fun setup() {
    loginInfoRepository = LoginInfoRepositoryImpl(LoginInfoTable)
  }

  @AfterAll
  fun destroy() {
    println("Dropping tables and closing database...")
    // 테이블 삭제 및 데이터베이스 리소스 종료
    transaction(database) {
//      SchemaUtils.drop(AdminTable) // 테이블 삭제
    }
    database.connector().close()
  }

  @Test
  fun `loginInfo crud 테스트`() = testApplication {

    val now = nowLocalDateTime()
    val userUuid = UUID.randomUUID()
    var id: @Contextual UUID? = null
    try {
      val loginInfo = LoginInfoModel(
        userUuid = userUuid,
        verificationCode = "123456",
        expireDate = now.plusMinutes(5),
        status = LoginStatus.ACTIVE
      )

      val created = loginInfoRepository.create(loginInfo)
      id = created.id
      assertNotNull(created.id)
      assertEquals(loginInfo.userUuid, created.userUuid)
      assertEquals(loginInfo.verificationCode, created.verificationCode)
      assertEquals(loginInfo.expireDate, created.expireDate)
      assertEquals(loginInfo.status, created.status)

      val updateUserUuid = UUID.randomUUID()
      val updateLoginInfo = LoginInfoModel(
        id = id,
        userUuid = updateUserUuid,
        verificationCode = "223456",
        expireDate = now.plusMinutes(5),
        status = LoginStatus.INACTIVE
      )

      val updateCount = loginInfoRepository.update(updateLoginInfo)
      val findUpdated = loginInfoRepository.find(id!!)
      assertNotNull(findUpdated)

      assertEquals(1, updateCount)
      assertEquals(updateLoginInfo.userUuid, findUpdated.userUuid)
      assertEquals(updateLoginInfo.verificationCode, findUpdated.verificationCode)
      assertEquals(updateLoginInfo.expireDate, findUpdated.expireDate)
      assertEquals(updateLoginInfo.status, findUpdated.status)

    } finally {
      loginInfoRepository.delete(id!!)
    }
  }

}