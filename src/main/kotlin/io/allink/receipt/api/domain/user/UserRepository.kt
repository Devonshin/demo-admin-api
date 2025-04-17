package io.allink.receipt.api.domain.user

import io.allink.receipt.api.common.Constant.Companion.AES256_KEY
import io.allink.receipt.api.common.ExposedRepository
import io.allink.receipt.api.common.PagedResult
import io.allink.receipt.api.util.AES256Util
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement

/**
 * Package: io.allink.receipt.admin.domain.user
 * Created: Devonshin
 * Date: 15/04/2025
 */

interface UserRepository : ExposedRepository<UserTable, String, UserModel> {

  suspend fun findAll(filter: UserFilter): PagedResult<UserModel>

  override val columnConvert: (String?) -> Column<out Any?>?
    get() = { column ->
      if (column == null) null
      when (column) {
        "name" -> UserTable.name
        "nickname" -> UserTable.nickname
        "phone" -> UserTable.phone
        "gender" -> UserTable.gender
        "birthday" -> UserTable.birthday
        "localYn" -> UserTable.localYn
        "email" -> UserTable.email
        "role" -> UserTable.role
        "joinSocialType" -> UserTable.joinSocialType
        else -> null
      }
    }

  override suspend fun find(id: String): UserModel? = query {
    table.selectAll().where { table.id eq id }.map { toModel(it) }.singleOrNull()
  }

  override fun toModel(row: ResultRow): UserModel {
    return UserModel(
      id = row[table.id],
      name = row[table.name],
      status = row[table.status],
      phone = AES256Util.decrypt(row[table.phone], AES256_KEY),
      gender = row[table.gender],
      ci = "***",/*AES256Util.decrypt(row[table.ci], AES256_KEY)*/
      birthday = row[table.birthday],
      localYn = row[table.localYn],
      email = AES256Util.decrypt(row[table.email], AES256_KEY),
      role = row[table.role],
      joinSocialType = row[table.joinSocialType],
      nickname = AES256Util.decrypt(row[table.nickname], AES256_KEY),
      mtchgId = row[table.mtchgId],
      cpointRegType = row[table.cpointRegType],
      cpointRegDate = row[table.cpointRegDate],
      regDate = row[table.regDate],
      modDate = row[table.modDate],
    )
  }

  override fun toRow(model: UserModel): UserTable.(InsertStatement<EntityID<String>>) -> Unit = {
    it[name] = model.name
    it[status] = model.status
    it[phone] = AES256Util.encrypt(model.phone, AES256_KEY)
    it[gender] = model.gender
    it[ci] = AES256Util.encrypt(model.ci, AES256_KEY)
    it[birthday] = model.birthday
    it[nickname] = AES256Util.encrypt(model.nickname, AES256_KEY)
    it[localYn] = model.localYn
    it[email] = AES256Util.encrypt(model.email, AES256_KEY)
    it[role] = model.role
    it[joinSocialType] = model.joinSocialType
    it[mtchgId] = model.mtchgId
    it[cpointRegType] = model.cpointRegType
    it[cpointRegDate] = model.cpointRegDate
    it[regDate] = model.regDate
    it[modDate] = model.modDate
  }

  override fun toUpdateRow(model: UserModel): UserTable.(UpdateStatement) -> Unit = {
    it[name] = model.name
    it[status] = model.status
    it[phone] = AES256Util.encrypt(model.phone, AES256_KEY)
    it[gender] = model.gender
    it[ci] = AES256Util.encrypt(model.ci, AES256_KEY)
    it[birthday] = model.birthday
    it[nickname] = AES256Util.encrypt(model.nickname, AES256_KEY)
    it[localYn] = model.localYn
    it[email] = AES256Util.encrypt(model.email, AES256_KEY)
    it[role] = model.role
    it[joinSocialType] = model.joinSocialType
    it[mtchgId] = model.mtchgId
    it[cpointRegType] = model.cpointRegType
    it[cpointRegDate] = model.cpointRegDate
    it[modDate] = model.modDate
  }

}