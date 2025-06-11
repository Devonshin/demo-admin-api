// MerchantTagRepositoryImplTest.kt

package io.allink.receipt.api.domain.store

import io.allink.io.allink.receipt.admin.config.TestConfigLoader.loadTestConfig
import io.allink.receipt.api.config.plugin.dataSource
import io.allink.receipt.api.util.DateUtil
import io.ktor.server.config.*
import io.ktor.server.testing.*
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException
import org.jetbrains.exposed.v1.r2dbc.Database
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StoreRepositoryImplTest {

  private lateinit var repository: StoreRepository
  private lateinit var database: Database

  @BeforeAll
  fun init() {
    println("init..")
    val config   = loadTestConfig()
    database = Database.connect(dataSource(config!!.config("postgres")))
  }

  @AfterAll
  fun destroy() {
    println("Dropping tables and closing database...")
    database.connector().close()
  }

  @BeforeEach
  fun setUp() {
    repository = StoreRepositoryImpl(StoreTable)
  }

  @Test
  fun `create should insert a new merchant tag into the database and return the created model`() = testApplication {

    environment {
      config = ApplicationConfig("application-test.conf")
    }
    val id = UUID.randomUUID().toString()
    val userUuid = UUID.randomUUID()
    val store = StoreModel(
      id = id,
      storeName = "Test123",
      franchiseCode = "franchiseCode",
      zoneCode = "zoneCode",
      addr1 = "addr1",
      addr2 = "addr2",
      mapUrl = "mapUrl",
      tel = "tel",
      mobile = "mobile",
      managerName = "managerName",
      siteLink = "siteLink",
      workType = "workType",
      businessNo = "businessNo",
      businessNoLaw = "businessNoLaw",
      ceoName = "ceoName",
      businessType = "businessType",
      eventType = "eventType",
      email = "email",
      storeType = "storeType",
      iconUrl = "iconUrl",
      logoUrl = "logoUrl",
      receiptWidthInch = "3",
      status = StatusCode.ACTIVE,
      regDate = DateUtil.nowLocalDateTime(),
      regBy = userUuid
    )

    val result = repository.create(store)
    var created = false

    try {
      repository.create(result)
    } catch (ex: ExposedSQLException) {
      if (ex.message!!.contains("duplicate key")) {
        created = true
      }
    }

    assertTrue(created)

  }
}