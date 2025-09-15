package io.allink.receipt.api.domain.user

import io.allink.receipt.api.domain.BaseFilter
import io.allink.receipt.api.domain.BaseModel
import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.Sorter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

/**
 * Package: io.allink.receipt.admin.domain.user
 * Created: Devonshin
 * Date: 15/04/2025
 */
@Serializable
@Schema(title = "Objet Utilisateur", description = "Abonné au reçu électronique mobile")
data class UserModel(
  @param:Schema(title = "Identifiant unique", description = "Identifiant unique de l'utilisateur", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String?,
  @param:Schema(title = "Nom", description = "Nom de l'utilisateur", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val name: String?,
  @param:Schema(
    title = "Statut d'inscription",
    description = "Valeur du statut d'inscription",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED,
    exampleClasses = [UserStatus::class]
  )
  val status: UserStatus?,
  @param:Schema(
    title = "Numéro de mobile",
    description = "Numéro de mobile",
    nullable = false,
    requiredMode = RequiredMode.REQUIRED,
    example = "01012349876"
  )
  val phone: String?,
  @param:Schema(title = "Sexe", description = "Sexe", nullable = false, requiredMode = RequiredMode.REQUIRED, example = "F|M")
  val gender: String?,
  @param:Schema(title = "Valeur CI", description = "CI", nullable = false, requiredMode = RequiredMode.REQUIRED, hidden = true)
  @Transient
  val ci: String? = null,
  @param:Schema(title = "Date de naissance", description = "Date de naissance", nullable = false, requiredMode = RequiredMode.REQUIRED)
  val birthday: String?,
  @param:Schema(title = "Statut de résident national", description = "Valeur de résident national", example = "Y|N")
  val localYn: String?,
  @param:Schema(title = "E-mail", description = "Adresse e-mail", nullable = true)
  val email: String?,
  @param:Schema(title = "Rôle", description = "Rôle de l'utilisateur", exampleClasses = [UserRole::class], example = "USER|TEMP")
  val role: UserRole?,
  @param:Schema(title = "Canal d'inscription", description = "Canal d'inscription", example = "NAVER|KAKAO")
  val joinSocialType: String?,
  @param:Schema(title = "Pseudo", description = "Pseudo lié au compte social")
  val nickname: String?,
  @param:Schema(title = "탄소포인트 회원 연동아이디", description = "탄소포인트 회원 연동아이디")
  val mtchgId: String?,
  @Schema(title = "Organisme d'envoi de documents électroniques", description = "Organisme d'envoi de documents électroniques", example = "kakao|naver")
  val cpointRegType: String?,
  @Schema(title = "Date et heure d'inscription au programme Carbon Point", description = "Date et heure d'inscription au programme Carbon Point")
  val cpointRegDate: @Contextual LocalDateTime?,
  @Schema(title = "Date et heure d'inscription", description = "Date et heure d'inscription de l'utilisateur", example = "2025-03-05T13:08:12.152764")
  val regDate: @Contextual LocalDateTime?,
  @Schema(title = "Date et heure de modification", description = "Date et heure de modification de l'utilisateur", example = "2025-03-05T13:08:12.152764")
  val modDate: @Contextual LocalDateTime?
) : BaseModel<String>

object UserTable : Table("user") {
  val id = varchar("uuid", 36)
  val name = varchar("name", 255).nullable()
  val status = enumerationByName("status", 20, UserStatus::class).nullable()
  val phone = varchar("phone", 50).nullable()
  val gender = varchar("gender", 50).nullable()
  val ci = varchar("ci", 255).nullable()
  val birthday = varchar("birthday", 50).nullable()
  val localYn = varchar("local_yn", 1).nullable()
  val email = varchar("email", 255).nullable()
  val regDate = datetime("reg_date").nullable()
  val modDate = datetime("mod_date").nullable()
  val role = enumerationByName("role", 20, UserRole::class).nullable()
  val joinSocialType = varchar("join_social_type", 20).nullable()
  val nickname = varchar("nickname", 255).nullable()
  val mtchgId = varchar("mtchg_id", 255).nullable()
  val cpointRegType = varchar("cpoint_reg_type", 20).nullable()
  val cpointRegDate = datetime("cpoint_reg_date").nullable()
  override val primaryKey: PrimaryKey? = PrimaryKey(id)
}

@Schema(title = "Statut d'inscription de l'utilisateur", description = "Statut d'inscription de l'utilisateur")
enum class UserStatus {
  ACTIVE,
  NORMAL,
  INACTIVE;

  companion object {
    fun from(value: String): UserStatus {
      return try {
        valueOf(value.uppercase())
      } catch (e: IllegalArgumentException) {
        throw IllegalStateException("Unknown UserStatus: $value")
      }
    }
  }
}

@Schema(title = "Rôles de l'utilisateur", description = "Rôles de l'utilisateur")
enum class UserRole {
  USER,
  TEMP
}

@Serializable
@Schema(title = "Filtre de recherche d'utilisateur", description = "Filtre de recherche d'utilisateur")
data class UserFilter(
  @param:Schema(title = "Numéro de téléphone mobile", description = "Recherche EQ uniquement")
  val phone: String? = null,
  @param:Schema(title = "Nom", description = "Recherche par préfixe (commence par)")
  val name: String? = null,
  @param:Schema(title = "Pseudo", description = "Recherche EQ uniquement")
  val nickName: String? = null,
  @param:Schema(title = "Tranche d'âge")
  val age: Age? = null,
  @param:Schema(title = "Sexe", example = "M|F")
  val gender: String? = null,
  @param:Schema(
    title = "Tri", exampleClasses = [Sorter::class],
    description = """Champs de tri : name, nickname, phone, gender, birthday, localYn, email, role, regDate, joinSocialType"""
  )
  override val sort: List<Sorter>? = null,
  @param:Schema(title = "Pagination", requiredMode = RequiredMode.REQUIRED)
  override val page: Page = Page(1, 10)
) : BaseFilter

@Serializable
@Schema(title = "Tranche d'âge")
data class Age(
  @param:Schema(title = "Année de naissance de début", description = "Doit être antérieure à l'année de fin", example = "2002")
  val from: String,
  @param:Schema(title = "Année de naissance de fin", example = "2022")
  val to: String
)
