package io.allink.receipt.api.domain.store

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.merchant.MerchantGroupTable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */

class StoreRepositoryImpl(
  override val table: StoreTable
) : StoreRepository {

  override suspend fun findAll(filter: StoreFilter): PagedResult<StoreModel> {
    val offset = filter.page.page.minus(1).times(filter.page.pageSize)
    val select = table.selectAll()

    filter.name?.let { name ->
      select.andWhere { table.storeName like "$name%" }
    }
    filter.franchiseCode?.let {
      select.andWhere { table.franchiseCode eq it }
    }
    filter.id?.let {
      select.andWhere { table.id eq it }
    }
    filter.businessNo?.let {
      select.andWhere { table.businessNo eq it }
    }
    filter.period.let {
      it.from.let { from ->
        select.andWhere { table.regDate greaterEq from }
      }
      it.to.let { to ->
        select.andWhere { table.regDate lessEq to }
      }
    }
    columnSort(select, filter.sort, columnConvert)
    val totalCount = select.count().toInt()
    val items = select.limit(filter.page.pageSize)
      .offset(offset.toLong())
      .map { row: ResultRow -> toModel(row) }
      .toList()

    return PagedResult(
      items = items,
      currentPage = filter.page.page,
      totalCount = totalCount,
      totalPages = (totalCount + filter.page.pageSize - 1) / filter.page.pageSize
    )
  }

  override suspend fun searchStores(filter: StoreSearchFilter): PagedResult<StoreSearchModel> {

    val offset = filter.page.page.minus(1).times(filter.page.pageSize)
    val select = table
      .join(MerchantGroupTable, JoinType.LEFT, table.franchiseCode, MerchantGroupTable.id)
      .select(
        table.id,
        table.storeName,
        table.franchiseCode,
        table.ceoName,
        table.tel,
        table.businessNo,
        table.businessType,
        table.eventType,
        MerchantGroupTable.id,
        MerchantGroupTable.receiptType
      )

    filter.name?.let { name ->
      select.andWhere { table.storeName like "$name%" }
    }
    filter.franchiseCode?.let {
      select.andWhere { table.franchiseCode eq it }
    }
    filter.id?.let {
      select.andWhere { table.id eq it }
    }
    filter.businessNo?.let {
      select.andWhere { table.businessNo eq it }
    }
    columnSort(select, filter.sort, columnConvert)
    val totalCount = select.count().toInt()
    val items = select.limit(filter.page.pageSize)
      .offset(offset.toLong())
      .map { row: ResultRow -> toSearchModel(row) }
      .toList()

    return PagedResult(
      items = items,
      currentPage = filter.page.page,
      totalCount = totalCount,
      totalPages = (totalCount + filter.page.pageSize - 1) / filter.page.pageSize
    )
  }

  fun toSearchModel(row: ResultRow): StoreSearchModel {
    val receiptType = row[MerchantGroupTable.receiptType]
    val deviceType = if (receiptType == "MERCHANT_RECEIPT") {
      "CAT"
    } else {
      "OKPOS"
    }
    return StoreSearchModel(
      id = row[table.id],
      storeName = row[table.storeName],
      franchiseCode = row[table.franchiseCode],
      businessNo = row[table.businessNo],
      ceoName = row[table.ceoName],
      tel = row[table.tel],
      businessType = row[table.businessType],
      eventType = row[table.eventType],
      deviceType = deviceType
    )
  }

}