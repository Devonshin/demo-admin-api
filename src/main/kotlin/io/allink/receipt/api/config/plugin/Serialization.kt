package io.allink.receipt.api.config.plugin

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Application.configureSerialization() {
  install(ContentNegotiation) {
    json(Json {
      serializersModule = kotlinx.serialization.modules.SerializersModule {
        contextual(LocalDateTime::class, LocalDateTimeSerializer)
      }
      prettyPrint = true
      isLenient = true
      encodeDefaults = true
      ignoreUnknownKeys = true // Ignore unknown keys during deserialization
      coerceInputValues = true
    })
  }
}

// LocalDateTime Serializer
object LocalDateTimeSerializer : KSerializer<LocalDateTime> {
  private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

  override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: LocalDateTime) {
    encoder.encodeString(value.format(formatter))
  }

  override fun deserialize(decoder: Decoder): LocalDateTime {
    return LocalDateTime.parse(decoder.decodeString(), formatter)
  }
}
