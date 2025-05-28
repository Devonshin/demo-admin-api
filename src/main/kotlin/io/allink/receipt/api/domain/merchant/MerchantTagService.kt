package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.PagedResult
import java.util.UUID


interface MerchantTagService {

  suspend fun getTags(merchantTagFilter: MerchantTagFilter): PagedResult<SimpleMerchantTagModel>
  suspend fun getTag(tagId: String): MerchantTagModel
  suspend fun modifyTag(modify: MerchantTagModifyModel, userUuid: UUID): MerchantTagModel

}
