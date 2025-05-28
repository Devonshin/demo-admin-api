package io.allink.receipt.api.domain.file

import io.allink.receipt.api.domain.Sorter
import io.allink.receipt.api.domain.agency.bz.AgencyStatus
import io.allink.receipt.api.domain.agency.bz.BzAgencyFilter
import io.github.smiley4.ktoropenapi.config.SimpleBodyConfig
import java.util.*

/**
 * Package: io.allink.receipt.api.common
 * Created: Devonshin
 * Date: 22/05/2025
 */

fun agencyListRequest(): SimpleBodyConfig.() -> Unit = {
  description = "대리점 목록 조회 요청"
  required = true
  example("agency-list-request") {
    value = BzAgencyFilter(
      id = "c7f0d23e-eceb-4434-b489-668c0b61a7f9",
      businessNo = "123-45-67890",
      agencyName = "대리점명",
      status = AgencyStatus.ACTIVE,
      sort = listOf(
        Sorter("agencyName", "ASC")
      )
    )
  }
}

