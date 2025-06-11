package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.repository.TransactionUtil

/**
 * Package: io.allink.receipt.api.domain.receipt
 * Created: Devonshin
 * Date: 18/04/2025
 */

class IssueReceiptServiceImpl(
  val issueReceiptRepository: IssueReceiptRepository
) : IssueReceiptService {
  override suspend fun findAllReceipt(
    filter: ReceiptFilter
  ): PagedResult<SimpleIssueReceiptModel> = TransactionUtil.withTransaction {
    issueReceiptRepository.findAll(filter)
  }

  override suspend fun findReceipt(
    userId: String,
    receiptId: String
  ): IssueReceiptModel? = TransactionUtil.withTransaction {
    issueReceiptRepository.findByIdAndUserId(userId, receiptId)
  }
}