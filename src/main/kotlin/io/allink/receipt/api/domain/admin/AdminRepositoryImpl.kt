package io.allink.receipt.api.domain.admin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.update
import java.util.*

/**
 * Package: io.allink.receipt.admin.domain.admin
 * Created: Devonshin
 * Date: 13/04/2025
 */

class AdminRepositoryImpl(
  override val table: AdminTable
) : AdminRepository {

  override suspend fun create(model: AdminModel): AdminModel {
    val created = table.insertAndGetId(toRow(model))
    model.id = created.value
    return model
  }

  override suspend fun update(model: AdminModel): Int {
    return table.update(
      where = { table.id eq model.id!! },
      body = toUpdateRow(model)
    )
  }

  override suspend fun findAllByAgencyId(agencyUuid: UUID): Flow<AdminModel> {
    return table.selectAll().where {
      table.agencyUuid eq agencyUuid
    }.map {
      toModel(it)
    }
  }

  override suspend fun findByPhone(phone: String): AdminModel? {
    return table.selectAll().where {
      table.phone eq phone
    }.limit(1).map {
      toModel(it)
    }.firstOrNull()
  }

  override suspend fun findByUserUuid(uUID: UUID): AdminModel? {
    return table.selectAll().where {
      table.id eq uUID
    }.limit(1).map {
      toModel(it)
    }.firstOrNull()
  }

  override suspend fun find(id: UUID): AdminModel? {
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