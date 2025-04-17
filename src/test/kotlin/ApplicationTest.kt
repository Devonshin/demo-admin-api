package io.allink

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

  @Test
  fun testRoot() = testApplication {
    environment {
      config = ApplicationConfig("application-test.conf")
    }

    application {
//      module()
    }
    client.get("/").apply {
      assertEquals(HttpStatusCode.OK, status)
    }
  }

}
