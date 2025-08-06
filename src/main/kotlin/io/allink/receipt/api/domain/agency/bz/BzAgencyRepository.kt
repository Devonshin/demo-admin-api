package io.allink.receipt.api.domain.agency.bz

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.admin.AdminTable
import io.allink.receipt.api.repository.ExposedRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.agency
 * Created: Devonshin
 * Date: 22/05/2025
 */

interface BzAgencyRepository : ExposedRepository<BzAgencyTable, UUID, BzAgencyModel> {

  suspend fun findAllByFilter(filter: BzAgencyFilter): PagedResult<SimpleBzAgencyModel>

  override suspend fun create(model: BzAgencyModel): BzAgencyModel {
    val createdId = table.insertAndGetId {
      toRow(model)(it)
    }
    return model.copy(id = createdId.value)
  }

  override suspend fun update(model: BzAgencyModel): Int {
    return table.update({ table.id eq model.id!! }) {
      toUpdateRow(model)(it)
    }
  }

  override suspend fun find(id: UUID): BzAgencyModel? {

    val staffs = AdminTable
      .selectAll()
      .where {
        AdminTable.agencyUuid eq id
      }
      .map {
        BzAgencyAdminModel(
          id = it[AdminTable.id].value,
          fullName = it[AdminTable.fullName],
          phone = it[AdminTable.phone],
          email = it[AdminTable.email],
          status = it[AdminTable.status],
          regDate = it[AdminTable.regDate],
          modDate = it[AdminTable.modDate],
          modBy = it[AdminTable.modBy],
          regBy = it[AdminTable.regBy]
        )
      }
      .toList()

    return table
      .selectAll()
      .where {
        table.id eq id
      }
      .mapNotNull {
        toModel(it, staffs)
      }
      .singleOrNull()
  }

  override suspend fun delete(id: UUID): Int {
    TODO()
  }

  fun toModel(row: ResultRow, staffs: List<BzAgencyAdminModel>): BzAgencyModel {
    return BzAgencyModel(
      id = row[table.id].value,
      agencyName = row[table.agencyName],
      businessNo = row[table.businessNo],
      addr1 = row[table.addr1],
      addr2 = row[table.addr2],
      tel = row[table.tel],
      ceoName = row[table.ceoName],
      ceoPhone = row[table.ceoPhone],
      staffs = staffs,
      applicationFilePath = row[table.applicationFilePath],
      bzFilePath = row[table.bzFilePath],
      idFilePath = row[table.idFilePath],
      bankFilePath = row[table.bankFilePath],
      isReceiptAlliance = row[table.isReceiptAlliance],
      infraRatio = row[table.infraRatio],
      rewardBaseRatio = row[table.rewardBaseRatio],
      rewardCommissionRatio = row[table.rewardCommissionRatio],
      rewardPackageRatio = row[table.rewardPackageRatio],
      advertisementRatio = row[table.advertisementRatio],
      isCouponAdv = row[table.isCouponAdv],
      couponAdvRatio = row[table.couponAdvRatio],
      tagDeposit = row[table.tagDeposit],
      agencyDeposit = row[table.agencyDeposit],
      settlementBank = row[table.settlementBank],
      bankAccountName = row[table.bankAccountName],
      bankAccountNo = row[table.bankAccountNo],
      status = row[table.status],
      regDate = row[table.regDate],
      regBy = row[table.regBy],
      modDate = row[table.modDate],
      modBy = row[table.modBy]
    )
  }

  override fun toModel(row: ResultRow): BzAgencyModel {
    return toModel(row, listOf())
  }

  override fun toRow(model: BzAgencyModel): BzAgencyTable.(UpdateBuilder<*>) -> Unit = {
    it[agencyName] = model.agencyName
    it[businessNo] = model.businessNo
    it[addr1] = model.addr1
    it[addr2] = model.addr2
    it[tel] = model.tel
    it[ceoName] = model.ceoName
    it[ceoPhone] = model.ceoPhone
    it[applicationFilePath] = model.applicationFilePath
    it[bzFilePath] = model.bzFilePath
    it[idFilePath] = model.idFilePath
    it[bankFilePath] = model.bankFilePath
    it[isReceiptAlliance] = model.isReceiptAlliance
    it[infraRatio] = model.infraRatio
    it[rewardBaseRatio] = model.rewardBaseRatio
    it[rewardCommissionRatio] = model.rewardCommissionRatio
    it[rewardPackageRatio] = model.rewardPackageRatio
    it[advertisementRatio] = model.advertisementRatio
    it[isCouponAdv] = model.isCouponAdv
    it[couponAdvRatio] = model.couponAdvRatio
    it[tagDeposit] = model.tagDeposit
    it[agencyDeposit] = model.agencyDeposit
    it[settlementBank] = model.settlementBank
    it[bankAccountName] = model.bankAccountName
    it[bankAccountNo] = model.bankAccountNo
    it[status] = model.status!!
    it[regDate] = model.regDate!!
    it[regBy] = model.regBy!!
  }

  override fun toUpdateRow(model: BzAgencyModel): BzAgencyTable.(UpdateStatement) -> Unit = {
    it[agencyName] = model.agencyName
    it[businessNo] = model.businessNo
    it[addr1] = model.addr1
    it[addr2] = model.addr2
    it[tel] = model.tel
    it[ceoName] = model.ceoName
    it[ceoPhone] = model.ceoPhone
    it[applicationFilePath] = model.applicationFilePath
    it[bzFilePath] = model.bzFilePath
    it[idFilePath] = model.idFilePath
    it[bankFilePath] = model.bankFilePath
    it[isReceiptAlliance] = model.isReceiptAlliance
    it[infraRatio] = model.infraRatio
    it[rewardBaseRatio] = model.rewardBaseRatio
    it[rewardCommissionRatio] = model.rewardCommissionRatio
    it[rewardPackageRatio] = model.rewardPackageRatio
    it[advertisementRatio] = model.advertisementRatio
    it[isCouponAdv] = model.isCouponAdv
    it[couponAdvRatio] = model.couponAdvRatio
    it[tagDeposit] = model.tagDeposit
    it[agencyDeposit] = model.agencyDeposit
    it[settlementBank] = model.settlementBank
    it[bankAccountName] = model.bankAccountName
    it[bankAccountNo] = model.bankAccountNo
    it[status] = model.status!!
    it[modDate] = model.modDate
    it[modBy] = model.modBy
  }

}