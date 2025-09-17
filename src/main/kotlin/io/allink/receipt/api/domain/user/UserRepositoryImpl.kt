package io.allink.receipt.api.domain.user

import io.allink.receipt.api.common.Constant.Companion.AES256_KEY
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.util.AES256Util
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.neq
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.selectAll

/**
 * Package: io.allink.receipt.admin.domain.user
 * Created: Devonshin
 * Date: 15/04/2025
 */

class UserRepositoryImpl(
  override val table: UserTable
) : UserRepository {
  override suspend fun findAll(filter: UserFilter): PagedResult<UserModel> {
    val offset = filter.page.page.minus(1).times(filter.page.pageSize)
    val select = table.selectAll()

    filter.name?.let { name ->
      select.andWhere { table.name like "$name%" }
    }
    filter.nickName?.let {
      select.andWhere { table.nickname eq AES256Util.encrypt(it, AES256_KEY) }
    }
    filter.phone?.let {
      select.andWhere { table.phone eq AES256Util.encrypt(it, AES256_KEY) }
    }
    filter.gender?.let {
      select.andWhere { table.gender eq it }
    }
    filter.age?.let {
      it.from.let { from ->
        select.andWhere { table.birthday greaterEq from.toString() }
      }
      it.to.let { to ->
        select.andWhere { table.birthday lessEq to.toString() }
      }
    }
    select.andWhere { table.role eq UserRole.USER }
    columnSort(select, filter.sort, columnConvert)
    val totalCount = select.count().toInt()
    val items = select.limit(filter.page.pageSize)
      .offset(offset.toLong())
      .toList()
      .map { toModel(it) }
    return PagedResult(
      items = items,
      totalCount = totalCount,
      currentPage = filter.page.page,
      totalPages = (totalCount + filter.page.pageSize - 1) / filter.page.pageSize
    )
  }

  override suspend fun create(model: UserModel): UserModel {
    TODO("Not yet implemented")
  }

  override suspend fun update(model: UserModel): Int {
    TODO("Not yet implemented")
  }

  override suspend fun delete(id: String): Int {
    TODO("Not yet implemented")
  }

}