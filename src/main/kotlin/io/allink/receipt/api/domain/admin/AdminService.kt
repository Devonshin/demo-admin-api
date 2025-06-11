package io.allink.receipt.api.domain.admin

import java.util.*

interface AdminService {

  suspend fun findByPhoneNo(phoneNumber: String): AdminModel?
  suspend fun findByUserUuId(userUuid: UUID): AdminModel?

}
