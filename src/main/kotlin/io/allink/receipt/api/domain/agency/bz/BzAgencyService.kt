package io.allink.receipt.api.domain.agency.bz

import io.allink.receipt.api.domain.PagedResult
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.agency
 * Created: Devonshin
 * Date: 22/05/2025
 */

interface BzAgencyService {

  suspend fun getAgencies(filter: BzAgencyFilter): PagedResult<SimpleBzAgencyModel>

  suspend fun getAgency(agencyId: String): BzAgencyModel

  suspend fun updateAgency(agency: BzAgencyModel, userUuid: String): BzAgencyModel?

  suspend fun modifyAgency(agency: BzAgencyModel, userUuid: String): BzAgencyModel?

  suspend fun createdAgency(userUuid: String): UUID
}
