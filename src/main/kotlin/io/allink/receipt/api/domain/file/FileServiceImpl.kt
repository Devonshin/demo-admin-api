package io.allink.receipt.api.domain.file

import io.ktor.server.config.*
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration

/**
 * Package: io.allink.receipt.api.domain.file
 * Created: Devonshin
 * Date: 23/05/2025
 */

class FileServiceImpl(
  private val s3Client: S3Client,
  private val s3Presigner: S3Presigner,
  config: ApplicationConfig
) : FileService {

  val bucketName = config.property("aws.s3.bucketName").getString()
  override suspend fun uploadFile(
    fileName: String,
    fileBytes: ByteArray
  ) {
    try {
      val request = PutObjectRequest.builder()
        .bucket(bucketName)
        .key(fileName)
        .contentType("application/octet-stream")
        .build()
      s3Client.putObject(request, RequestBody.fromBytes(fileBytes))
    } catch (e: Exception) {
      throw RuntimeException("S3 업로드 실패: ${e.message}", e)
    }
  }

  override suspend fun generatePresignedUrl(fileUrl: String, expiryDuration: Long): String {
    val presignRequest = GetObjectPresignRequest.builder()
      .signatureDuration(Duration.ofMinutes(expiryDuration))
      .getObjectRequest(
        GetObjectRequest.builder()
          .bucket(bucketName)
          .key(fileUrl)
          .build()
      )
      .build()

    // Presigned URL 생성
    val presignedUrl = s3Presigner.presignGetObject(presignRequest).url().toString()
    return presignedUrl
  }

}