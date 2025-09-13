package io.allink.receipt.api.domain.file

import io.ktor.server.config.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.net.URL

/**
 * @file FileServiceImplTest.kt
 * @brief S3 업로드/프리사인 URL 생성 로직 단위 테스트 (외부 호출은 Mock)
 * @author Devonshin
 * @date 2025-09-12
 */
class FileServiceImplTest {

  private fun config(): ApplicationConfig = MapApplicationConfig(
    "aws.s3.bucketName" to "test-bucket"
  )

  @Test
  fun `generatePresignedUrl should return url from presigner`() {
    // given
    val s3 = mockk<S3Client>(relaxed = true)
    val presigner = mockk<S3Presigner>()
    val service = FileServiceImpl(s3, presigner, config())

    val presigned = mockk<PresignedGetObjectRequest>()
    every { presigned.url() } returns URL("https://example.com/test-file.png")
every { presigner.presignGetObject(any<GetObjectPresignRequest>()) } returns presigned

    // when
val url = kotlinx.coroutines.runBlocking { service.generatePresignedUrl("path/to/file.png", 15) }

    // then
    assertEquals("https://example.com/test-file.png", url)
verify { presigner.presignGetObject(any<GetObjectPresignRequest>()) }
  }

  @Test
  fun `uploadFile should delegate to s3Client`() {
    // given
    val s3 = mockk<S3Client>()
    val presigner = mockk<S3Presigner>(relaxed = true)
    val service = FileServiceImpl(s3, presigner, config())

    every { s3.putObject(any<PutObjectRequest>(), any<RequestBody>()) } returns PutObjectResponse.builder().build()

    // when
    kotlinx.coroutines.runBlocking {
      service.uploadFile("dir/id/field.png", "hello".toByteArray())
    }

    // then
    verify { s3.putObject(any<PutObjectRequest>(), any<RequestBody>()) }
  }

  @Test
  fun `uploadFile should wrap exception as RuntimeException`() {
    // given
    val s3 = mockk<S3Client>()
    val presigner = mockk<S3Presigner>(relaxed = true)
    val service = FileServiceImpl(s3, presigner, config())

    every { s3.putObject(any<PutObjectRequest>(), any<RequestBody>()) } throws IllegalStateException("boom")

    // when - then
    val ex = assertThrows(RuntimeException::class.java) {
      kotlinx.coroutines.runBlocking {
        service.uploadFile("dir/id/field.png", ByteArray(0))
      }
    }
    assert(ex.message!!.contains("S3 업로드 실패"))
  }
}