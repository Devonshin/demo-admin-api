package io.allink.receipt.api.domain.npoint

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.repository.TransactionUtil
import io.r2dbc.spi.IsolationLevel.READ_COMMITTED


class NPointServiceImpl(
  val nPointRepository: NPointRepository
) : NPointService {

  override suspend fun getAllNPointPay(
    nPointFilter: NPointFilter
  ): PagedResult<NPointPayModel> = TransactionUtil.withTransaction(READ_COMMITTED, true) {
    nPointRepository.findAll(filter = nPointFilter)
  }

}
