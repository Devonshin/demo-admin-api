package io.allink.receipt.api.domain.receipt.edoc

import io.allink.receipt.api.domain.BaseModel
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Serializable

/**
 * Package: io.allink.receipt.api.domain.receipt.edoc
 * Created: Devonshin
 * Date: 20/04/2025
 */


@Serializable @Schema(name = "EdocModel", title = "전자문서", description = "전자문서 발송 정보")
class EdocModel(
  @param:Schema(
    title = "전자문서 발송기관",
    description = "전자문서 발송기관 코드",
    requiredMode = RequiredMode.REQUIRED,
    example = "kakao|naver"
  )
  val sender: String,
  @param:Schema(
    title = "사용자 영수증 고유아이디",
    description = "사용자의 영수증 고유아이디",
    requiredMode = RequiredMode.REQUIRED,
    example = "3a931370-cd0b-4427-bf38-418111969c22"
  )
  override var id: String?,
  /*partnerReqUuid*/
  @param:Schema(
    title = "영수증 고유아이디",
    description = "영수증 고유아이디",
    requiredMode = RequiredMode.REQUIRED,
    example = "1234faa-cd0b-4427-bf38-418111969c22"
  )
  val receiptUuid: String,
  @param:Schema(title = "전자문서 아이디", description = "전자문서 고유아이디", requiredMode = RequiredMode.REQUIRED)
  val envelopId: String,
  @param:Schema(title = "전송 결과 코드", description = "전송 결과 코드", requiredMode = RequiredMode.REQUIRED)
  val responseCode: String,
  @param:Schema(title = "발송일시", description = "전자문서 발송 요청일시", requiredMode = RequiredMode.REQUIRED)
  val regDate: String,
  @param:Schema(title = "사용자 고유이이디", description = "영수증 사용자 고유아이디", requiredMode = RequiredMode.REQUIRED)
  val userId: String,
) : BaseModel<String>
