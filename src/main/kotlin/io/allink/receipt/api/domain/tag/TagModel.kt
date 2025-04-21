package io.allink.receipt.api.domain.tag

import java.time.LocalDateTime

/**
* Package: io.allink.receipt.api.domain.tag
* Created: Devonshin
* Date: 18/04/2025
*/

data class TagModel(
  val name: String,
  val tagId: String,
  val status: TagStatus,
  val storeUid: String? = null,
  val regDate: LocalDateTime,
  val modDate: LocalDateTime? = null
)

enum class TagStatus {
  NORMAL,
  DELETED,
  PENDING
}

