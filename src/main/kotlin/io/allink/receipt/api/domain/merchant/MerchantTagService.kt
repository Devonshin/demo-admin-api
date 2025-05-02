package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.PagedResult


interface MerchantTagService {

  suspend fun getTags(merchantTagFilter: MerchantTagFilter): PagedResult<SimpleMerchantTagModel>
  suspend fun getTag(tagId: String): MerchantTagModel

}
