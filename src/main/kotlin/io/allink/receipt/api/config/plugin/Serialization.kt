package io.allink.receipt.api.config.plugin

import io.allink.receipt.api.domain.admin.AdvAgencyMasterRole
import io.allink.receipt.api.domain.admin.AdvAgencyStaffRole
import io.allink.receipt.api.domain.admin.BzAgencyMasterRole
import io.allink.receipt.api.domain.admin.BzAgencyStaffRole
import io.allink.receipt.api.domain.admin.MasterRole
import io.allink.receipt.api.domain.admin.MerchantMasterRole
import io.allink.receipt.api.domain.admin.MerchantStaffRole
import io.allink.receipt.api.domain.admin.Role
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.polymorphic
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun Application.configureSerialization() {
  install(ContentNegotiation) {
    json(Json {
      serializersModule = kotlinx.serialization.modules.SerializersModule {
        contextual(LocalDateTime::class, LocalDateTimeSerializer)
        contextual(UUID::class, UUIDSerializer)
        polymorphic(Role::class) {
          subclass(MasterRole::class, MasterRole.serializer())
          subclass(BzAgencyStaffRole::class, BzAgencyStaffRole.serializer())
          subclass(BzAgencyMasterRole::class, BzAgencyMasterRole.serializer())
          subclass(AdvAgencyStaffRole::class, AdvAgencyStaffRole.serializer())
          subclass(AdvAgencyMasterRole::class, AdvAgencyMasterRole.serializer())
          subclass(MerchantStaffRole::class, MerchantStaffRole.serializer())
          subclass(MerchantMasterRole::class, MerchantMasterRole.serializer())
        }
      }
      prettyPrint = true
      isLenient = true
      encodeDefaults = true
      ignoreUnknownKeys = true // Ignore unknown keys during deserialization
      coerceInputValues = true
    })
  }

}

object UUIDSerializer : KSerializer<UUID> {
  override val descriptor: SerialDescriptor =
    PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

  override fun serialize(encoder: Encoder, value: UUID) {
    encoder.encodeString(value.toString())
  }

  override fun deserialize(decoder: Decoder): UUID {
    return UUID.fromString(decoder.decodeString())
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
