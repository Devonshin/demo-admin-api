package io.allink.receipt.api.util

import org.slf4j.Logger
import java.nio.charset.StandardCharsets
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Package: io.allink.receipt.admin.util
 * Created: Devonshin
 * Date: 15/04/2025
 */

class AES256Util {
  companion object {
    val logger: Logger = org.slf4j.LoggerFactory.getLogger(this.javaClass.name)

    const val ENCRYPTION_ALGORITHM: String = "AES"
    const val TRANSFORMATION: String = "AES/CBC/PKCS5Padding"

    @Throws(
      NoSuchPaddingException::class,
      NoSuchAlgorithmException::class,
      InvalidAlgorithmParameterException::class,
      InvalidKeyException::class,
      IllegalBlockSizeException::class,
      BadPaddingException::class
    )
    fun encrypt(plainText: String?, encKey: String): String? {
      if (plainText == null || plainText.isEmpty()) return null
      val keyData = encKey.toByteArray()
      val secureKey = SecretKeySpec(keyData, ENCRYPTION_ALGORITHM)
      val ivBytes = ByteArray(16)
      val iv = IvParameterSpec(ivBytes)
      val cipher = Cipher.getInstance(TRANSFORMATION)
      cipher.init(Cipher.ENCRYPT_MODE, secureKey, iv)
      val encryptedBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
      return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    @Throws(
      NoSuchPaddingException::class,
      NoSuchAlgorithmException::class,
      InvalidAlgorithmParameterException::class,
      InvalidKeyException::class,
      IllegalBlockSizeException::class,
      BadPaddingException::class
    )
    fun decrypt(encryptedText: String?, encKey: String): String? {
      if (encryptedText == null || encryptedText.isEmpty()) return null
      val cipher = Cipher.getInstance(TRANSFORMATION)
      val keyData = encKey.toByteArray()
      val secureKey = SecretKeySpec(keyData, ENCRYPTION_ALGORITHM)
      val ivBytes = ByteArray(16)
      val iv = IvParameterSpec(ivBytes)
      cipher.init(Cipher.DECRYPT_MODE, secureKey, iv)
      val decodedBytes = Base64.getDecoder().decode(encryptedText.replace("\n", "").replace("\r", ""))
      val originalBytes = cipher.doFinal(decodedBytes)
      return String(originalBytes, StandardCharsets.UTF_8)
    }
  }
}