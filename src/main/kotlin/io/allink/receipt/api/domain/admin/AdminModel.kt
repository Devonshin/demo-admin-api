package io.allink.receipt.api.domain.admin

import io.allink.receipt.api.domain.BaseModel
import io.allink.receipt.api.domain.agency.bz.BzAgencyTable
import io.allink.receipt.api.util.DateUtil.Companion.nowLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.javatime.datetime
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
  val loginId: String? = null,
  val password: String? = null,
  val fullName: String,
  @Polymorphic
  val role: Role,
  val phone: String,
  val email: String? = null,
  val status: AdminStatus,
  val regDate: @Contextual LocalDateTime? = null,
  val modDate: @Contextual LocalDateTime? = null,
  val agencyUuid: @Contextual UUID? = null,
  val regBy: @Contextual UUID? = null,
  val modBy: @Contextual UUID? = null,
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
  val regBy = uuid(name = "reg_by").nullable()
  val modBy = uuid(name = "mod_by").nullable()
  val agencyUuid = reference("agency_uuid", BzAgencyTable.id).nullable()
}

@Serializable
sealed interface Role {
  val description: String
  val menus: List<Menu>
  val permission: List<Permission>
}

fun String.toRole(): Role? = when (this.uppercase()) {
  "MASTER" -> MasterRole()
  "BZ_AGENCY_STAFF" -> BzAgencyStaffRole()
  "BZ_AGENCY_MASTER" -> BzAgencyMasterRole()
  "ADV_AGENCY_STAFF" -> AdvAgencyStaffRole()
  "ADV_AGENCY_MASTER" -> AdvAgencyMasterRole()
  "MERCHANT_STAFF" -> MerchantStaffRole()
  "MERCHANT_MASTER" -> MerchantMasterRole()
  else -> null
}

fun Role.toRoleString(): String = when (this) {
  is MasterRole -> "MASTER"
  is BzAgencyStaffRole -> "BZ_AGENCY_STAFF"
  is BzAgencyMasterRole -> "BZ_AGENCY_MASTER"
  is AdvAgencyStaffRole -> "ADV_AGENCY_STAFF"
  is AdvAgencyMasterRole -> "ADV_AGENCY_MASTER"
  is MerchantStaffRole -> "MERCHANT_STAFF"
  is MerchantMasterRole -> "MERCHANT_MASTER"
}

@Serializable
data class MasterRole(
    override val description: String = "전체 마스터",
    override val menus: List<Menu> = Menu.entries.toList(),
    override val permission: List<Permission> = Permission.entries.toList()
) : Role

@Serializable
data class BzAgencyStaffRole(
    override val description: String = "영업 대리점 스태프",
    override val menus: List<Menu> = listOf(Menu.BZ_AGENCIES_STORE),
    override val permission: List<Permission> = listOf(Permission.VIEW)
) : Role

@Serializable
data class BzAgencyMasterRole(
  override val description: String = "영업 대리점 마스터",
  override val menus: List<Menu> = listOf(Menu.BZ_AGENCIES_STORE),
  override val permission: List<Permission> = listOf(Permission.VIEW)
) : Role

@Serializable
data class AdvAgencyStaffRole(
  override val description: String = "광고 대리점 스태프",
  override val menus: List<Menu> = listOf(Menu.ADV_AGENCIES),
  override val permission: List<Permission> = listOf(Permission.VIEW)
) : Role

@Serializable
data class AdvAgencyMasterRole(
    override val description: String = "광고 대리점 마스터",
    override val menus: List<Menu> = listOf(Menu.ADV_AGENCIES),
    override val permission: List<Permission> = listOf(Permission.VIEW)
) : Role

@Serializable
data class MerchantStaffRole(
    override val description: String = "가맹점 스태프",
    override val menus: List<Menu> = listOf(Menu.BZ_AGENCIES, Menu.PROFILES),
    override val permission: List<Permission> = listOf(Permission.VIEW)
) : Role

@Serializable
data class MerchantMasterRole(
  override val description: String = "가맹점 마스터",
  override val menus: List<Menu> = listOf(Menu.BZ_AGENCIES, Menu.PROFILES),
  override val permission: List<Permission> = listOf(Permission.VIEW)
) : Role

enum class Menu(val path: String) {
  SETTINGS("settings"),
  PROFILES("profiles"),
  DASHBOARD("dashboard"),
  ADVERTISEMENTS("advertisements"),
  ADV_AGENCIES("adv-agencies"), /*광고 대리점*/
  BZ_AGENCIES("bz-agencies"), /*영업 대리점*/
  BZ_AGENCIES_STORE("bz-agencies-stores"), /*영업 대리점 가맹점*/
  MERCHANTS("merchant"),
  POINTS("points"),
  RECEIPTS("receipts"),
  SETTLEMENTS("settlements"),
  STORES("stores"),
  TAGS("tags"),
  USERS("users"),
}

enum class Permission {
  VIEW,
  LIST,
  MODIFY,
}

enum class AdminStatus {
  ACTIVE,
  INACTIVE
}