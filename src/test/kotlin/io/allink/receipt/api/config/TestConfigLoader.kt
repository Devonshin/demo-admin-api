package io.allink.io.allink.receipt.admin.config

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.*
import java.io.File

/**
 * Package: io.allink.io.allink.receipt.admin.config
 * Created: Devonshin
 * Date: 13/04/2025
 */

object TestConfigLoader {
    fun loadTestConfig(): ApplicationConfig? {
      return try {
        val resource = this::class.java.classLoader.getResource("application-test.conf")
          ?: throw IllegalStateException("application-test.conf not found")

        HoconApplicationConfig(ConfigFactory.parseFile(File(resource.file)))

      } catch (e: Exception) {
        println("Error loading test configuration: ${e.message}")
        null
      }
    }

}