package io.allink.receipt.api.domain.admin

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import java.util.*

/**
 * Package: io.allink.receipt.admin.domain.admin
 * Created: Devonshin
 * Date: 13/04/2025
 */

class AdminRepositoryImpl(
  override val table: AdminTable
) : AdminRepository {

  override suspend fun create(model: AdminModel): AdminModel = query {
    val created = table.insertAndGetId(toRow(model))
    model.id = created.value
    model
  }

  override suspend fun update(model: AdminModel): Int = query {
    table.update(
      where = { table.id eq model.id!! },
      body = toUpdateRow(model)
    )
  }

  override suspend fun findAllByAgencyId(agencyUuid: UUID): List<AdminModel> {
    return query {
      table.selectAll().where {
        table.agencyUuid eq agencyUuid
      }.map {
        toModel(it)
      }
    }
  }

  override suspend fun findByPhone(phone: String): AdminModel? = query {
    table.selectAll().where {
      table.phone eq phone
    }.limit(1).map {
      toModel(it)
    }.firstOrNull()
  }

  override suspend fun findByUserUuid(uUID: UUID): AdminModel? = query {
    table.selectAll().where {
      table.id eq uUID
    }.limit(1).map {
      toModel(it)
    }.firstOrNull()
  }

  override suspend fun find(id: UUID): AdminModel? = query {
    table.selectAll().where {
      table.id eq id
    }.limit(1).map {
      toModel(it)
    }.firstOrNull()
  }

  override suspend fun delete(id: UUID): Int = deleteQuery {
    table.deleteWhere {
      table.id eq id
    }
  }


}