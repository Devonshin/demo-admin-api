package io.allink.receipt.api.domain.user

import io.allink.receipt.api.common.PagedResult

/**
 * Package: io.allink.receipt.admin.domain.user
 * Created: Devonshin
 * Date: 15/04/2025
 */

class UserServiceImpl(
  private val userRepository: UserRepository
): UserService {

  override suspend fun findAllUser(filter: UserFilter): PagedResult<UserModel> {
    return userRepository.findAll(filter)
  }

  override suspend fun findUser(id: String): UserModel? {
    return userRepository.find(id)
  }

}