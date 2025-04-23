package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.domain.PagedResult

/**
 * Package: io.allink.receipt.api.domain.receipt
 * Created: Devonshin
 * Date: 18/04/2025
 */

interface IssueReceiptService {
  suspend fun findAllReceipt(filter: ReceiptFilter): PagedResult<SimpleIssueReceiptModel>

  suspend fun findReceipt(userId: String, receiptId: String): IssueReceiptModel?
}