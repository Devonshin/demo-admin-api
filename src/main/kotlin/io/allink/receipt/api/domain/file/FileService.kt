package io.allink.receipt.api.domain.file

/**
 * Package: io.allink.receipt.api.domain.file
 * Created: Devonshin
 * Date: 23/05/2025
 */

interface FileService {

  suspend fun uploadFile(fileName: String, fileBytes: ByteArray)

  suspend fun generatePresignedUrl(fileUrl: String, expiryDuration: Long): String

}