package io.allink.receipt.api.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Package: io.allink.receipt.admin.util
 * Created: Devonshin
 * Date: 13/04/2025
 */

class DateUtil {

  val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

  companion object {

    val zoneId: ZoneId = ZoneId.systemDefault()
    fun nowLocalDateTime(): LocalDateTime {
      return LocalDateTime.now()
    }

    fun nowLocalDateTimeStr(): String {
      return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }
    fun nowLocalDateTimeStrMs(): String {
      return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
    }

    fun nowLocalDateTimeFormat(dateTime: LocalDateTime): String {
      return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    }

    fun nowInstant(dateTime: LocalDateTime): Instant {
      return dateTime.toInstant(zoneId.rules.getOffset(dateTime))
    }

    fun nowInstant(): Instant {
      return LocalDateTime.now().atZone(zoneId).toInstant()
    }


  }
}