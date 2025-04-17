package io.allink.receipt.api.domain.code

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll

class ServiceCodeRepositoryImpl(
  override val table: ServiceCodeTable
) : ServiceCodeRepository {

  override suspend fun findAll(groupCode: String): List<ServiceCodeModel> = query {
    table.selectAll().where(
      table.serviceGroup.eq(groupCode)
    ).toList().let {
      return@let it.map { code ->
        ServiceCodeModel(
          id = code[table.serviceCode],
          serviceGroup = code[table.serviceGroup],
          serviceName = code[table.serviceName],
          price = code[table.price],
          status = code[table.status]
        )
      }
    }
  }

}
