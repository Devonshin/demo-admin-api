package io.allink.receipt.api.domain.admin

import io.allink.receipt.api.domain.BaseModel
import io.allink.receipt.api.util.DateUtil.Companion.nowLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.*


/**
 * Package: io.allink.receipt.admin.domain.admin
 * Created: Devonshin
 * Date: 12/04/2025
 */

@Serializable
data class AdminModel(
  override var id: @Contextual UUID? = null,
  val loginId: String?,
  val password: String?,
  val fullName: String,
  val role: Role,
  val phone: String,
  val email: String?,
  val status: AdminStatus,
  val regDate: @Contextual LocalDateTime,
  val modDate: @Contextual LocalDateTime?,
) : BaseModel<UUID>

object AdminTable : UUIDTable(name = "admin", columnName = "uuid") {
  val loginId = varchar(name = "login_id", length = 100).nullable()
  val password = varchar(name = "password", length = 255).nullable()
  val fullName = varchar(name = "full_name", length = 50)
  val role = varchar(name = "role", length = 20)
  val phone = varchar(name = "phone", length = 15)
  val email = varchar(name = "email", length = 100).nullable()
  val status = enumerationByName("status", 20, AdminStatus::class)
  val regDate = datetime(name = "reg_date").default(nowLocalDateTime())
  val modDate = datetime(name = "mod_date").nullable()
}

sealed interface Role {
  val description: String
  val menus: List<Menu>
  val permission: List<Permission>
}

fun String.toRole(): Role? = when (this.uppercase()) {
  "ROLE_MASTER" -> MasterRole
  "ROLE_AGENCY_STAFF" -> AgencyStaffRole
  "ROLE_AGENCY_MASTER" -> AgencyMasterRole
  "ROLE_MERCHANT_STAFF" -> MerchantStaffRole
  "ROLE_MERCHANT_MASTER" -> MerchantMasterRole
  else -> null
}

fun Role.toRoleString(): String = when (this) {
  is MasterRole -> "ROLE_MASTER"
  is AgencyStaffRole -> "ROLE_AGENCY_STAFF"
  is AgencyMasterRole -> "ROLE_AGENCY_MASTER"
  is MerchantStaffRole -> "ROLE_MERCHANT_STAFF"
  is MerchantMasterRole -> "ROLE_MERCHANT_MASTER"
}

object MasterRole : Role {
  override val description = "마스터 권한"
  override val menus = Menu.entries.toList()
  override val permission = Permission.entries.toList()
}

object AgencyStaffRole : Role {
  override val description = "광고 스태프 권한"
  override val menus = listOf(Menu.ADV_AGENCY)
  override val permission = listOf(Permission.VIEW)
}

object AgencyMasterRole : Role {
  override val description = "광고 마스터 권한"
  override val menus = listOf(Menu.ADV_AGENCY)
  override val permission = listOf(Permission.VIEW)
}

object MerchantStaffRole : Role {
  override val description = "대리점 스태프 권한"
  override val menus = listOf(Menu.BZ_AGENCY, Menu.PROFILE)
  override val permission = listOf(Permission.VIEW)
}

object MerchantMasterRole : Role {
  override val description = "대리점 마스터 권한"
  override val menus = listOf(Menu.BZ_AGENCY, Menu.PROFILE)
  override val permission = listOf(Permission.VIEW)
}

enum class Menu(val path: String) {
  SETTING("setting"),
  PROFILE("profile"),
  DASHBOARD("dashboard"),
  ADVERTISEMENT("advertisement"),
  ADV_AGENCY("adv-agency"), /*광고 대리점*/
  BZ_AGENCY("bz-agency"), /*영업 대리점*/
  MERCHANT("merchant"),
  POINT("point"),
  RECEIPT("receipt"),
  SETTLEMENT("settlement"),
  STORE("store"),
  TAG("tag"),
  USER("user"),
}

enum class Permission {
  VIEW,
  LIST,
  CREATE,
  UPDATE,
  DELETE
}

enum class AdminStatus {
  ACTIVE,
  INACTIVE
}