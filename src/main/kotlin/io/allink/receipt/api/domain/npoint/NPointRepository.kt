package io.allink.receipt.api.domain.npoint

import io.allink.receipt.api.common.Constant.Companion.AES256_KEY
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.store.StoreTable
import io.allink.receipt.api.domain.user.UserTable
import io.allink.receipt.api.repository.ExposedRepository
import io.allink.receipt.api.util.AES256Util
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import kotlin.math.max

/**
 * Package: io.allink.receipt.api.domain.point
 * Created: Devonshin
 * Date: 20/05/2025
 */

interface NPointRepository : ExposedRepository<NPointWaitingTable, Long, NPointPayModel> {

  suspend fun findAll(filter: NPointFilter): PagedResult<NPointPayModel>

  override fun toModel(row: ResultRow): NPointPayModel {

    val points = calculatePoints(row)
    val txHistory = buildTransactionHistory(row)
    val txResult = calculateTxResult(txHistory)

    return NPointPayModel(
      id = row[table.id],
      point = points,
      status = txResult,
      user = NPointUserModel(
        id = row[UserTable.id],
        name = row[UserTable.name],
        phone = AES256Util.decrypt(row[UserTable.phone], AES256_KEY),
        gender = row[UserTable.gender],
        birthday = row[UserTable.birthday],
        nickname = AES256Util.decrypt(row[UserTable.nickname], AES256_KEY)
      ),
      store = NPointStoreModel(
        id = row[StoreTable.id],
        storeName = row[StoreTable.storeName],
        franchiseCode = row[StoreTable.franchiseCode],
        businessNo = row[StoreTable.businessNo]
      ),
      provideCase = row[table.provideCase].desc,
      pointTrNo = txHistory?.txNo,
      pointPayNo = txHistory?.id.takeIf { it != null }?.toString(),
      regDate = txHistory?.regDate
    )
  }

  private fun calculatePoints(row: ResultRow): Int? {
    return if (row[table.provideCase] == PointProvideCase.EVENT) {
      row[UserEventPointTable.points]
    } else {
      row[NPointUserReviewTable.points]
    }
  }

  private fun buildTransactionHistory(row: ResultRow): NPointTxtHistoryModel? {
    return if (row[NPointTxHistoryTable.id] != null) {
      NPointTxtHistoryModel(
        txNo = row[NPointTxHistoryTable.txNo],
        regDate = row[NPointTxHistoryTable.regDate],
        resultCode = row[NPointTxHistoryTable.resultCode],
        id = row[NPointTxHistoryTable.id]
      )
    } else {
      null
    }
  }

  private fun calculateTxResult(transactionHistory: NPointTxtHistoryModel?): String {
    return when {
      transactionHistory == null -> "지급 대기중"
      transactionHistory.resultCode == "OK" -> "지급완료"
      else -> "지급실패[${transactionHistory.resultCode}]"
    }
  }


  override fun toRow(model: NPointPayModel): NPointWaitingTable.(InsertStatement<EntityID<Long>>) -> Unit {
    TODO("Not yet implemented")
  }

  override fun toUpdateRow(model: NPointPayModel): NPointWaitingTable.(UpdateStatement) -> Unit {
    TODO("Not yet implemented")
  }

  override suspend fun create(model: NPointPayModel): NPointPayModel {
    TODO("Not yet implemented")
  }

  override suspend fun update(model: NPointPayModel): Int {
    TODO("Not yet implemented")
  }

  override suspend fun find(id: Long): NPointPayModel? {
    TODO("Not yet implemented")
  }

  override suspend fun delete(id: Long): Int {
    TODO("Not yet implemented")
  }

  override val columnConvert: (String?) -> Column<out Any?>?
    get() = { column ->
      if (column == null) null
      else when (column) {
        "phone" -> UserTable.phone
        "userId" -> UserTable.id
        "userName" -> UserTable.name
        "userNickName" -> UserTable.nickname
        "storeId" -> StoreTable.id
        "storeBusinessNo" -> StoreTable.businessNo
        "storeName" -> StoreTable.storeName
        "franchiseCode" -> StoreTable.franchiseCode
        else -> null
      }
    }
}