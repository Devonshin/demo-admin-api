package io.allink.receipt.api.domain.login

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.*

/**
 * Package: io.allink.receipt.admin.domain.login
 * Created: Devonshin
 * Date: 14/04/2025
 */

class LoginInfoRepositoryImpl(
  override val table: LoginInfoTable
) : LoginInfoRepository {

  override suspend fun create(model: LoginInfoModel): LoginInfoModel {
    val created = table.insertAndGetId {
      toRow(model)(it)
    }
    model.id = created.value
    return model
  }

  override suspend fun update(model: LoginInfoModel): Int {
    return table.update(
      where = { table.id eq model.id!! },
      body = toUpdateRow(model)
    )
  }

  override suspend fun find(id: UUID): LoginInfoModel? {
    return table.selectAll().where {
      table.id eq id
    }.limit(1).map {
      toModel(it)
    }.firstOrNull()
  }

  override suspend fun delete(id: UUID): Int {
    return table.deleteWhere {
      table.id eq id
    }
  }


}