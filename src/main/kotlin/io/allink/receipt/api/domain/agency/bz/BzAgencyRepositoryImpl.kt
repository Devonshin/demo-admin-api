package io.allink.receipt.api.domain.agency.bz

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.admin.AdminTable
import io.allink.receipt.api.domain.login.LoginInfoTable
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.*
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.agency
 * Created: Devonshin
 * Date: 22/05/2025
 */

class BzAgencyRepositoryImpl(
  override val table: BzAgencyTable
) : BzAgencyRepository {

  override suspend fun findAllByFilter(filter: BzAgencyFilter): PagedResult<BzListAgencyModel> = query {
    val offset = filter.page.page.minus(1) * filter.page.pageSize
    val latestLoginAlias = LoginInfoTable.loginDate.max().alias("latestLogin")

    val select = table
      .join(AdminTable, JoinType.LEFT, table.id, AdminTable.agencyUuid)
      .join(LoginInfoTable, JoinType.LEFT, AdminTable.id, LoginInfoTable.userUuid)
      .select(
        table.id,
        table.agencyName,
        table.businessNo,
        table.status,
        latestLoginAlias
      ).groupBy(
        table.id,
        table.agencyName,
        table.businessNo,
        table.status,
      )

    filter.id?.let {
      try {
        select.andWhere {
          table.id eq UUID.fromString(it)
        }
      } catch (e: IllegalArgumentException) {
        throw BadRequestException("agency id is not a UUID: $it")
      }
    }
    filter.agencyName?.let { select.andWhere { table.agencyName like "$it%" } }
    filter.businessNo?.let { select.andWhere { table.businessNo eq it } }
    filter.status?.let { select.andWhere { table.status eq it } }

    columnSort(select, filter.sort, columnConvert)
    val totalCount = select.count().toInt()
    val items = select.limit(filter.page.pageSize)
      .offset(offset.toLong())
      .toList()
      .map {
        BzListAgencyModel(
          id = it[table.id].value,
          agencyName = it[table.agencyName],
          businessNo = it[table.businessNo],
          status = it[table.status],
          latestLoginAt = it[latestLoginAlias]
        )
      }

    return@query PagedResult(
      items = items,
      currentPage = filter.page.page,
      totalCount = totalCount,
      totalPages = (totalCount + filter.page.pageSize - 1) / filter.page.pageSize
    )
  }

  override val columnConvert: (String?) -> Expression<out Any?>?
    get() = { column ->
      val latestLoginAlias = LoginInfoTable.loginDate.max().alias("latestLogin")
      when (column) {
        "id" -> table.id
        "agencyName" -> table.agencyName
        "businessNo" -> table.businessNo
        "status" -> table.status
        "latestLoginAt" -> latestLoginAlias
        else -> null
      }
    }
}