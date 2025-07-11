package io.allink.receipt.api.common

import io.ktor.http.*
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

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
    const val MERCHANT_SERVICE_GROUP_CODE = "MERT_SVC"
    const val ERECEIPT = "ERECEIPT" //전자영수증
    const val REVIEWPRJ = "REVIEWPRJ" //999 리뷰 프로젝트
    const val REVIEWPT = "REVIEWPT" //리뷰 리워드
    const val DLVRVIEWPT = "DLVRVIEWPT" //배달 리뷰 리워드
    const val CPNADVTZ = "CPNADVTZ" //쿠폰 광고
    const val HUBADVTZ = "HUBADVTZ" //허브 광고

    val SYSTEM_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
  }
}

@Schema(
  title = "상태코드",
  description = "상태값",
  example = "NORMAL,ACTIVE: 정상, INACTIVE: 중지, EXPIRED: 만료, PENDING: 대기, DELETED: 삭제"
)
enum class StatusCode(
  val value: String
) {
  ACTIVE("정상"),
  NORMAL("정상"),
  INACTIVE("중지"),
  PENDING("대기"),
  EXPIRED("만료"),
  DELETED("삭제")
}

@Schema(
  title = "상태코드",
  description = "상태값: PENDING 시 결제 시도. FAIL, COMPLETE는 시스템만 할당 가능. ",
  example = "FAIL: 결제실패, COMPLETE: 결제완료, PENDING: 익월 1일까지 대기, STANDBY: 즉시 결제 대기, CANCELED: 결제취소"
)
enum class BillingStatusCode(
  val value: String
) {
  STANDBY("즉시 결제 대기"),
  PENDING("익월 1일까지 대기"),
  COMPLETE("결제완료"),
  CANCELED("결제취소"),
  FAIL("결제실패"),
}