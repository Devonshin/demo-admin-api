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
      tags = listOf("파일 관리", "가맹점 관리", "영업 대리점 관리")
      summary = "파일 미리보기 임시 URL"
      description = "파일을 미리보기 혹은 다운로드 할 수 있는 임시 URL을 생성합니다. 임시 URL은 생성 후 15분간 유효합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<Request<String>> {
          required = true
          description = "파일 경로"
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
      tags = listOf("파일 관리", "가맹점 관리", "영업 대리점 관리")
      summary = "파일 업로드"
      description = "한번에 하나의 파일을 업로드를 합니다. 파일을 업로드 후 응답으로 생성되는 파일 경로를 대상 필드의 값으로 설정 후 저장해야 합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        pathParameter<String>("menu") {
          description = "메뉴 아이디"
          required = true
          example("menu") {
            description = "파일을 등록하는 메뉴"
            value = "bz-agencies|stores|advertisements|..."
          }
        }
        pathParameter<String>("field") {
          description = "필드명"
          required = true
          example("field") {
            description = "파일을 등록하는 필드명"
            value = """
          영업 대리점: [bz|id|bank|application]
          가맹점: [bz|id|bank|application|coupon|logo|banner]
          광고: [banner]
        """.trimIndent()
          }
        }
        pathParameter<String>("id") {
          description = "파일을 등록하는 대상의 고유 아이디"
          required = true
          example("id") {
            description = "파일을 등록하는 대상의 고유 아이디 ( EX. 가맹점 고유아이디, 대리점 고유아이디.. )"
          }
        }
        multipartBody {
          description = "업로드 파일, png, pdf, xlsx 만 가능"
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
    get("/download/batch-file/tag",  {
      operationId = "file-download-tag"
      tags = listOf("태그 관리")
      summary = "태그 일괄 등록 파일 다운로드 URL"
      description = "태그 일괄 등록용 파일을 다운로드 할 수 있는 임시 URL을 생성합니다. 임시 URL은 생성 후 15분간 유효합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<Request<String>> {
          required = true
          description = "파일 경로"
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
      tags = listOf("태그 관리")
      summary = "태그 일괄 등록 파일 업로드"
      description = "태그 일괄 등록용 파일을 업로드 합니다."
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
          description = "성공 응답"
          body<Response<String>> {
            example("파일 업로드 응답") {
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
      tags = listOf("가맹점 관리")
      summary = "가맹점 배치 파일 업로드"
      description = "가맹점 일괄 등록용 배치 파일을 업로드 합니다."
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
          description = "성공 응답"
          body<Response<String>> {
            example("파일 업로드 응답") {
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
    get("/download/batch-file/stores",  {
      operationId = "file-download-store"
      tags = listOf("가맹점 관리")
      summary = "가맹점 다운로드 URL"
      description = "가맹점 일괄 등록용 파일을 다운로드 할 수 있는 임시 URL을 생성합니다. 임시 URL은 생성 후 15분간 유효합니다."
      securitySchemeNames = listOf("auth-jwt")
      request {
        body<Request<String>> {
          required = true
          description = "파일 경로"
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
          파일 업로드 실패:  
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
          파일 업로드 실패: 요청하신 메뉴명[$menu]과, 필드[$field]를 확인 해주세요. 
          요청 가능 메뉴와 필드 목록 : $ACCEPT_FILE_FIELDS
        """.trimIndent()
    )
  }
  if (!checkAcceptFileType(fileName)) {
    throw InvalidFileUploadException(
      """
          파일 업로드 실패: 업로드 불가 파일  
          업로드 가능한 파일 확장자 : $ACCEPT_FILE_TYPE
        """.trimIndent()
    )
  }
}

fun agencyFileUploadResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<String>> {
    example("파일 업로드 경로 응답") {
      value = Response(data = "/bz-agencies/1234-1234-1234-1234/id.png")
    }
  }
}

fun agencyFilePreviewResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<String>> {
    example("파일 경로 응답") {
      value = Response(data = "https://bz-agencies/1234-1234-1234-1234/id.png")
    }
  }
}