package io.allink.receipt.api.domain.store

import io.allink.receipt.api.common.StatusCode
import io.allink.receipt.api.domain.*
import io.allink.receipt.api.domain.agency.bz.BzAgencyTable
import io.allink.receipt.api.domain.agency.bz.SimpleBzAgencyModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceModel
import io.allink.receipt.api.domain.store.npoint.NPointStoreServiceRegistModel
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.datetime
import java.time.LocalDateTime
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.store
 * Created: Devonshin
 * Date: 16/04/2025
 */
@Serializable
@Schema(name = "storeModel", title = "Magasin affilié", description = "Informations d'enregistrement du magasin")
data class StoreModel(
  @param:Schema(title = "Identifiant unique", description = "Identifiant unique du magasin", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String? = null,
  @param:Schema(title = "Nom du magasin", description = "Nom du magasin", requiredMode = RequiredMode.REQUIRED)
  val storeName: String,
  @param:Schema(title = "Code de franchise", description = "Code de franchise")
  val franchiseCode: String? = null,
  @param:Schema(title = "Code de zone", description = "Code de zone")
  val zoneCode: String? = null,
  @param:Schema(title = "Adresse (rue/lotissement)", description = "Adresse (rue/lotissement)")
  val addr1: String? = null,
  @param:Schema(title = "Adresse détaillée", description = "Adresse détaillée")
  val addr2: String? = null,
  @param:Schema(title = "URL de la carte", description = "URL de la carte")
  val mapUrl: String? = null,
  @param:Schema(title = "Latitude", description = "Latitude")
  val lat: String? = null,
  @param:Schema(title = "Longitude", description = "Longitude")
  val lon: String? = null,
  @param:Schema(title = "Téléphone du représentant", description = "Téléphone du représentant")
  val tel: String? = null,
  @param:Schema(title = "Numéro du contact", description = "Numéro du contact")
  val mobile: String? = null,
  @param:Schema(title = "Nom du contact", description = "Nom du contact")
  val managerName: String? = null,
  @param:Schema(title = "Adresse du site", description = "Adresse du site")
  val siteLink: String? = null,
  @param:Schema(title = "Type de travail", description = "Type de travail")
  val workType: String? = null,
  @param:Schema(title = "Numéro d'immatriculation de l'entreprise", description = "Numéro d'immatriculation de l'entreprise")
  val businessNo: String? = null,
  @param:Schema(title = "Numéro d'immatriculation de la société", description = "Numéro d'immatriculation de la société")
  val businessNoLaw: String? = null,
  @param:Schema(title = "Nom du représentant", description = "Nom du représentant")
  val ceoName: String? = null,
  @param:Schema(title = "Secteur d'activité", description = "Secteur d'activité")
  val businessType: String? = null,
  @param:Schema(title = "Type d'activité", description = "Type d'activité")
  val eventType: String? = null,
  @param:Schema(title = "E-mail", description = "E-mail")
  val email: String? = null,
  @param:Schema(title = "Type de magasin", description = "Type de magasin")
  val storeType: String? = null,
  @param:Schema(title = "URL de l'icône", description = "URL de l'icône")
  val iconUrl: String? = null,
  @param:Schema(title = "URL du logo", description = "URL du logo")
  val logoUrl: String? = null,
  @param:Schema(title = "Largeur du reçu (pouces)", description = "Largeur du reçu (pouces)")
  val receiptWidthInch: String? = null,
  @param:Schema(
    name = "status",
    title = "Statut du magasin",
    description = "Code de statut du magasin",
    example = "ACTIVE,NORMAL: normal, INACTIVE: arrêté, PENDING: en attente, DELETED: supprimé",
    requiredMode = RequiredMode.REQUIRED,
    allowableValues = ["ACTIVE", "NORMAL", "INACTIVE", "PENDING", "DELETED"]
  )
  val status: StatusCode? = null,
  @param:Schema(title = "ID de connexion du magasin", description = "ID de connexion du magasin")
  val partnerLoginId: String? = null,
  @param:Schema(title = "Mot de passe de connexion du magasin", description = "Mot de passe de connexion du magasin", hidden = true)
  @Transient
  val partnerLoginPassword: String? = null,
  @param:Schema(
    title = "Chemin du fichier de la demande",
    description = "Détermine l'enregistrement de la demande d'agence selon la présence de la valeur; si null, non enregistré; définir la valeur reçue après le téléversement du fichier",
    example = "/stores/c7f0d23e-eceb-4434-b489-668c0b61a7f9/application.pdf | null"
  )
  val applicationFilePath: String? = null,
  @param:Schema(
    title = "Chemin du fichier de l'extrait d'immatriculation",
    description = "Détermine l'enregistrement du fichier d'immatriculation selon la présence de la valeur; si null, non enregistré; définir la valeur reçue après le téléversement du fichier",
    example = "/stores/c7f0d23e-eceb-4434-b489-668c0b61a7f9/bz.pdf | null"
  )
  val bzFilePath: String? = null,
  @param:Schema(
    title = "Chemin du fichier de la pièce d'identité du représentant",
    description = "Détermine l'enregistrement du fichier d'identité du représentant selon la présence de la valeur; si null, non enregistré; définir la valeur reçue après le téléversement du fichier",
    example = "/stores/c7f0d23e-eceb-4434-b489-668c0b61a7f9/id.pdf"
  )
  val idFilePath: String? = null,
  @param:Schema(
    title = "Chemin du fichier de la copie du relevé bancaire",
    description = "Détermine l'enregistrement de la copie du relevé bancaire selon la présence de la valeur; si null, non enregistré; définir la valeur reçue après le téléversement du fichier",
    example = "/stores/c7f0d23e-eceb-4434-b489-668c0b61a7f9/bank.pdf | null"
  )
  val bankFilePath: String? = null,
  @param:Schema(title = "Informations du magasin de points", description = "Informations du magasin de points")
  val nPointStore: NPointStoreModel? = null,
  @param:Schema(title = "Date d'enregistrement", description = "Date d'enregistrement", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime? = null,
  @param:Schema(title = "Date de modification", description = "Date de modification", example = "2025-04-17 12:00:00.213123")
  val modDate: @Contextual LocalDateTime? = null,
  @param:Schema(title = "Date de suppression", description = "Date de suppression", example = "2025-04-17 12:00:00.213123")
  val deleteDate: @Contextual LocalDateTime? = null,
  @param:Schema(title = "ID de l'enregistreur", description = "ID de l'enregistreur")
  val regBy: @Contextual UUID? = null,
  @param:Schema(title = "ID du modificateur", description = "ID du modificateur")
  val modBy: @Contextual UUID? = null,
  @param:Schema(title = "Service d'avis Naver Points", description = "Service d'avis Naver Points")
  val npointStoreServices: List<NPointStoreServiceModel>? = null,
  @param:Schema(title = "Publicité de coupon", description = "Indique si le magasin fait de la publicité par coupon; l'image du coupon est requise", example = "true|false")
  val couponAdYn: Boolean? = false,
  @param:Schema(title = "Liste des jetons de carte enregistrés du magasin", description = "Liste des informations de jeton de carte du magasin")
  val storeBillingTokens: List<StoreBillingTokenModel>? = null,
  @param:Schema(title = "Informations de paiement du service du magasin", description = "Informations de paiement du service du magasin")
  var storeBilling: StoreBillingModel? = null,
  @param:Schema(title = "Agence commerciale du magasin", description = "Agence commerciale du magasin")
  val bzAgency: SimpleBzAgencyModel? = null,
) : BaseModel<String>

@Serializable
@Schema(name = "simpleStoreModel", title = "Magasin affilié", description = "Informations succinctes du magasin")
data class SimpleStoreModel(
  @param:Schema(title = "Identifiant unique du magasin", description = "Identifiant unique du magasin", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String? = null,
  @param:Schema(title = "Nom du magasin", description = "Nom du magasin", requiredMode = RequiredMode.REQUIRED)
  val storeName: String,
  @param:Schema(title = "Code de franchise", description = "Code de franchise")
  val franchiseCode: String? = null,
  @param:Schema(title = "Numéro d'immatriculation de l'entreprise", description = "Numéro d'immatriculation de l'entreprise")
  val businessNo: String? = null,
  @param:Schema(title = "Nom du représentant", description = "Nom du représentant")
  val ceoName: String? = null,
) : BaseModel<String>


@Serializable
@Schema(name = "storeSearchModel", title = "Magasin affilié", description = "Informations succinctes pour le mappage du magasin")
data class StoreSearchModel(
  @param:Schema(title = "Identifiant unique du magasin", description = "Identifiant unique du magasin", nullable = false, requiredMode = RequiredMode.REQUIRED)
  override var id: String? = null,
  @param:Schema(title = "Nom du magasin", description = "Nom du magasin", requiredMode = RequiredMode.REQUIRED)
  val storeName: String,
  @param:Schema(title = "Code de franchise", description = "Code de franchise")
  val franchiseCode: String? = null,
  @param:Schema(title = "Numéro d'immatriculation de l'entreprise", description = "Numéro d'immatriculation de l'entreprise")
  val businessNo: String? = null,
  @param:Schema(title = "Nom du représentant", description = "Nom du représentant")
  val ceoName: String? = null,
  @param:Schema(title = "Téléphone principal", description = "Téléphone principal")
  val tel: String? = null,
  @param:Schema(title = "Secteur d'activité", description = "Secteur d'activité")
  val businessType: String? = null,
  @param:Schema(title = "Type d'activité", description = "Type d'activité")
  val eventType: String? = null,
  @param:Schema(title = "Type d'appareil", description = "Type d'appareil", example = "CAT, OKPOS,...")
  val deviceType: String? = null,
  @param:Schema(
    name = "status",
    title = "Statut du magasin",
    description = "Code de statut du magasin",
    example = "ACTIVE,NORMAL: normal, INACTIVE: arrêté, PENDING: en attente, DELETED: supprimé",
    requiredMode = RequiredMode.REQUIRED,
    allowableValues = ["ACTIVE", "NORMAL", "INACTIVE", "PENDING", "DELETED"]
  )
  val status: StatusCode? = null,
  @param:Schema(title = "Date d'enregistrement", description = "Date d'enregistrement", example = "2025-04-17 12:00:00.213123")
  val regDate: @Contextual LocalDateTime? = null,
) : BaseModel<String>


@Serializable
@Schema(name = "storeRegistModel", title = "Magasin affilié", description = "Informations de demande d'enregistrement du magasin")
data class StoreRegistModel(
  @param:Schema(title = "Nom du magasin", description = "Nom du magasin", requiredMode = RequiredMode.REQUIRED)
  val storeName: String,
  @param:Schema(title = "Code de franchise", description = "Code de franchise", requiredMode = RequiredMode.REQUIRED)
  val franchiseCode: String? = null,
  @param:Schema(title = "Adresse (rue/lotissement)", description = "Adresse (rue/lotissement)", requiredMode = RequiredMode.REQUIRED)
  val addr1: String? = null,
  @param:Schema(title = "Adresse détaillée", description = "Adresse détaillée")
  val addr2: String? = null,
  @param:Schema(title = "Téléphone du représentant", description = "Téléphone du représentant", requiredMode = RequiredMode.REQUIRED)
  val tel: String? = null,
  @param:Schema(title = "Numéro du contact", description = "Numéro du contact")
  val mobile: String? = null,
  @param:Schema(title = "Nom du contact", description = "Nom du contact")
  val managerName: String? = null,
  @param:Schema(title = "Type de travail", description = "Type de travail")
  val workType: String? = null,
  @param:Schema(title = "Numéro d'immatriculation de l'entreprise", description = "Numéro d'immatriculation de l'entreprise", requiredMode = RequiredMode.REQUIRED, example = "123-45-67890")
  val businessNo: String,
  @param:Schema(title = "Numéro d'immatriculation de la société", description = "Numéro d'immatriculation de la société")
  val businessNoLaw: String? = null,
  @param:Schema(title = "Nom du représentant", description = "Nom du représentant", requiredMode = RequiredMode.REQUIRED)
  val ceoName: String? = null,
  @param:Schema(title = "Secteur d'activité", description = "Secteur d'activité")
  val businessType: String? = null,
  @param:Schema(title = "Type d'activité", description = "Type d'activité")
  val eventType: String? = null,
  @param:Schema(title = "E-mail", description = "E-mail")
  val email: String? = null,
  @param:Schema(
    title = "Chemin du fichier de la demande",
    description = "Détermine l'enregistrement de la demande d'agence selon la présence de la valeur; si null, non enregistré; définir la valeur reçue après le téléversement du fichier",
    example = "/stores/c7f0d23e-eceb-4434-b489-668c0b61a7f9/application.pdf | null"
  )
  val applicationFilePath: String? = null,
  @param:Schema(
    title = "Chemin du fichier de l'extrait d'immatriculation",
    description = "Détermine l'enregistrement du fichier d'immatriculation selon la présence de la valeur; si null, non enregistré; définir la valeur reçue après le téléversement du fichier",
    example = "/stores/c7f0d23e-eceb-4434-b489-668c0b61a7f9/bz.pdf | null"
  )
  val bzFilePath: String? = null,
  @param:Schema(
    title = "Chemin du fichier de la pièce d'identité du représentant",
    description = "Détermine l'enregistrement du fichier d'identité du représentant selon la présence de la valeur; si null, non enregistré; définir la valeur reçue après le téléversement du fichier",
    example = "/stores/c7f0d23e-eceb-4434-b489-668c0b61a7f9/id.pdf"
  )
  val idFilePath: String? = null,
  @param:Schema(
    title = "Chemin du fichier de la copie du relevé bancaire",
    description = "Détermine l'enregistrement de la copie du relevé bancaire selon la présence de la valeur; si null, non enregistré; définir la valeur reçue après le téléversement du fichier",
    example = "/stores/c7f0d23e-eceb-4434-b489-668c0b61a7f9/bank.pdf | null"
  )
  val bankFilePath: String? = null,
  @param:Schema(
    name = "status",
    title = "Statut du magasin",
    description = "Code de statut du magasin",
    requiredMode = RequiredMode.REQUIRED,
    example = "ACTIVE,NORMAL: normal, INACTIVE: arrêté, PENDING: en attente, DELETED: supprimé",
    allowableValues = ["ACTIVE", "NORMAL", "INACTIVE", "PENDING", "DELETED"]
  )
  val status: StatusCode? = null,
  @param:Schema(
    title = "Services à utiliser par le magasin",
    description = "Services à utiliser par le magasin. Point important : informations de paiement (storeBilling) requises. Si npointStoreServices est fourni, les informations de paiement doivent aussi être fournies."
  )
  val npointStoreServices: List<NPointStoreServiceRegistModel>? = null,
  @param:Schema(title = "Informations de paiement du service du magasin", description = "En environnement de développement, toujours traité avec succès. Les tests réels ne sont possibles qu'en production.")
  val storeBilling: StoreBillingRegistModel? = null,
  @param:Schema(title = "Publicité de coupon", description = "Indique si le magasin fait de la publicité par coupon; l'image du coupon est requise", example = "true|false")
  val couponAdYn: Boolean? = false,
  @param:Schema(title = "Identifiant unique de l'agence commerciale du magasin", description = "Identifiant unique de l'agence commerciale du magasin")
  val bzAgencyId: String? = null,
)

@Serializable
@Schema(name = "storeModifyModel", title = "Informations de demande de modification du magasin", description = "Informations de demande de modification du magasin")
data class StoreModifyModel(
  @param:Schema(
    title = "Identifiant unique du magasin",
    description = "Identifiant unique du reçu de l'utilisateur",
    requiredMode = RequiredMode.REQUIRED,
    example = "3a931370-cd0b-4427-bf38-418111969c22"
  )
  val id: String,
  @param:Schema(title = "Nom du magasin", description = "Nom du magasin", requiredMode = RequiredMode.REQUIRED)
  val storeName: String,
  @param:Schema(title = "Code de franchise", description = "Code de franchise", requiredMode = RequiredMode.REQUIRED)
  val franchiseCode: String? = null,
  @param:Schema(title = "Adresse (rue/lotissement)", description = "Adresse (rue/lotissement)", requiredMode = RequiredMode.REQUIRED)
  val addr1: String? = null,
  @param:Schema(title = "Adresse détaillée", description = "Adresse détaillée")
  val addr2: String? = null,
  @param:Schema(title = "Téléphone du représentant", description = "Téléphone du représentant", requiredMode = RequiredMode.REQUIRED)
  val tel: String? = null,
  @param:Schema(title = "Numéro du contact", description = "Numéro du contact")
  val mobile: String? = null,
  @param:Schema(title = "Nom du contact", description = "Nom du contact")
  val managerName: String? = null,
  @param:Schema(title = "Type de travail", description = "Type de travail")
  val workType: String? = null,
  @param:Schema(title = "Numéro d'immatriculation de l'entreprise", description = "Numéro d'immatriculation de l'entreprise", requiredMode = RequiredMode.REQUIRED)
  val businessNo: String? = null,
  @param:Schema(title = "Numéro d'immatriculation de la société", description = "Numéro d'immatriculation de la société")
  val businessNoLaw: String? = null,
  @param:Schema(title = "Nom du représentant", description = "Nom du représentant", requiredMode = RequiredMode.REQUIRED)
  val ceoName: String? = null,
  @param:Schema(title = "Secteur d'activité", description = "Secteur d'activité")
  val businessType: String? = null,
  @param:Schema(title = "Type d'activité", description = "Type d'activité")
  val eventType: String? = null,
  @param:Schema(title = "E-mail", description = "E-mail")
  val email: String? = null,
  @param:Schema(
    title = "Chemin du fichier de la demande",
    description = "Détermine l'enregistrement de la demande d'agence selon la présence de la valeur; si null, non enregistré; définir la valeur reçue après le téléversement du fichier",
    example = "/stores/c7f0d23e-eceb-4434-b489-668c0b61a7f9/application.pdf | null"
  )
  val applicationFilePath: String? = null,
  @param:Schema(
    title = "Chemin du fichier de l'extrait d'immatriculation",
    description = "Détermine l'enregistrement du fichier d'immatriculation selon la présence de la valeur; si null, non enregistré; définir la valeur reçue après le téléversement du fichier",
    example = "/stores/c7f0d23e-eceb-4434-b489-668c0b61a7f9/bz.pdf | null"
  )
  val bzFilePath: String? = null,
  @param:Schema(
    title = "Chemin du fichier de la pièce d'identité du représentant",
    description = "Détermine l'enregistrement du fichier d'identité du représentant selon la présence de la valeur; si null, non enregistré; définir la valeur reçue après le téléversement du fichier",
    example = "/stores/c7f0d23e-eceb-4434-b489-668c0b61a7f9/id.pdf | null"
  )
  val idFilePath: String? = null,
  @param:Schema(
    title = "Chemin du fichier de la copie du relevé bancaire",
    description = "Détermine l'enregistrement de la copie du relevé bancaire selon la présence de la valeur; si null, non enregistré; définir la valeur reçue après le téléversement du fichier",
    example = "/stores/c7f0d23e-eceb-4434-b489-668c0b61a7f9/bank.pdf | null"
  )
  val bankFilePath: String? = null,
  @param:Schema(
    name = "status",
    title = "Statut du magasin",
    description = "Code de statut du magasin",
    example = "ACTIVE,NORMAL: normal, INACTIVE: arrêté, PENDING: en attente, DELETED: supprimé",
    requiredMode = RequiredMode.REQUIRED,
    allowableValues = ["ACTIVE", "NORMAL", "INACTIVE", "PENDING", "DELETED"]
  )
  val status: StatusCode? = null,
  @param:Schema(
    title = "Services à utiliser par le magasin",
    description = "Important : informations de paiement (storeBilling) requises. Si npointStoreServices est fourni, les informations de paiement doivent aussi être fournies. Renseignez ces valeurs uniquement si des modifications sont nécessaires; laissez à null pour conserver l'état actuel. Les services et informations de paiement envoyés lors de la modification seront appliqués à partir du 1er du mois suivant."
  )
  val npointStoreServices: List<NPointStoreServiceRegistModel>? = listOf(),
  @param:Schema(title = "Informations de paiement du service du magasin", description = "En environnement de développement, toujours traité avec succès. Les tests réels ne sont possibles qu'en production.")
  val storeBilling: StoreBillingRegistModel? = null,
  @param:Schema(title = "Publicité de coupon", description = "Indique si le magasin fait de la publicité par coupon; l'image du coupon est requise", example = "true|false")
  val couponAdYn: Boolean? = false,
  @param:Schema(title = "Identifiant unique de l'agence commerciale du magasin", description = "Identifiant unique de l'agence commerciale du magasin")
  val bzAgencyId: String? = null
)

object StoreTable : Table("store") {
  val id = varchar("store_uid", length = 36)
  val storeName = varchar("store_name", length = 255)
  val storeType = varchar("store_type", length = 255).nullable()
  val zoneCode = varchar("zone_code", length = 20).nullable()
  val addr1 = varchar("addr1", length = 255).nullable()
  val addr2 = varchar("addr2", length = 255).nullable()
  val iconUrl = varchar("icon_url", length = 255).nullable()
  val logoUrl = varchar("logo_url", length = 255).nullable()
  val franchiseCode = varchar("franchise_code", length = 30).nullable()
  val mapUrl = varchar("map_url", length = 255).nullable()
  val lat = varchar("lat", length = 20).nullable()
  val lon = varchar("lon", length = 20).nullable()
  val tel = varchar("tel", length = 15).nullable()
  val mobile = varchar("mobile", length = 15).nullable()
  val managerName = varchar("manager_name", length = 30).nullable()
  val siteLink = varchar("site_link", length = 255).nullable()
  val receiptWidthInch = varchar("receipt_width_inch", length = 2).nullable()
  val status = enumerationByName<StatusCode>("status", 20).nullable()
  val workType = varchar("work_type", length = 30).nullable()
  val businessNo = varchar("business_no", length = 30).nullable()
  val partnerLoginId = varchar("partner_login_id", length = 50).nullable()
  val partnerLoginPassword = varchar("partner_login_pword", length = 255).nullable()
  val ceoName = varchar("ceo_name", length = 30).nullable()
  val businessType = varchar("business_type", length = 255).nullable()
  val eventType = varchar("event_type", length = 255).nullable()
  val email = varchar("email", length = 255).nullable()
  val businessNoLaw = varchar("business_no_law", length = 30).nullable()
  val couponAdYn = bool("coupon_ad_yn").nullable()
  val applicationFilePath = varchar(name = "application_file_path", length = 255).nullable()
  val bzFilePath = varchar(name = "bz_file_path", length = 255).nullable()
  val idFilePath = varchar(name = "id_file_path", length = 255).nullable()
  val bankFilePath = varchar(name = "bank_file_path", length = 255).nullable()
  val bzAgencyId = reference("bz_agency_uuid", BzAgencyTable.id).nullable()
  val regDate = datetime("reg_date").nullable()
  val regBy = uuid("reg_by").nullable()
  val modDate = datetime("mod_date").nullable()
  val modBy = uuid("mod_by").nullable()
  val deleteDate = datetime("delete_date").nullable()
  override val primaryKey = PrimaryKey(id)
}

@Serializable
@Schema(name = "searchStoreFilter", title = "Filtre de recherche de magasin pour mappage", description = "Filtre de recherche de magasin pour mappage, p. ex. lors de l'enregistrement de balises")
data class StoreSearchFilter(
  @param:Schema(
    title = "Identifiant unique du magasin",
    description = "Identifiant unique du magasin, recherche EQ",
    example = "store-123-45-67890",
  )
  val id: String? = null,
  @param:Schema(
    title = "Numéro d'immatriculation de l'entreprise",
    description = "Numéro d'immatriculation de l'entreprise, recherche EQ",
    example = "123-45-67890",
  )
  val businessNo: String? = null,
  @param:Schema(title = "Nom du magasin", description = "Recherche \"commence par\"")
  val name: String? = null,
  @param:Schema(
    title = "Code de franchise",
    description = "Code de franchise, recherche EQ",
    example = "EDIYA",
  )
  val franchiseCode: String? = null,
  @param:Schema(
    title = "Tri", exampleClasses = [Sorter::class], description = """
    Champs de tri : id, businessNo, name, franchiseCode
  """
  )
  override val sort: List<Sorter>? = null,
  @param:Schema(title = "Pagination", requiredMode = RequiredMode.REQUIRED)
  override val page: Page = Page(1, 10)
) : BaseFilter

@Serializable
@Schema(name = "storeFilter", title = "Filtre de recherche de magasin", description = "Filtre de recherche de magasin")
data class StoreFilter(
  @param:Schema(title = "ID du magasin", description = "Identifiant unique du magasin, recherche EQ")
  val id: String? = null,
  @param:Schema(
    title = "Numéro d'immatriculation de l'entreprise",
    description = "Numéro d'immatriculation de l'entreprise, recherche EQ",
    example = "123-45-67890",
  )
  val businessNo: String? = null,
  @param:Schema(title = "Nom du magasin", description = "Recherche \"commence par\"")
  val name: String? = null,
  @param:Schema(
    title = "Code de franchise",
    description = "Recherche EQ, code de franchise obtenu à partir de la consultation du code de service",
    example = "EDIYA",
  )
  val franchiseCode: String? = null,
  @param:Schema(
    title = "Période de recherche",
    description = "Intervalle de début et de fin de la période à rechercher",
    example = """{"from: "2025-04-17T12:00:00", "to: "2025-05-17T12:00:00"}"""
  )
  val period: PeriodFilter,
  @param:Schema(
    title = "Tri", exampleClasses = [Sorter::class], description = """
    Champs de tri : id, businessNo, name, franchiseCode, regDate, modDate, addr1, managerName, ceoName
  """
  )
  override val sort: List<Sorter>? = null,
  @param:Schema(title = "Pagination", requiredMode = RequiredMode.REQUIRED)
  override val page: Page = Page(1, 10)
) : BaseFilter

