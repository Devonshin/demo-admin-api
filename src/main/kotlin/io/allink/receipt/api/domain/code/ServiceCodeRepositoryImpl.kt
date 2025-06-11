package io.allink.receipt.api.domain.code

import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.r2dbc.selectAll

class ServiceCodeRepositoryImpl(
  override val table: ServiceCodeTable
) : ServiceCodeRepository {

  override suspend fun findAll(groupCode: String): List<ServiceCodeModel> {
    return table.selectAll().where(
      table.serviceGroup eq groupCode
    ).toList().let {
      return@let it.map { code ->
        ServiceCodeModel(
          id = code[table.serviceCode],
          serviceGroup = code[table.serviceGroup],
          serviceName = code[table.serviceName],
          price = code[table.price],
          status = code[table.status],
          serviceType = code[table.serviceType]
        )
      }
    }
  }

}
