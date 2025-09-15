package io.allink.receipt.api.domain.receipt

import io.allink.receipt.api.domain.advertisement.AdvertisementTable
import io.allink.receipt.api.domain.merchant.MerchantTagTable
import io.allink.receipt.api.domain.receipt.edoc.KakaoBillTable
import io.allink.receipt.api.domain.receipt.edoc.NaverBillTable
import io.allink.receipt.api.domain.store.StoreTable
import io.allink.receipt.api.domain.user.UserTable
import io.allink.receipt.api.domain.user.review.UserPointReviewTable
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.stringLiteral
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.select

/**
 * Package: io.allink.receipt.api.domain.receipt
 * Created: Devonshin
 * Date: 18/04/2025
 */

class IssueReceiptRepositoryImpl(
  override val table: IssueReceiptTable
) : IssueReceiptRepository {

  override suspend fun findByIdAndUserId(userId: String, receiptId: String): IssueReceiptModel? {

    var edoc = KakaoBillTable.select(
      KakaoBillTable.userId,
      KakaoBillTable.receiptUuid,
      KakaoBillTable.envelopId,
      KakaoBillTable.regDate,
      stringLiteral("kakao").alias("sender")
    ).where { KakaoBillTable.userId eq userId }
      .andWhere { KakaoBillTable.receiptUuid eq receiptId }
      .toList()
      .firstOrNull()?.let {
        SimpleEdocModel(
          id = it[stringLiteral("kakao")],
          envelopId = it[KakaoBillTable.envelopId],
          regDate = it[KakaoBillTable.regDate]
        )
      }
    if (edoc == null) {
      edoc = NaverBillTable.select(
        NaverBillTable.userId,
        NaverBillTable.receiptUuid,
        NaverBillTable.envelopId,
        NaverBillTable.regDate,
        stringLiteral("naver").alias("sender")
      ).where { NaverBillTable.userId eq userId }
        .andWhere { NaverBillTable.receiptUuid eq receiptId }
        .toList().firstOrNull()?.let {
          SimpleEdocModel(
            id = it[stringLiteral("naver")],
            envelopId = it[NaverBillTable.envelopId],
            regDate = it[NaverBillTable.regDate]
          )
        }
    }

    return table
      .join(
        MerchantTagTable,
        JoinType.LEFT,
        table.tagId,
        MerchantTagTable.id
      )
      .join(
        StoreTable,
        JoinType.LEFT,
        table.storeUid,
        StoreTable.id
      )
      .join(
        UserTable,
        JoinType.LEFT,
        table.userUid,
        UserTable.id
      )
      .join(
        UserPointReviewTable,
        JoinType.LEFT,
        table.id,
        UserPointReviewTable.id
      )
      .join(
        AdvertisementTable,
        JoinType.LEFT,
        table.advertisementId,
        AdvertisementTable.id
      )
      .select(
        table.id,
        table.storeUid,
        table.tagId,
        table.userUid,
        table.receiptType,
        table.receiptAmount,
        table.issueDate,
        table.originIssueId,
        MerchantTagTable.id,
        MerchantTagTable.deviceId,
        StoreTable.id,
        StoreTable.storeName,
        StoreTable.franchiseCode,
        StoreTable.businessNo,
        StoreTable.ceoName,
        UserTable.id,
        UserTable.name,
        UserPointReviewTable.id,
        UserPointReviewTable.status,
        AdvertisementTable.id,
        AdvertisementTable.title,
        AdvertisementTable.merchantGroupId,
      )
      .where {
        table.id eq receiptId
      }
      .andWhere {
        table.userUid eq userId
      }
      .toList().firstOrNull()?.let {
        toModel(it).apply { this.edoc = edoc }
      }
  }
}