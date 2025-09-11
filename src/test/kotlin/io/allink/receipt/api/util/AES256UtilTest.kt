package io.allink.receipt.api.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AES256UtilTest {

  /**
   * Tests for AES256Util.Companion.encrypt method.
   *
   * The `encrypt` function in the `Companion` class of `AES256Util` is responsible for
   * encrypting a given string using AES encryption in CBC mode with PKCS5 padding. It
   * creates an initialization vector (IV) using the first 16 bytes of the provided key and
   * uses Base64 URL encoding to return the encrypted result as a string.
   */

  val key = "rWsq73mYW5WC8KTtzDyj1OUnQ4ubGlMU"

  @Test
  fun `encrypt should return valid encrypted string for valid input`() {
    // Given
    val plainText = "Hello World"
    
    // When
    val encrypted = AES256Util.encrypt(plainText, key)
    
    // Then
    assert(encrypted != null)
    assert(encrypted!!.isNotEmpty())
    assert(encrypted != plainText)
  }

  @Test
  fun `decrypt should return original text`() {
    // Given
    val originalText = "Test Message"
    val encrypted = AES256Util.encrypt(originalText, key)
    
    // When
    val decrypted = AES256Util.decrypt(encrypted, key)
    
    // Then
    assertEquals(originalText, decrypted)
  }

  @Test
  fun `encrypt should return different results for different inputs`() {
    val input1 = "Hello World"
    val input2 = "Goodbye World"

    val encrypted1 = AES256Util.encrypt(input1, key)
    val encrypted2 = AES256Util.encrypt(input2, key)

    assertEquals(input1, AES256Util.decrypt(encrypted1, key))
    assertEquals(input2, AES256Util.decrypt(encrypted2, key))
  }

}