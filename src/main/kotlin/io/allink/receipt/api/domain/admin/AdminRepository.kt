package io.allink.receipt.api.domain.admin

import io.allink.receipt.api.repository.ExposedRepository
import io.allink.receipt.api.util.DateUtil.Companion.nowLocalDateTime
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import java.util.*

/**
 * Package: io.allink.receipt.admin.domain.admin
 * Created: Devonshin
 * Date: 13/04/2025
 */

interface AdminRepository : ExposedRepository<AdminTable, UUID, AdminModel> {

  override fun toRow(model: AdminModel): AdminTable.(InsertStatement<EntityID<UUID>>) -> Unit = {
    it[loginId] = model.loginId
    it[password] = model.password
    it[fullName] = model.fullName
    it[role] = model.role.toRoleString()
    it[phone] = model.phone
    it[email] = model.email
    it[status] = model.status
    it[regDate] = nowLocalDateTime()
    it[modDate] = null
  }

  override fun toUpdateRow(model: AdminModel): AdminTable.(UpdateStatement) -> Unit = {
    it[loginId] = model.loginId
    it[password] = model.password
    it[fullName] = model.fullName
    it[role] = model.role.toRoleString()
    it[phone] = model.phone
    it[email] = model.email
    it[status] = model.status
    it[regDate] = model.regDate
    it[modDate] = nowLocalDateTime()
  }

  override fun toModel(row: ResultRow): AdminModel {
    return AdminModel(
      id = row[table.id].value,
      loginId = row[table.loginId],
      password = row[table.password],
      fullName = row[table.fullName],
      role = row[table.role].toRole()!!,
      phone = row[table.phone],
      email = row[table.email],
      status = row[table.status],
      regDate = row[table.regDate],
      modDate = row[table.modDate]
    )
  }

  suspend fun findByPhone(phone: String): AdminModel?

  suspend fun findByUserUuid(uUID: UUID): AdminModel?

  override val columnConvert: (String?) -> Column<out Any?>?
    get() = TODO("Not yet implemented")
}