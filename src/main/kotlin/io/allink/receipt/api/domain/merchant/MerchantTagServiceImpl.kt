package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.PagedResult
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException


class MerchantTagServiceImpl(
  val merchantTagRepository: MerchantTagRepository
): MerchantTagService
{

  override suspend fun getTags(merchantTagFilter: MerchantTagFilter): PagedResult<SimpleMerchantTagModel> {
    return merchantTagRepository.findAll(merchantTagFilter)
  }

  override suspend fun getTag(tagId: String): MerchantTagModel {
    if(tagId.isEmpty()) throw BadRequestException("No tag id provided")
    return merchantTagRepository.find(tagId)?: throw NotFoundException("No tag found for id $tagId")
  }
}
