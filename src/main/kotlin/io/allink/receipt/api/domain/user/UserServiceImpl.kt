package io.allink.receipt.api.domain.user

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.repository.TransactionUtil

/**
 * Package: io.allink.receipt.admin.domain.user
 * Created: Devonshin
 * Date: 15/04/2025
 */

class UserServiceImpl(
  private val userRepository: UserRepository
): UserService {

  override suspend fun findAllUser(filter: UserFilter): PagedResult<UserModel> = TransactionUtil.withTransaction {
    userRepository.findAll(filter)
  }

  override suspend fun findUser(id: String): UserModel? = TransactionUtil.withTransaction {
    userRepository.find(id)
  }

}