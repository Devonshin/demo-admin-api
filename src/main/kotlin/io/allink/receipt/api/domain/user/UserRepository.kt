package io.allink.receipt.api.domain.user

import io.allink.receipt.api.common.Constant.Companion.AES256_KEY
import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.repository.ExposedRepository
import io.allink.receipt.api.util.AES256Util
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import org.jetbrains.exposed.v1.core.statements.UpdateStatement
import org.jetbrains.exposed.v1.r2dbc.selectAll

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
        "regDate" -> UserTable.regDate
        "joinSocialType" -> UserTable.joinSocialType
        else -> null
      }
    }

  override suspend fun find(id: String): UserModel? {
    return table.selectAll().where { table.id eq id }.map { toModel(it) }.singleOrNull()
  }

  override fun toModel(row: ResultRow): UserModel {
    return Companion.toModel(row)
  }

  override fun toRow(model: UserModel): UserTable.(UpdateBuilder<*>) -> Unit = {
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

  companion object {
    fun toModel(row: ResultRow): UserModel {
      return UserModel(
        id = row[UserTable.id],
        name = row[UserTable.name],
        status = row[UserTable.status],
        phone = AES256Util.decrypt(row[UserTable.phone], AES256_KEY),
        gender = row[UserTable.gender],
        ci = "***",/*AES256Util.decrypt(row[UserTable.ci], AES256_KEY)*/
        birthday = row[UserTable.birthday],
        localYn = row[UserTable.localYn],
        email = AES256Util.decrypt(row[UserTable.email], AES256_KEY),
        role = row[UserTable.role],
        joinSocialType = row[UserTable.joinSocialType],
        nickname = AES256Util.decrypt(row[UserTable.nickname], AES256_KEY),
        mtchgId = row[UserTable.mtchgId],
        cpointRegType = row[UserTable.cpointRegType],
        cpointRegDate = row[UserTable.cpointRegDate],
        regDate = row[UserTable.regDate],
        modDate = row[UserTable.modDate],
      )
    }
  }

}