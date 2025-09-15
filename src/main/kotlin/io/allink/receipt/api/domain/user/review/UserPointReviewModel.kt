package io.allink.receipt.api.domain.user.review

import io.allink.receipt.api.domain.BaseModel
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime

/**
 * Package: io.allink.receipt.api.domain.review
 * Created: Devonshin
 * Date: 21/04/2025
 */

@Serializable @Schema(name = "userPointReviewModel", title = "Avis de points utilisateur", description = "Objet d'avis de points de l'utilisateur")
data class UserPointReviewModel(
  @param:Schema(
    title = "Identifiant unique de l'avis de points utilisateur",
    description = "Identifiant unique de l'avis de points de l'utilisateur",
    requiredMode = RequiredMode.REQUIRED,
    example = "3a931370-cd0b-4427-bf38-418111969c22"
  )
  override var id: String?,
  @param:Schema(title = "Identifiant unique de l'utilisateur", description = "Identifiant unique de l'utilisateur", requiredMode = RequiredMode.REQUIRED)
  val userUuid: String,
  @param:Schema(title = "Identifiant unique du partenaire de points", description = "Identifiant unique du partenaire de points", requiredMode = RequiredMode.REQUIRED)
  val storeUid: String,
  @param:Schema(title = "Identifiant unique du reçu", description = "Identifiant unique du reçu", requiredMode = RequiredMode.REQUIRED)
  val receiptUuid: String,
  @param:Schema(title = "Statut actuel", description = "Statut de rédaction de l'avis", requiredMode = RequiredMode.REQUIRED)
  val status: UserReviewStatus,
  @param:Schema(title = "Points de l'avis", description = "Points accordés pour l'avis")
  val points: Int?,
  @param:Schema(title = "Date et heure d'expiration de la rédaction de l'avis", description = "Date et heure d'expiration de la saisie de l'URL de l'avis", example = "2025-03-05T13:08:12.152764")
  val expireDate: @Contextual LocalDateTime?,
  @param:Schema(title = "URL de l'avis", description = "URL de l'avis saisie par l'utilisateur")
  val reviewUrl: String?,
  @param:Schema(title = "Date et heure d'enregistrement", description = "Date et heure de la demande d'enregistrement de l'avis", example = "2025-03-05T13:08:12.152764")
  val regDate: @Contextual LocalDateTime,
  @param:Schema(title = "Date et heure de modification", description = "Date et heure de modification de l'enregistrement de l'avis", example = "2025-03-05T13:08:12.152764")
  val modDate: @Contextual LocalDateTime?,
) : BaseModel<String>

@Schema(name = "UserReviewStatus", title = "Avis utilisateur", description = "Statut d'avancement de l'avis utilisateur", enumAsRef = true)
enum class UserReviewStatus(val desc: String) {
  WRITING("En cours de rédaction"),
  APPLIED("URL saisie terminée"),
  APPROVED("Vérification de l'avis terminée"),
  COMPLETED("Terminé"),
  REJECTED("Rejeté");
}

object UserPointReviewTable : Table("n_point_user_review") {
  val id = varchar("receipt_uuid", length = 36)
  val userUuid = varchar("user_uuid", length = 36)
  val storeUid = varchar("store_uid", length = 36)
  val status = enumerationByName<UserReviewStatus>("status", length = 10)
  val reviewUrl = varchar("review_url", length = 255).nullable()
  val regDate = datetime("reg_date")
  val modDate = datetime("mod_date").nullable()
  val points = integer("points").nullable()
  val expireDate = datetime("expire_date").nullable()
  override val primaryKey = PrimaryKey(id)
}
