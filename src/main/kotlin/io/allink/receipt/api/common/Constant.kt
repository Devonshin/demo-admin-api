package io.allink.receipt.api.common

import io.ktor.http.*

/**
 * Package: io.allink.receipt.admin.common
 * Created: Devonshin
 * Date: 15/04/2025
 */

class Constant {
  companion object {
    const val AES256_KEY = "rWsq73mYW5WC8KTtzDyj1OUnQ4ubGlMU"
    val ACCEPT_FILE_FIELDS = mapOf(
      "stores" to listOf("bz", "id", "bank", "application", "coupon"),
      "bz-agencies" to listOf("bz", "id", "bank", "application"),
      "tags" to listOf("bz", "id", "bank", "application"),
      "advertisement" to listOf("banner"),
    )
    val ACCEPT_FILE_TYPE = listOf(
      ContentType.Image.PNG.contentSubtype,
      ContentType.Application.Pdf.contentSubtype,
      ContentType.Application.Xlsx.contentSubtype
    )

    fun checkAcceptFileField(menu: String, field: String): Boolean {
      return ACCEPT_FILE_FIELDS.containsKey(menu) && ACCEPT_FILE_FIELDS[menu]?.contains(field) == true
    }

    fun checkAcceptFileType(fileName: String): Boolean {
      return ACCEPT_FILE_TYPE.any { fileName.endsWith(it.toString(), true) }
    }
  }
}