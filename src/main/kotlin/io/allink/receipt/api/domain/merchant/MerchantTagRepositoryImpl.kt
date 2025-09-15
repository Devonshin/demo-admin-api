package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.merchant.MerchantTagTable.id
import io.allink.receipt.api.domain.merchant.MerchantTagTable.modDate
import io.allink.receipt.api.domain.merchant.MerchantTagTable.regDate
import io.allink.receipt.api.domain.merchant.MerchantTagTable.storeUid
import io.allink.receipt.api.domain.store.StoreTable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.insert
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.update

/**
 * Package: io.allink.receipt.api.domain.merchant
 * Created: Devonshin
 * Date: 18/04/2025
 */

class MerchantTagRepositoryImpl(
  override val table: MerchantTagTable
) : MerchantTagRepository {

  override suspend fun findAll(filter: MerchantTagFilter): PagedResult<SimpleMerchantTagModel> {

    val offset = filter.page.page.minus(1).times(filter.page.pageSize)
    val select = table
      .join(
        StoreTable,
        JoinType.LEFT,
        table.storeUid,
        StoreTable.id
      )
      .select(
        table.id,
        table.storeUid,
        table.tagName,
        table.regDate,
        table.modDate,
        StoreTable.id,
        StoreTable.storeName,
        StoreTable.franchiseCode,
        StoreTable.businessNo,
        StoreTable.tel,
        StoreTable.regDate,
        StoreTable.modDate,
        StoreTable.deleteDate,
        StoreTable.status,
      )

    filter.id?.let { select.andWhere { table.id eq it } }

    filter.name?.let { select.andWhere { table.tagName like "it%" } }

    filter.storeId?.let { select.andWhere { table.storeUid eq it } }

    filter.businessNo?.let { select.andWhere { StoreTable.businessNo eq it } }

    filter.storeName?.let { select.andWhere { StoreTable.storeName like "it%" } }

    filter.franchiseCode?.let { select.andWhere { StoreTable.franchiseCode eq it } }

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
      .toList()
      .map {
        SimpleMerchantTagModel(
          id = it[id],
          name = it[table.tagName],
          store = SimpleMerchantTagStoreModel(
            id = it[storeUid],
            storeName = it[StoreTable.storeName],
            businessNo = it[StoreTable.businessNo],
            franchiseCode = it[StoreTable.franchiseCode],
            status = it[StoreTable.status]
          ),
          regDate = it[regDate],
          modDate = it[modDate]
        )
      }
    return PagedResult(
      items = items,
      currentPage = filter.page.page,
      totalCount = totalCount,
      totalPages = (totalCount + filter.page.pageSize - 1) / filter.page.pageSize
    )
  }

  override suspend fun findForUpdate(id: String): MerchantTagModel? {
    return table
      .select(
        table.id,
        table.merchantGroupId,
        table.deviceId,
        table.storeUid,
        table.merchantStoreId,
        table.tagName,
        table.regDate,
        table.modDate,
      )
      .where { table.id eq id }
      .map { toUpdateModel(it) }
      .singleOrNull()
  }

  override suspend fun create(model: MerchantTagModel): MerchantTagModel {
    table.insert {
      toRow(model)(it)
    }
    return model
  }

  override suspend fun update(model: MerchantTagModel): Int {
    return table.update(
      where = { table.id eq model.id!! },
      body = toUpdateRow(model)
    )
  }
}