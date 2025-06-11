package io.allink.receipt.api.domain.login

import io.allink.receipt.api.repository.ExposedRepository
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import java.util.*

/**
 * Package: io.allink.receipt.admin.domain.login
 * Created: Devonshin
 * Date: 14/04/2025
 */

interface LoginInfoRepository: ExposedRepository<LoginInfoTable, UUID, LoginInfoModel> {
  override fun toModel(row: ResultRow): LoginInfoModel {
    return LoginInfoModel(
      id = row[table.id].value,
      userUuid = row[table.userUuid].value,
      verificationCode = row[table.verificationCode],
      expireDate = row[table.expireDate],
      loginDate = row[table.loginDate],
      status = row[table.status])
  }

  override fun toRow(model: LoginInfoModel): LoginInfoTable.(UpdateBuilder<*>) -> Unit = {
    it[userUuid] = model.userUuid
    it[verificationCode] = model.verificationCode
    it[expireDate] = model.expireDate
    it[status] = model.status
    it[loginDate] = model.loginDate
  }

  override fun toUpdateRow(model: LoginInfoModel): LoginInfoTable.(UpdateStatement) -> Unit = {
    it[userUuid] = model.userUuid
    it[verificationCode] = model.verificationCode
    it[expireDate] = model.expireDate
    it[status] = model.status
    it[loginDate] = model.loginDate
  }

  override val columnConvert: (String?) -> Column<out Any?>?
    get() = TODO("Not yet implemented")
}