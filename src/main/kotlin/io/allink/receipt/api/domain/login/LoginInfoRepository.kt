package io.allink.receipt.api.domain.login

import io.allink.receipt.api.repository.ExposedRepository
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import java.util.UUID

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

  override fun toRow(model: LoginInfoModel): LoginInfoTable.(InsertStatement<EntityID<UUID>>) -> Unit = {
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