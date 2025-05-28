package io.allink.receipt.api.domain.merchant

import io.allink.receipt.api.domain.PagedResult
import io.allink.receipt.api.domain.merchant.batch.TagStatus
import io.allink.receipt.api.domain.store.StoreService
import io.allink.receipt.api.util.DateUtil
import io.allink.receipt.api.util.DateUtil.Companion.nowLocalDateTimeStrMs
import io.ktor.server.plugins.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import java.util.*


class MerchantTagServiceImpl(
  val merchantTagRepository: MerchantTagRepository,
  val storeService: StoreService,
  val dynamoDbClient: DynamoDbClient
) : MerchantTagService {

  override suspend fun getTags(merchantTagFilter: MerchantTagFilter): PagedResult<SimpleMerchantTagModel> {
    return merchantTagRepository.findAll(merchantTagFilter)
  }

  override suspend fun modifyTag(modify: MerchantTagModifyModel, userUuid: UUID): MerchantTagModel {

    val tagModel = merchantTagRepository.findForUpdate(modify.id)
    val store = if (modify.storeId != null) {
      storeService.findStore(modify.storeId) ?: throw NotFoundException("No store found for id ${modify.storeId}")
    } else {
      null
    }
    val now = DateUtil.nowLocalDateTime()
    newSuspendedTransaction {
      try {
        if (tagModel != null) {
          merchantTagRepository.update(
            tagModel.copy(
              merchantStoreId = store?.id,
              storeUid = store?.id,
              tagName = modify.name ?: "어드민 등록 태그",
              merchantGroupId = store?.franchiseCode,
              deviceId = modify.deviceId,
              modBy = userUuid,
              modDate = now
            )
          )
        } else {
          merchantTagRepository.create(
            MerchantTagModel(
              id = modify.id,
              merchantStoreId = store?.id,
              storeUid = store?.id,
              tagName = modify.name ?: "어드민 등록 태그",
              deviceId = modify.deviceId,
              merchantGroupId = store?.franchiseCode,
              regBy = userUuid,
              regDate = now,
            )
          )
        }

        val queryRequest = QueryRequest.builder()
          .tableName("tags")
          .indexName("tagId-index")
          .keyConditionExpression("tagId = :tagId")
          .expressionAttributeValues(
            mapOf(":tagId" to AttributeValue.builder().s(modify.id).build())
          )
          .build()
        dynamoDbClient.query(queryRequest).items().forEach { item ->
          dynamoDbClient.deleteItem {
            it.tableName("tags")
              .key(
                mapOf(
                  "uid" to AttributeValue.builder().s(item["uid"]?.s()).build()
                )
              )
          }
          println("Deleted item with tagId: ${item["tagId"]?.s()}")
        }

        dynamoDbClient.putItem {
          it.tableName("tags")
            .item(
              mapOf(
                "uid" to AttributeValue.builder().s(modify.id).build(),
                "tagId" to AttributeValue.builder().s(modify.id).build(),
                "storeUid" to AttributeValue.builder().s(store?.id).build(),
                "tagName" to AttributeValue.builder().s(modify.name).build(),
                "status" to AttributeValue.builder().s(TagStatus.NORMAL.name).build(),
                "regDate" to AttributeValue.builder().s(nowLocalDateTimeStrMs()).build(),
                "regUserId" to AttributeValue.builder().s(userUuid.toString()).build(),
              )
            )
        }
      } catch (e: Exception) {
        throw e
      }
    }
    return getTag(modify.id)
  }

  override suspend fun getTag(tagId: String): MerchantTagModel {
    if (tagId.isEmpty()) throw BadRequestException("No tag id provided")
    return merchantTagRepository.find(tagId) ?: throw NotFoundException("No tag found for id $tagId")
  }



}
