package io.allink.receipt.api.domain.admin

import io.allink.io.allink.receipt.admin.config.TestConfigLoader.loadTestConfig
import io.allink.receipt.api.config.plugin.dataSource
import io.allink.receipt.api.domain.user.UserStatus
import io.ktor.server.testing.*
import kotlinx.serialization.Contextual
import org.jetbrains.exposed.v1.r2dbc.Database
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import java.time.LocalDateTime
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AdminRepositoryTest {

  private lateinit var adminRepository: AdminRepository
  private lateinit var database: Database

  @BeforeAll
  fun init() {
    println("init..")
    val config = loadTestConfig()
    database = Database.connect(dataSource(config!!.config("postgres")))
  }

  @AfterAll
  fun destroy() {
    println("Dropping tables and closing database...")
    database.connector().close()
  }

  @BeforeEach
  fun setup() {
    adminRepository = AdminRepositoryImpl(AdminTable)
  }

  @Test
  fun `admin crud 테스트`() = testApplication {
    var id: @Contextual UUID? = null
    try {
      // given
      val now = LocalDateTime.now()
      val adminModel = AdminModel(
        loginId = "adminLogin",
        password = "securePassword",
        fullName = "Admin User",
        role = MasterRole(),
        phone = "123456789",
        email = "admin@example.com",
        status = AdminStatus.ACTIVE,
        regDate = now,
        agencyUuid = null,
      )
      var createdAdmin: AdminModel? = adminRepository.create(adminModel)
      //
      assertEquals(adminModel.loginId, createdAdmin?.loginId)
      assertEquals(adminModel.password, createdAdmin?.password)
      assertEquals(adminModel.fullName, createdAdmin?.fullName)
      assertEquals(adminModel.role, createdAdmin?.role)
      assertEquals(adminModel.phone, createdAdmin?.phone)
      assertEquals(adminModel.email, createdAdmin?.email)
      assertEquals(adminModel.status, UserStatus.ACTIVE)
      assertEquals(adminModel.regDate, createdAdmin?.regDate)
      assertEquals(adminModel.modDate, createdAdmin?.modDate)

      id = createdAdmin?.id
      val find = adminRepository.find(id!!)
      assertNotNull(find)
      val regDate = createdAdmin?.regDate!!

      val updateModel = AdminModel(
        id = id,
        loginId = "updateAdminLogin",
        password = "updateSecurePassword",
        fullName = "update Admin User",
        role = BzAgencyStaffRole(),
        phone = "211222112",
        email = "update-admin@example.com",
        status = AdminStatus.INACTIVE,
        modDate = now,
        agencyUuid = null,
      )

      val updateCount = adminRepository.update(model = updateModel)
      val findUpdated = adminRepository.find(id)
      assertEquals(1, updateCount)
      assertEquals(updateModel.loginId, findUpdated?.loginId)
      assertEquals(updateModel.password, findUpdated?.password)
      assertEquals(updateModel.fullName, findUpdated?.fullName)
      assertEquals(updateModel.role, findUpdated?.role)
      assertEquals(updateModel.phone, findUpdated?.phone)
      assertEquals(updateModel.email, findUpdated?.email)
      assertEquals(UserStatus.INACTIVE, findUpdated?.status)
      assertEquals(updateModel.regDate, findUpdated?.regDate)
      assertEquals(updateModel.modDate, findUpdated?.modDate)

    } finally {
      adminRepository.delete(id!!)
      val deleted = adminRepository.find(id)
      assertNull(deleted)
    }
  }
}