package io.allink.receipt.api.domain.user.event

import io.allink.receipt.api.domain.npoint.NPointUserModel
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/**
 * Package: io.allink.receipt.api.domain.user.event
 * Created: Devonshin
 * Date: 20/05/2025
 */

@Serializable
data class UserEventSessionModel(
  val user: NPointUserModel,
  val eventSessionId: String,
  val partnerReqUuid: String,
  val advertisementId: String,
  val status: String,
  val beginAt: String,
  val expireAt: String?,
  val totalReservedPoint: String?,
  val regDate: String,
  val modDate: @Contextual String?,
)
