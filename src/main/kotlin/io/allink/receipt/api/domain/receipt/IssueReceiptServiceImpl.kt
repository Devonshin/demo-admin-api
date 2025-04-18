package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.common.PagedResult

/**
 * Package: io.allink.receipt.api.domain.receipt
 * Created: Devonshin
 * Date: 18/04/2025
 */

class IssueReceiptServiceImpl(
  val issueReceiptRepository: IssueReceiptRepository
) : IssueReceiptService {
  override suspend fun findAllReceipt(filter: ReceiptFilter): PagedResult<SimpleIssueReceiptModel> {
    return issueReceiptRepository.findAll(filter)
  }
}