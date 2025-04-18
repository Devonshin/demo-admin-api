package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.common.Constant.Companion.AES256_KEY
import io.allink.receipt.api.common.PagedResult
import io.allink.receipt.api.domain.store.StoreTable
import io.allink.receipt.api.domain.user.UserTable
import io.allink.receipt.api.util.AES256Util
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.andWhere

/**
 * Package: io.allink.receipt.api.domain.receipt
 * Created: Devonshin
 * Date: 18/04/2025
 */

class IssueReceiptRepositoryImpl(
  override val table: IssueReceiptTable
) : IssueReceiptRepository {
  override suspend fun findAll(filter: ReceiptFilter): PagedResult<SimpleIssueReceiptModel> = query {

    val offset = filter.page.page.minus(1).times(filter.page.pageSize)
    val select = table
      /*.join(
        MerchantTagTable,
        JoinType.INNER,
        table.tagId,
        MerchantTagTable.id
      )*/
      .join(
        StoreTable,
        JoinType.INNER,
        table.storeUid,
        StoreTable.id
      )
      .join(
        UserTable,
        JoinType.INNER,
        table.userUid,
        UserTable.id
      )
      .select(
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
        UserTable.id,
        UserTable.name
      )

    filter.storeName?.let {
      select.andWhere { StoreTable.storeName like "it%" }
    }
    filter.franchiseCode?.let {
      select.andWhere { StoreTable.franchiseCode eq it }
    }
    filter.phone?.let {
      select.andWhere { UserTable.phone eq AES256Util.encrypt(it, AES256_KEY) }
    }
    filter.businessNo?.let {
      select.andWhere { StoreTable.businessNo eq it }
    }
    filter.tagUid?.let {
      select.andWhere { table.tagId eq it }
    }
    filter.userId?.let {
      select.andWhere { table.userUid eq it }
    }
    filter.userName?.let {
      select.andWhere { UserTable.name like "$it%" }
    }
    filter.userNickName?.let {
      select.andWhere { UserTable.nickname eq AES256Util.encrypt(it, AES256_KEY) }
    }
    filter.storeId?.let {
      select.andWhere { StoreTable.id eq it }
    }
    filter.period.let {
      it.from.let { from ->
        select.andWhere { table.issueDate greaterEq from }
      }
      it.to.let { to ->
        select.andWhere { table.issueDate lessEq to }
      }
    }

    columnSort(select, filter.sort, columnConvert)
    val totalCount = select.count().toInt()
    val items = select.limit(filter.page.pageSize)
      .offset(offset.toLong())
      .toList()
      .map { toSimpleModel(it) }

    return@query PagedResult(
      items = items,
      currentPage = filter.page.page,
      totalCount = totalCount,
      totalPages = (totalCount + filter.page.pageSize - 1) / filter.page.pageSize
    )
  }
}