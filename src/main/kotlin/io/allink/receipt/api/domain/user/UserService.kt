package io.allink.receipt.api.domain.user

import io.allink.receipt.api.common.PagedResult

/**
 * Package: io.allink.receipt.admin.domain.user
 * Created: Devonshin
 * Date: 15/04/2025
 */

interface UserService {

  suspend fun findUser(id: String): UserModel?
  suspend fun findAllUser(filter: UserFilter): PagedResult<UserModel>

}