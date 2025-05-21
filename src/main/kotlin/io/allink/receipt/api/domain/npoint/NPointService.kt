package io.allink.receipt.api.domain.npoint

import io.allink.receipt.api.domain.PagedResult


interface NPointService {

  suspend fun getAllNPointPay(nPointFilter: NPointFilter): PagedResult<NPointPayModel>

}
