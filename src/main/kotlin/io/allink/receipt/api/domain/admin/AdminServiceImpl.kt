package io.allink.receipt.api.domain.admin

import java.util.*

class AdminServiceImpl(
  private val adminRepository: AdminRepository
): AdminService {

  override suspend fun findByUserUuId(userUuid: UUID): AdminModel? {
    return adminRepository.findByUserUuid(userUuid)
  }
  override suspend fun findByPhoneNo(phoneNumber: String): AdminModel?{
    return adminRepository.findByPhone(phoneNumber)
  }

}
