package io.allink.receipt.api.domain.merchant.batch

import io.allink.receipt.api.util.DateUtil
import io.retable.Retable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutRequest
import software.amazon.awssdk.services.dynamodb.model.WriteRequest
import java.io.File
import java.util.*

/**
 * Package: io.allink.receipt.api.domain.tag
 * Created: Devonshin
 * Date: 18/04/2025
 */

suspend fun insertTagsFromExcelToDynamoDb(
  filePath: String,
  dynamoDbClient: DynamoDbClient
) {
  readExcelAndProcess(filePath) { tagIds ->
    val tagModels = createTagModels(tagIds)
    batchInsertTags(dynamoDbClient, tagModels)
    println("Inserted ${tagModels.size} tags")
  }
}

suspend fun readExcelAndProcess(filePath: String, processTagIds: (List<TagBatch>) -> Unit) {
  val tagIds = readTagIdsFromExcel(filePath)
  processTagIds(tagIds)
}

fun createTagModels(tagBatches: List<TagBatch>): List<TagModel> {
  val currentDate = DateUtil.nowLocalDateTime()
  return tagBatches.map { tagBatch ->
    TagModel(
      name = tagBatch.tagName ?: tagBatch.tagId,
      tagId = tagBatch.tagId,
      status = TagStatus.NORMAL,
      storeUid = tagBatch.storeUid,
      deviceId = tagBatch.deviceId,
      regDate = currentDate
    )
  }
}

fun batchInsertTags(dynamoDbClient: DynamoDbClient, tags: List<TagModel>) {
  dynamoDbClient.use {
    val batchSize = 25
    val regDate = DateUtil.nowLocalDateTimeStrMs()
    tags.chunked(batchSize).forEach { chunk ->
      println("Inserted ${chunk.size} tags")
      val putRequests = chunk.map { tag ->
        WriteRequest.builder()
          .putRequest(
            PutRequest.builder()
              .item(
                mapOf(
                  "uid" to AttributeValue.builder().s(UUID.randomUUID().toString()).build(),
                  "tagId" to AttributeValue.builder().s(tag.tagId).build(),
                  "tagName" to AttributeValue.builder().s(tag.name).build(),
                  "storeUid" to AttributeValue.builder().s(tag.storeUid).build(),
                  "deviceId" to AttributeValue.builder().s(tag.deviceId).build(),
                  "regDate" to AttributeValue.builder().s(regDate).build(),
                  "status" to AttributeValue.builder().s(TagStatus.NORMAL.name).build()
                )
              ).build()
          ).build()
      }

      val batchRequest = BatchWriteItemRequest.builder()
        .requestItems(mapOf("tags" to putRequests))
        .build()

      dynamoDbClient
        .batchWriteItem(batchRequest)
        .also { response ->
          if (response.hasUnprocessedItems() && response.unprocessedItems().isNotEmpty()) {
            println("Some items were not processed: ${response.unprocessedItems()}")
          }
        }
    }
  }
}

private suspend fun readTagIdsFromExcel(filePath: String): List<TagBatch> {
  return withContext(Dispatchers.IO) {
    File(filePath).inputStream().use { inputStream ->
      Retable.excel()
        .read(inputStream)
        .records
        .map { record ->
          TagBatch(
            record["TAG_ID"]!!,
            record["TAG_NAME"],
            record["DEVICE_ID"],
            record["STORE_ID"]
          )
        }
        .toList()
    }
  }
}

data class TagBatch(
  val tagId: String,
  val tagName: String?,
  val storeUid: String?,
  val deviceId: String?
)
