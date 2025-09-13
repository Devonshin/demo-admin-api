package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.common.Constant.Companion.AES256_KEY
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.advertisement.AdvertisementTable
import io.allink.receipt.api.domain.merchant.MerchantTagTable
import io.allink.receipt.api.domain.store.SimpleStoreModel
import io.allink.receipt.api.domain.store.StoreTable
import io.allink.receipt.api.domain.user.UserTable
import io.allink.receipt.api.domain.user.review.UserPointReviewTable
import io.allink.receipt.api.repository.ExposedRepository
import io.allink.receipt.api.util.AES256Util
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ColumnSet
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.r2dbc.Query
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.select

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

  suspend fun findAll(filter: ReceiptFilter): PagedResult<SimpleIssueReceiptModel> {

    val offset = filter.page.page.minus(1).times(filter.page.pageSize)

    // Item 조회를 위한 기본 JOIN (항상 필요)
    val selectFrom = table
      .join(
        StoreTable,
        JoinType.LEFT,
        table.storeUid,
        StoreTable.id
      )
      .join(
        UserTable,
        JoinType.LEFT,
        table.userUid,
        UserTable.id
      )

    // Count는 가능한 단순하게: 필요한 경우에만 JOIN 추가
    var countFrom: ColumnSet = table
    val needsStoreJoin =
      listOf(filter.storeName, filter.franchiseCode, filter.businessNo, filter.storeId).any { it != null }
    val needsUserJoin = listOf(filter.phone, filter.userId, filter.userName, filter.userNickName).any { it != null }
    if (needsStoreJoin) {
      countFrom = countFrom.join(StoreTable, JoinType.LEFT, table.storeUid, StoreTable.id)
    }
    if (needsUserJoin) {
      countFrom = countFrom.join(UserTable, JoinType.LEFT, table.userUid, UserTable.id)
    }

    // Count 전용 쿼리(선택 컬럼 최소화)
    val countQuery = countFrom.select(table.id)
    // Item 전용 쿼리(필요 컬럼 선택)
    val select = selectFrom.select(
      table.id,
      table.storeUid,
      table.tagId,
      table.userUid,
      table.receiptType,
      table.receiptAmount,
      table.issueDate,
      table.originIssueId,
      StoreTable.id,
      StoreTable.storeName,
      StoreTable.franchiseCode,
      StoreTable.businessNo,
      StoreTable.ceoName,
      UserTable.id,
      UserTable.name
    )

    // 동일 필터를 두 쿼리에 적용
    applyFilters(filter, select, countQuery)

    // 먼저 총 개수 계산 (정렬 적용 전에 실행하여 문법 오류 회피)
    val totalCount = countQuery.count().toInt()

    // 이후 정렬 적용 및 페이징 아이템 조회
    columnSort(select, filter.sort, columnConvert)

    val items = if (offset >= totalCount) {
      emptyList()
    } else {
      select.limit(filter.page.pageSize)
        .offset(offset.toLong())
        .toList()
        .map { toSimpleModel(it) }
    }

    return PagedResult(
      items = items,
      currentPage = filter.page.page,
      totalCount = totalCount,
      totalPages = (totalCount + filter.page.pageSize - 1) / filter.page.pageSize
    )
  }

  fun applyFilters(filter: ReceiptFilter, select: Query, countQuery: Query) {
    filter.storeName?.let {
      select.andWhere { StoreTable.storeName like "$it%" }; countQuery.andWhere { StoreTable.storeName like "$it%" }
    }
    filter.franchiseCode?.let {
      select.andWhere { StoreTable.franchiseCode eq it }; countQuery.andWhere { StoreTable.franchiseCode eq it }
    }
    filter.phone?.let {
      val enc = AES256Util.encrypt(
        it,
        AES256_KEY
      ); select.andWhere { UserTable.phone eq enc }; countQuery.andWhere { UserTable.phone eq enc }
    }
    filter.businessNo?.let {
      select.andWhere { StoreTable.businessNo eq it }; countQuery.andWhere { StoreTable.businessNo eq it }
    }
    filter.tagUid?.let { val v = it; select.andWhere { table.tagId eq v }; countQuery.andWhere { table.tagId eq v } }
    filter.userId?.let {
      select.andWhere { table.userUid eq it }; countQuery.andWhere { table.userUid eq it }
    }
    filter.userName?.let {
      select.andWhere { UserTable.name like "$it%" }; countQuery.andWhere { UserTable.name like "$it%" }
    }
    filter.userNickName?.let {
      val enc = AES256Util.encrypt(
        it,
        AES256_KEY
      ); select.andWhere { UserTable.nickname eq enc }; countQuery.andWhere { UserTable.nickname eq enc }
    }
    filter.storeId?.let {
      select.andWhere { StoreTable.id eq it }; countQuery.andWhere { StoreTable.id eq it }
    }
    filter.period.let {
      it.from.let { from -> select.andWhere { table.issueDate greaterEq from }; countQuery.andWhere { table.issueDate greaterEq from } }
      it.to.let { to -> select.andWhere { table.issueDate lessEq to }; countQuery.andWhere { table.issueDate lessEq to } }
    }
  }

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

  override fun toRow(model: IssueReceiptModel): IssueReceiptTable.(UpdateBuilder<*>) -> Unit = {
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
        ceoName = row[StoreTable.ceoName],
      ),
      tag = SimpleMerchantTagReceiptModel(
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
        "issueDate" -> table.issueDate
        "receiptType" -> table.receiptType
        "receiptAmount" -> table.receiptAmount
        else -> null
      }
    }
}