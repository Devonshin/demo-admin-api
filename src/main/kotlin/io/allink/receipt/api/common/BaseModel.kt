package io.allink.receipt.api.common

import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode
import kotlinx.serialization.Serializable

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
