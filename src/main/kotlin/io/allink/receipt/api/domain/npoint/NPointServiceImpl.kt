package io.allink.receipt.api.domain.npoint

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.repository.TransactionUtil


class NPointServiceImpl(
  val nPointRepository: NPointRepository
): NPointService {

  override suspend fun getAllNPointPay(
    nPointFilter: NPointFilter
  ): PagedResult<NPointPayModel> = TransactionUtil.withTransaction {
    nPointRepository.findAll(filter = nPointFilter)
  }

}
