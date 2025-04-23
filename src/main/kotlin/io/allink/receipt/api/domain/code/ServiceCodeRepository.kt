package io.allink.receipt.api.domain.code

import io.allink.receipt.api.repository.ExposedRepository
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

interface ServiceCodeRepository: ExposedRepository<ServiceCodeTable, String, ServiceCodeModel> {

  suspend fun findAll(groupCode: String): List<ServiceCodeModel>

  override val columnConvert: (String?) -> Column<out Any?>?
    get() = TODO("Not yet implemented")

  override suspend fun delete(id: String): Int {
    TODO("Not yet implemented")
  }

  override suspend fun find(id: String): ServiceCodeModel? {
    TODO("Not yet implemented")
  }

  override suspend fun update(model: ServiceCodeModel): Int {
    TODO("Not yet implemented")
  }

  override suspend fun create(model: ServiceCodeModel): ServiceCodeModel {
    TODO("Not yet implemented")
  }

  override fun toUpdateRow(model: ServiceCodeModel): ServiceCodeTable.(UpdateStatement) -> Unit {
    TODO("Not yet implemented")
  }

  override fun toRow(model: ServiceCodeModel): ServiceCodeTable.(InsertStatement<EntityID<String>>) -> Unit {
    TODO("Not yet implemented")
  }

  override fun toModel(row: ResultRow): ServiceCodeModel {
    TODO("Not yet implemented")
  }
}
