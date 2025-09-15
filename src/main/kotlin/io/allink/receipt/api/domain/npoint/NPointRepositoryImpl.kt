package io.allink.receipt.api.domain.npoint

import io.allink.receipt.api.common.Constant.Companion.AES256_KEY
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.store.StoreTable
import io.allink.receipt.api.domain.user.UserTable
import io.allink.receipt.api.util.AES256Util
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.select

/**
 * Package: io.allink.receipt.api.domain.point
 * Created: Devonshin
 * Date: 20/05/2025
 */

class NPointRepositoryImpl(override val table: NPointWaitingTable) : NPointRepository {

  override suspend fun findAll(filter: NPointFilter): PagedResult<NPointPayModel> {

    val offset = filter.page.page.minus(1).times(filter.page.pageSize)
    val select = table
      .join(NPointUserReviewTable, JoinType.LEFT, table.receiptUuid, NPointUserReviewTable.id)
      .join(StoreTable, JoinType.LEFT, NPointUserReviewTable.storeUid, StoreTable.id)
      .join(UserTable, JoinType.LEFT, NPointUserReviewTable.userUuid, UserTable.id)
      .join(UserEventPointTable, JoinType.LEFT, table.id, UserEventPointTable.id)
      .join(NPointTxHistoryTable, JoinType.LEFT, table.id, NPointTxHistoryTable.id)
      .select(
        table.id.alias("waiting_id"),
        table.receiptUuid,
        table.provideCase,
        table.regDate,
        NPointUserReviewTable.id.alias("review_id"),
        NPointUserReviewTable.points,
        StoreTable.id.alias("store_id"),
        StoreTable.storeName,
        StoreTable.businessNo,
        StoreTable.franchiseCode,
        StoreTable.ceoName,
        UserTable.id.alias("user_id"),
        UserTable.name,
        UserTable.nickname,
        UserTable.phone,
        UserTable.gender,
        UserTable.birthday,
        UserEventPointTable.points,
        UserEventPointTable.id.alias("event_id"),
        NPointTxHistoryTable.id.alias("history_id"),
        NPointTxHistoryTable.txNo,
        NPointTxHistoryTable.resultCode,
        NPointTxHistoryTable.regDate
      )

    filter.period.let {
      it.from.let { from ->
        select.andWhere { table.regDate greaterEq from }
      }
      it.to.let { to ->
        select.andWhere { table.regDate lessEq to }
      }
    }

    filter.storeId?.let {
      select.andWhere { StoreTable.id eq it }
    }
    filter.storeName?.let {
      select.andWhere {
        StoreTable.storeName like "$it%"
      }
    }
    filter.franchiseCode?.let {
      select.andWhere { StoreTable.franchiseCode eq it }
    }
    filter.businessNo?.let {
      select.andWhere { StoreTable.businessNo eq it }
    }
    filter.phone?.let {
      select.andWhere { UserTable.phone eq AES256Util.encrypt(it, AES256_KEY) }
    }
    filter.userId?.let {
      select.andWhere { UserTable.id eq it }
    }
    filter.userName?.let {
      select.andWhere { UserTable.name like "$it%" }
    }
    filter.userNickName?.let {
      select.andWhere { UserTable.nickname eq AES256Util.encrypt(it, AES256_KEY) }
    }

    columnSort(select, filter.sort, columnConvert)

    val totalCount = select.count().toInt()
    val items = select.limit(filter.page.pageSize)
      .offset(offset.toLong())
      .toList()
      .map { toModel(row = it) }

    return PagedResult(
      items = items,
      currentPage = filter.page.page,
      totalCount = totalCount,
      totalPages = (totalCount + filter.page.pageSize - 1) / filter.page.pageSize
    )
  }
}