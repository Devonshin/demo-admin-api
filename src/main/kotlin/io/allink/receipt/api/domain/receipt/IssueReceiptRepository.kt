package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.common.ExposedRepository
import io.allink.receipt.api.common.PagedResult
import io.allink.receipt.api.domain.advertisement.AdvertisementTable
import io.allink.receipt.api.domain.advertisement.SimpleAdvertisementModel
import io.allink.receipt.api.domain.merchant.MerchantTagTable
import io.allink.receipt.api.domain.merchant.SimpleMerchantTagModel
import io.allink.receipt.api.domain.store.SimpleStoreModel
import io.allink.receipt.api.domain.store.StoreTable
import io.allink.receipt.api.domain.user.SimpleUserModel
import io.allink.receipt.api.domain.user.UserTable
import io.allink.receipt.api.domain.user.review.SimpleUserPointReviewModel
import io.allink.receipt.api.domain.user.review.UserPointReviewTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

/**
 * Package: io.allink.receipt.api.domain.receipt
 * Created: Devonshin
 * Date: 18/04/2025
 */

interface IssueReceiptRepository : ExposedRepository<IssueReceiptTable, String, IssueReceiptModel> {

  override suspend fun delete(id: String): Int {
    TODO("Not yet implemented")
  }

  suspend fun findByIdAndUserId(userId: String, receiptId: String): IssueReceiptModel?

  override suspend fun find(id: String): IssueReceiptModel? {
    return null
  }

  override suspend fun update(model: IssueReceiptModel): Int {
    TODO("Not yet implemented")
  }

  override suspend fun create(model: IssueReceiptModel): IssueReceiptModel {
    TODO("Not yet implemented")
  }

  suspend fun findAll(filter: ReceiptFilter): PagedResult<SimpleIssueReceiptModel>

  override fun toUpdateRow(model: IssueReceiptModel): IssueReceiptTable.(UpdateStatement) -> Unit = {
    it[storeUid] = model.store?.id
    it[tagId] = model.tag?.id
    it[issueDate] = model.issueDate
    it[userUid] = model.user?.id
    it[receiptType] = model.receiptType
    it[receiptAmount] = model.receiptAmount
    it[originIssueId] = model.originIssueId
    it[advertisementId] = model.advertisement?.id
  }

  override fun toRow(model: IssueReceiptModel): IssueReceiptTable.(InsertStatement<EntityID<String>>) -> Unit = {
    it[storeUid] = model.store?.id
    it[tagId] = model.tag?.id
    it[issueDate] = model.issueDate
    it[userUid] = model.user?.id
    it[receiptType] = model.receiptType
    it[receiptAmount] = model.receiptAmount
    it[originIssueId] = model.originIssueId
    it[advertisementId] = model.advertisement?.id
  }

  override fun toModel(row: ResultRow): IssueReceiptModel {
    return IssueReceiptModel(
      id = row[table.id],
      store = SimpleStoreModel(
        id = row[StoreTable.id],
        storeName = row[StoreTable.storeName],
        businessNo = row[StoreTable.businessNo],
        franchiseCode = row[StoreTable.franchiseCode],
        ceoName = row[StoreTable.ceoName]
      ),
      tag = SimpleMerchantTagModel(
        id = row[MerchantTagTable.id],
        deviceId = row[MerchantTagTable.deviceId],
      ),
      user = SimpleUserModel(
        id = row[UserTable.id],
        name = row[UserTable.name],
      ),
      issueDate = row[table.issueDate],
      receiptType = row[table.receiptType],
      receiptAmount = row[table.receiptAmount],
      originIssueId = row[table.originIssueId],
      advertisement = SimpleAdvertisementModel(
        id = row[AdvertisementTable.id]?.value,
        merchantGroupId = row[AdvertisementTable.merchantGroupId],
        title = row[AdvertisementTable.title],
      ),
      userPointReview = SimpleUserPointReviewModel(
        id = row[UserPointReviewTable.id],
        status = row[UserPointReviewTable.status],
      ),
      edoc = null
    )
  }

  fun toSimpleModel(row: ResultRow): SimpleIssueReceiptModel {
    return SimpleIssueReceiptModel(
      id = row[table.id],
      store = SimpleStoreModel(
        id = row[StoreTable.id],
        storeName = row[StoreTable.storeName],
        businessNo = row[StoreTable.businessNo],
        franchiseCode = row[StoreTable.franchiseCode],
        ceoName = row[StoreTable.ceoName]
      ),
      tagId = row[table.tagId],
      user = SimpleUserModel(
        id = row[UserTable.id],
        name = row[UserTable.name],
      ),
      issueDate = row[table.issueDate],
      receiptType = row[table.receiptType],
      receiptAmount = row[table.receiptAmount],
      originIssueId = row[table.originIssueId]
    )
  }

  override val columnConvert: (String?) -> Column<out Any?>?
    get() = { column ->
      if (column == null) null
      else when (column) {
        "phone" -> UserTable.phone
        "userId" -> UserTable.id
        "userName" -> UserTable.name
        "userNickName" -> UserTable.nickname
        "tagUid" -> MerchantTagTable.id
        "storeId" -> StoreTable.id
        "storeBusinessNo" -> StoreTable.businessNo
        "storeName" -> StoreTable.storeName
        "franchiseCode" -> StoreTable.franchiseCode
        else -> null
      }
    }
}