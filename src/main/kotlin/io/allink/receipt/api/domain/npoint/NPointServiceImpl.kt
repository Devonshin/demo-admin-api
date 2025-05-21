package io.allink.receipt.api.domain.npoint

import io.allink.receipt.api.domain.PagedResult


class NPointServiceImpl(
  val nPointRepository: NPointRepository
): NPointService {

  override suspend fun getAllNPointPay(nPointFilter: NPointFilter): PagedResult<NPointPayModel> {
    return nPointRepository.findAll(filter = nPointFilter)
  }

}
