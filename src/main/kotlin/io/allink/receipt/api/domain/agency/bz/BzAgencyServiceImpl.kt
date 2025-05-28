package io.allink.receipt.api.domain.agency.bz

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.admin.AdminModel
import io.allink.receipt.api.domain.admin.AdminRepository
import io.allink.receipt.api.domain.admin.BzAgencyMasterRole
import io.allink.receipt.api.util.DateUtil.Companion.nowLocalDateTime
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.Logger
import java.time.LocalDateTime
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.agency
 * Created: Devonshin
 * Date: 22/05/2025
 */

class BzAgencyServiceImpl(
  private val bzAgencyRepository: BzAgencyRepository,
  private val adminRepository: AdminRepository,
) : BzAgencyService {

  val logger: Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.name)
  override suspend fun getAgencies(filter: BzAgencyFilter): PagedResult<BzListAgencyModel> {
    return bzAgencyRepository.findAllByFilter(filter)
  }

  override suspend fun getAgency(agencyId: String): BzAgencyModel {
    if (agencyId.isEmpty()) throw BadRequestException("No agency id provided")
    try {
      val uuid = UUID.fromString(agencyId)
      return bzAgencyRepository.find(uuid) ?: throw NotFoundException("에이전시를 찾을 수 없음 [$agencyId]")
    } catch (e: IllegalArgumentException) {
      throw BadRequestException("Invalid agency id format: $agencyId")
    }
  }

  override suspend fun updateAgency(agency: BzAgencyModel, userUuid: String): BzAgencyModel? {
    return modifyAgency(agency, userUuid)
  }

  override suspend fun createdAgency(userUuid: String): UUID {
    val created = bzAgencyRepository.create(
      BzAgencyModel(
        id = UUID.randomUUID(),
        status = AgencyStatus.PENDING,
        regBy = UUID.fromString(userUuid),
        regDate = LocalDateTime.now(),
      )
    )
    return created.id!!
  }

  suspend fun manageStaffs(
    staffs: List<BzAgencyAdminModel>?,
    agencyUuid: UUID,
    userUuid: UUID
  ): List<BzAgencyAdminModel> {
    val agencyUsers = adminRepository.findAllByAgencyId(agencyUuid)
    val usersToDelete = agencyUsers.filterNot { agencyUser ->
      staffs?.any { it.id == agencyUser.id } ?: false
    }
    usersToDelete.forEach {
      adminRepository.delete(it.id!!)
    }
    val now = nowLocalDateTime()
    val afterStaffs = mutableListOf<BzAgencyAdminModel>()
    staffs?.forEach {
      if (it.id == null) {
        val created = adminRepository.create(
          AdminModel(
            id = UUID.randomUUID(),
            fullName = it.fullName,
            phone = it.phone,
            email = it.email,
            status = it.status,
            agencyUuid = agencyUuid,
            regDate = now,
            regBy = userUuid,
            role = BzAgencyMasterRole(),
          )
        )
        afterStaffs.add(
          it.copy(id = created.id!!, regDate = now, regBy = userUuid)
        )
      } else {

        adminRepository.update(
          AdminModel(
            id = it.id,
            fullName = it.fullName,
            phone = it.phone,
            email = it.email,
            status = it.status,
            agencyUuid = agencyUuid,
            modDate = now,
            modBy = userUuid,
            role = BzAgencyMasterRole(),
          )
        )
        afterStaffs.add(
          it.copy(modDate = now, modBy = userUuid,)
        )
      }
    }
    return afterStaffs
  }

  override suspend fun modifyAgency(agency: BzAgencyModel, userUuid: String): BzAgencyModel? {
    val bzAgency = bzAgencyRepository.find(agency.id!!)
    bzAgency ?: throw NotFoundException("등록 진행 중인 에이전시를 찾을 수 없음 [${agency.id}]")
    var agencyToModify: BzAgencyModel? = null
    newSuspendedTransaction {
      agencyToModify = agency.copy(
        modDate = LocalDateTime.now(),
        modBy = UUID.fromString(userUuid),
        staffs = manageStaffs(agency.staffs, bzAgency.id!!, UUID.fromString(userUuid)),
        regBy = bzAgency.regBy,
        regDate = bzAgency.regDate,
      )
      bzAgencyRepository.update(agencyToModify)
    }
    return agencyToModify
  }


}
