package io.allink.receipt.api.domain

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

/**
 * Package: io.allink.receipt.admin.common
 * Created: Devonshin
 * Date: 13/04/2025
 */

interface BaseModel<T> {
  var id: T?
}


@Serializable
@Schema(title = "목록 결과 객체", description = "목록 결과 객체")
data class PagedResult<T>(
  @Schema(title = "조회 데이터")
  val items: List<T>,
  @Schema(title = "전체 건수")
  val totalCount: Int,
  @Schema(title = "현재 페이지 번호")
  val currentPage: Int,
  @Schema(title = "전체 페이지 번호")
  val totalPages: Int
)

@Serializable
@Schema(title = "사용자 목록 정렬 지정 객체", description = "사용자 목록 정렬 지정 객체")
data class Sorter(
  @Schema(title = "정렬 대상", description = "정렬에 사용하고자 하는 필드명", example = "id")
  val field: String,
  @Schema(title = "정렬 순서", description = "ASC, DESC", allowableValues = ["ASC", "DESC"], example = "ASC")
  val direction: String
)


@Serializable
@Schema(title = "요청 페이징 정보")
data class Page(
  @Schema(title = "페이지 번호", description = "1 에서 무한대", example = "15", requiredMode = RequiredMode.REQUIRED)
  val page: Int = 1,
  @Schema(title = "페이지 길이", description = "최대 값 100", example = "10", requiredMode = RequiredMode.REQUIRED)
  val pageSize: Int = 10
)

@Serializable
@Schema(title = "검색 기간", description = "검색할 기간의 시작과 끝의 범위", example = """{"from: "2025-04-17T12:00:00", "to: "2025-05-17T12:00:00"}""", requiredMode = RequiredMode.REQUIRED)
data class PeriodFilter(
  @Schema(title = "시작일시", description = "검색을 시작할 년월일시", example = "2025-04-17T12:00:00", requiredMode = RequiredMode.REQUIRED)
  val from: @Contextual LocalDateTime,
  @Schema(title = "종료일시", description = "검색을 종료할 년월일시", example = "2025-04-17T12:00:00", requiredMode = RequiredMode.REQUIRED)
  val to: @Contextual LocalDateTime
)

