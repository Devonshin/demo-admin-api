package io.allink.receipt.api.domain.store.npoint

import io.allink.receipt.api.domain.code.ServiceCodeModel
import java.time.LocalDateTime
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.store.npoint
 * Created: Devonshin
 * Date: 10/06/2025
 */

interface NPointStoreServiceService {

  suspend fun registNPointStoreReviewService(
    merchantSelectedServices: List<NPointStoreServiceModifyModel>,
    storeUid: String,
    userUuid: UUID,
    yyMMddHHmm: String,
    now: LocalDateTime
  ): List<NPointStoreServiceModifyModel>

  suspend fun registService(
    merchantSelectedService: NPointStoreServiceModel
  ): NPointStoreServiceModel

  suspend fun getServiceCodes(): Map<String, ServiceCodeModel>
  suspend fun getStoreServices(storeUid: String): List<NPointStoreServiceModel>
  suspend fun cancelNPointStoreServices(storeUid: String)
}