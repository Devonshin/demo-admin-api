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
    val input = "DrVDOB/IAWg2LiwRHTOi+Q=="
    val decrypted = AES256Util.decrypt(input, key)
    println(",$decrypted,")
//    assertEquals(input, decrypted)
  }
  @Test
  fun `decrypt should return valid input`() {
    val input = "pbUZu0Jq/VhD5UPtrA/Tq8Uz85bnnFxv+sb+h0ymlxYyxez8Ic8JHCaTsomdS/197GkgJssucmlBB8PS3ukK8RLabPQCJXTwZzek7RUlbQ6tZ6z5mZ4Q+W1b28c/hzEu"
    val decrypted = AES256Util.decrypt(input, key)
    println(",$decrypted,")
//    assertEquals(input, decrypted)
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