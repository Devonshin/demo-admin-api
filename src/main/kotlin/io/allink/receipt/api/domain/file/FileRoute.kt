package io.allink.receipt.api.domain.file

import io.allink.receipt.api.common.Constant.Companion.ACCEPT_FILE_FIELDS
import io.allink.receipt.api.common.Constant.Companion.ACCEPT_FILE_TYPE
import io.allink.receipt.api.common.Constant.Companion.checkAcceptFileField
import io.allink.receipt.api.common.Constant.Companion.checkAcceptFileType
import io.allink.receipt.api.common.errorResponse
import io.allink.receipt.api.domain.Request
import io.allink.receipt.api.domain.Response
import io.allink.receipt.api.exception.InvalidFileUploadException
import io.github.smiley4.ktoropenapi.config.ResponseConfig
import io.github.smiley4.ktoropenapi.config.descriptors.SwaggerTypeDescriptor
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import io.swagger.v3.oas.models.media.BinarySchema
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Package: io.allink.receipt.api.domain.file
 * Created: Devonshin
 * Date: 23/05/2025
 */

fun Route.fileRoutes(
  fileService: FileService,
) {

  route("/file") {

    post("/preview", {
      operationId = "file-preview-url"
      tags = listOf("Gestion des fichiers", "Gestion des commerçants", "Gestion des agences commerciales")
      summary = "URL temporaire d’aperçu du fichier"
      description = "Génère une URL temporaire pour prévisualiser ou télécharger un fichier. L’URL est valable 15 minutes après sa création."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<Request<String>> {
          required = true
          description = "Chemin du fichier"
          example("file-path") {
            value = Request(data = "/bz-agencies/1234-1234-1234-1234/id.png")
          }
        }
      }
      response {
        code(HttpStatusCode.OK, agencyFilePreviewResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val filePath = call.receive<Request<String>>()
      val presignedUrl = fileService.generatePresignedUrl(filePath.data, 15)
      call.respond(
        HttpStatusCode.OK,
        Response(data = presignedUrl)
      )
    }

    post("/upload/{menu}/{field}/{id}", {
      operationId = "file-upload"
      tags = listOf("Gestion des fichiers", "Gestion des commerçants", "Gestion des agences commerciales")
      summary = "Téléversement de fichier"
      description = "Téléverse un seul fichier à la fois. Après le téléversement, utilisez le chemin de fichier renvoyé pour renseigner la valeur du champ cible puis enregistrez."
      securitySchemeNames = listOf("auth-jwt")
      request {
        pathParameter<String>("menu") {
          description = "Identifiant du menu"
          required = true
          example("menu") {
            description = "Menu où le fichier est enregistré"
            value = "bz-agencies|stores|advertisements|..."
          }
        }
        pathParameter<String>("field") {
          description = "Nom du champ"
          required = true
          example("field") {
            description = "Nom du champ où le fichier est enregistré"
            value = """
          Agence commerciale: [bz|id|bank|application]
          Commerçant: [bz|id|bank|application|coupon|logo|banner]
          Publicité: [banner]
        """.trimIndent()
          }
        }
        pathParameter<String>("id") {
          description = "Identifiant unique de la cible pour laquelle le fichier est enregistré"
          required = true
          example("id") {
            description = "Identifiant unique de la cible (ex. identifiant du commerçant, identifiant de l’agence, etc.)"
          }
        }
        multipartBody {
          description = "Fichier à téléverser; formats acceptés: png, pdf, xlsx uniquement"
          required = true
          part("file", type = SwaggerTypeDescriptor(BinarySchema())) {
            required = true
          }
        }
      }
      response {
        code(HttpStatusCode.OK, agencyFileUploadResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val menu = call.pathParameters["menu"]
      val field = call.pathParameters["field"]
      val id = call.pathParameters["id"]
      val multipart = call.receiveMultipart()
      var fileName: String? = null
      var extension: String? = null
      var fileBytes: ByteArray? = null
      multipart.forEachPart { part ->
        when (part) {
          is PartData.FileItem -> {
            part.originalFileName?.let {
              fileName = it
              extension = fileName.substringAfterLast(".", "")
              fileBytes = part.readToByteArray()
            }
          }

          else -> Unit
        }
        part.dispose()
      }

      validate(menu, field, fileName, extension, id, fileBytes)
      val fileUrl = "$menu/$id/$field.$extension"
      fileService.uploadFile(fileUrl, fileBytes!!)
      call.respond(HttpStatusCode.OK, Response(data = fileUrl))
    }

    /*태그 등록 파일 다운로드 url 생성*/
    get("/download/batch-file/tag", {
      operationId = "file-download-tag"
      tags = listOf("Gestion des tags")
      summary = "URL de téléchargement du fichier d’import des tags"
      description = "Génère une URL temporaire pour télécharger le fichier d’import des tags. Valable 15 minutes après création."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<Request<String>> {
          required = true
          description = "Chemin du fichier"
          example("file-path") {
            value = Request(data = "/batch-files/templates/tag.xlsx")
          }
        }
      }
      response {
        code(HttpStatusCode.OK, agencyFilePreviewResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val presignedUrl = fileService.generatePresignedUrl("batch-files/templates/tag.xlsx", 15)
      call.respond(
        HttpStatusCode.OK,
        Response(data = presignedUrl)
      )
    }

    /*태그 배치 등록*/
    post("/upload/batch/tags", {
      operationId = "tags-batch-upload"
      tags = listOf("Gestion des tags")
      summary = "Téléversement du fichier d’import des tags"
      description = "Téléverse le fichier d’import des tags."
      securitySchemeNames = listOf("auth-jwt")
      request {
        multipartBody {
          description = "tag.xlsx"
          required = true
          part("file", type = SwaggerTypeDescriptor(BinarySchema())) {
            required = true
          }
        }
      }
      response {
        code(HttpStatusCode.OK, {
          description = "Réponse réussie"
          body<Response<String>> {
            example("Réponse du téléversement de fichier") {
              value = Response(data = "/tag-batch-status/1234-1234-1234-1234")
            }
          }
        })
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val multipart = call.receiveMultipart()
      var fileName: String? = null
      var fileBytes: ByteArray? = null
      multipart.forEachPart { part ->
        when (part) {
          is PartData.FileItem -> {
            part.originalFileName?.let {
              fileName = it
              fileBytes = part.readToByteArray()
            }
          }

          else -> Unit
        }
        part.dispose()
      }

      val fileUrl = "batch-files/uploaded/tag.xlsx"
      fileService.uploadFile(fileUrl, fileBytes!!)
      call.respond(HttpStatusCode.OK, Response(data = fileUrl))
    }

    /*가맹점 배치 등록*/
    post("/upload/batch/stores", {
      operationId = "stores-batch-upload"
      tags = listOf("Gestion des commerçants")
      summary = "Téléversement du fichier d’import des commerçants"
      description = "Téléverse le fichier d’import pour l’enregistrement en masse des commerçants."
      securitySchemeNames = listOf("auth-jwt")
      request {
        multipartBody {
          description = "store.xlsx"
          required = true
          part("file", type = SwaggerTypeDescriptor(BinarySchema())) {
            required = true
          }
        }
      }
      response {
        code(HttpStatusCode.OK, {
          description = "Réponse réussie"
          body<Response<String>> {
            example("Réponse du téléversement de fichier") {
              value = Response(data = "/store-batch/1234-1234-1234-1234")
            }
          }
        })
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val multipart = call.receiveMultipart()
      var fileBytes: ByteArray? = null
      multipart.forEachPart { part ->
        when (part) {
          is PartData.FileItem -> {
            part.originalFileName?.let {
              fileBytes = part.readToByteArray()
            }
          }

          else -> Unit
        }
        part.dispose()
      }

      val fileUrl = "batch-files/uploaded/store.xlsx"
      fileService.uploadFile(fileUrl, fileBytes!!)
      call.respond(HttpStatusCode.OK, Response(data = fileUrl))
    }


    /*태그 등록 파일 다운로드 url 생성*/
    get("/download/batch-file/stores", {
      operationId = "file-download-store"
      tags = listOf("Gestion des commerçants")
      summary = "URL de téléchargement des commerçants"
      description = "Génère une URL temporaire pour télécharger le fichier d’import des commerçants. Valable 15 minutes après création."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<Request<String>> {
          required = true
          description = "Chemin du fichier"
          example("file-path") {
            value = Request(data = "/batch-files/templates/store.xlsx")
          }
        }
      }
      response {
        code(HttpStatusCode.OK, agencyFilePreviewResponse())
        code(HttpStatusCode.BadRequest, errorResponse())
      }
    }) {
      val presignedUrl = fileService.generatePresignedUrl("batch-files/templates/tag.xlsx", 15)
      call.respond(
        HttpStatusCode.OK,
        Response(data = presignedUrl)
      )
    }
  }
}

suspend fun PartData.FileItem.readToByteArray(): ByteArray {
  val byteChannel: ByteReadChannel = provider()

  return withContext(Dispatchers.IO) {
    val outputStream = ByteArrayOutputStream()
    byteChannel.copyTo(outputStream)
    outputStream.toByteArray()
  }
}

fun validate(menu: String?, field: String?, fileName: String?, extension: String?, id: String?, fileBytes: ByteArray?) {
  if (menu == null || field == null || fileName == null || extension == null || id == null || fileBytes == null) {
    throw InvalidFileUploadException(
      """
          Échec du téléversement de fichier:  
            menu: $menu, 
            field: $field, 
            id: $id, 
            extension: $extension, 
            fileName: $fileName, 
            fileBytes: $fileBytes
        """.trimIndent()
    )
  }
  if (!checkAcceptFileField(menu, field)) {
    throw InvalidFileUploadException(
      """
          Échec du téléversement de fichier: veuillez vérifier le nom de menu [$menu] et le champ [$field]. 
          Listes de menus et champs autorisés : $ACCEPT_FILE_FIELDS
        """.trimIndent()
    )
  }
  if (!checkAcceptFileType(fileName)) {
    throw InvalidFileUploadException(
      """
          Échec du téléversement de fichier: type de fichier non autorisé  
          Extensions de fichier autorisées : $ACCEPT_FILE_TYPE
        """.trimIndent()
    )
  }
}

fun agencyFileUploadResponse(): ResponseConfig.() -> Unit = {
  description = "Réponse réussie"
  body<Response<String>> {
    example("Réponse avec le chemin du fichier téléversé") {
      value = Response(data = "/bz-agencies/1234-1234-1234-1234/id.png")
    }
  }
}

fun agencyFilePreviewResponse(): ResponseConfig.() -> Unit = {
  description = "Réponse réussie"
  body<Response<String>> {
    example("Réponse avec le chemin du fichier") {
      value = Response(data = "https://bz-agencies/1234-1234-1234-1234/id.png")
    }
  }
}