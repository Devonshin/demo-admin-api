package io.allink.receipt.api.domain.agency.bz

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.Response
import io.allink.receipt.api.domain.Sorter
import io.allink.receipt.api.domain.admin.AdminStatus
import io.github.smiley4.ktoropenapi.config.ResponseConfig
import io.github.smiley4.ktoropenapi.config.SimpleBodyConfig
import java.time.LocalDateTime
import java.util.*

/**
 * Package: io.allink.receipt.api.common
 * Created: Devonshin
 * Date: 22/05/2025
 */

fun agencyListRequest(): SimpleBodyConfig.() -> Unit = {
  description = "대리점 목록 조회 요청"
  required = true
  example("agency-list-request") {
    value = BzAgencyFilter(
      id = "c7f0d23e-eceb-4434-b489-668c0b61a7f9",
      businessNo = "123-45-67890",
      agencyName = "대리점명",
      status = AgencyStatus.ACTIVE,
      sort = listOf(
        Sorter("agencyName", "ASC")
      )
    )
  }
}

fun agencyListResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<PagedResult<BzListAgencyModel>>> {
    example("대리점 목록 응답") {
      value = Response(
        data = PagedResult(
          items = listOf(
            BzListAgencyModel(
              id = UUID.fromString("c7f0d23e-eceb-4434-b489-668c0b61a7f9"),
              agencyName = "대리점명",
              businessNo = "123-45-67890",
              status = AgencyStatus.ACTIVE,
              latestLoginAt = LocalDateTime.now()
            )
          ),
          totalPages = 1,
          totalCount = 1,
          currentPage = 1
        )
      )
    }
  }
}

fun agencyDetailResponse(): ResponseConfig.() -> Unit = {
  description = "성공 응답"
  body<Response<BzAgencyModel>> {
    example("대리점 상세 정보 응답") {
      value = Response(
        data = BzAgencyModel(
          id = UUID.fromString("c7f0d23e-eceb-4434-b489-668c0b61a7f9"),
          agencyName = "대리점명",
          businessNo = "123-45-67890",
          addr1 = "서울시 강남구",
          addr2 = "테헤란로 123",
          tel = "02-123-4567",
          ceoName = "홍길동",
          ceoPhone = "010-1234-5678",
          staffs = listOf(
            BzAgencyAdminModel(
              id = UUID.fromString("c7f0d23e-eceb-4434-b489-668c0b61a7f9"),
              fullName = "<NAME>",
              phone = "010-1234-5678",
              email = "email@email.com",
              status = AdminStatus.ACTIVE,
              regBy = UUID.fromString("c7f0d23e-eceb-4434-b489-668c0b61a7f9"),
              regDate = LocalDateTime.now()
            )
          ),
          applicationFilePath = "/agencies/c7f0d23e-eceb-4434-b489-668c0b61a7f9/applicationFile.pdf",
          bzFilePath = "/agencies/c7f0d23e-eceb-4434-b489-668c0b61a7f9/bzFile.pdf",
          idFilePath = "/agencies/c7f0d23e-eceb-4434-b489-668c0b61a7f9/idFile.pdf",
          bankFilePath = null,
          isReceiptAlliance = true,
          infraRatio = 10,
          rewardBaseRatio = 20,
          rewardCommissionRatio = 30,
          rewardPackageRatio = 40,
          advertisementRatio = 50,
          isCouponAdv = true,
          couponAdvRatio = 60,
          tagDeposit = 100000,
          agencyDeposit = 1000000,
          settlementBank = "신한은행",
          bankAccountName = "홍길동",
          bankAccountNo = "123-456-789012",
          status = AgencyStatus.ACTIVE,
          regDate = LocalDateTime.now(),
          regBy = UUID.fromString("c7f0d23e-eceb-4434-b489-668c0b61a7f9"),
          modDate = LocalDateTime.now(),
          modBy = UUID.fromString("c7f0d23e-eceb-4434-b489-668c0b61a7f9")
        )
      )
    }
  }
}


fun agencyCreate(): SimpleBodyConfig.() -> Unit = {
  description = "대리점 생성"
  required = true
  example("대리점 생성") {
    value = BzAgencyModel(
      id = UUID.fromString("c7f0d23e-eceb-4434-b489-668c0b61a7f9"),
      agencyName = "대리점명",
      businessNo = "123-45-67890",
      addr1 = "서울시 강남구",
      addr2 = "테헤란로 123",
      tel = "02-123-4567",
      ceoName = "홍길동",
      ceoPhone = "010-1234-5678",
      staffs = listOf(
        BzAgencyAdminModel(
          id = UUID.fromString("c7f0d23e-eceb-4434-b489-668c0b61a7f9"),
          fullName = "<NAME>",
          phone = "010-1234-5678",
          email = "email@email.com",
          status = AdminStatus.ACTIVE
        )
      ),
      applicationFilePath = "/agencies/c7f0d23e-eceb-4434-b489-668c0b61a7f9/applicationFile.pdf",
      bzFilePath = "/agencies/c7f0d23e-eceb-4434-b489-668c0b61a7f9/bzFile.pdf",
      idFilePath = "/agencies/c7f0d23e-eceb-4434-b489-668c0b61a7f9/idFile.pdf",
      bankFilePath = null,
      isReceiptAlliance = true,
      infraRatio = 10,
      rewardBaseRatio = 20,
      rewardCommissionRatio = 30,
      rewardPackageRatio = 40,
      advertisementRatio = 50,
      isCouponAdv = false,
      couponAdvRatio = 60,
      tagDeposit = 100000,
      agencyDeposit = 1000000,
      settlementBank = "신한은행",
      bankAccountName = "홍길동",
      bankAccountNo = "123-456-789012",
      status = AgencyStatus.ACTIVE,
      regDate = null,
      regBy = null,
      modDate = null,
      modBy = null,
    )
  }
}
