package io.allink.receipt.api.domain.tag

import io.allink.receipt.api.util.DateUtil
import io.allink.receipt.api.util.DateUtil.Companion.nowLocalDateTimeStrMs
import io.retable.Retable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest
import software.amazon.awssdk.services.dynamodb.model.PutRequest
import software.amazon.awssdk.services.dynamodb.model.WriteRequest
import java.io.File
import java.util.UUID

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
    batchInsertTags(dynamoDbClient,  tagModels)
    println("Inserted ${tagModels.size} tags")
  }
}

fun createTagModels(tagIds: List<String>): List<TagModel> {
  val currentDate = DateUtil.nowLocalDateTime()
  return tagIds.map { tagId ->
    TagModel(
      name = tagId,
      tagId = tagId,
      status = TagStatus.NORMAL,
      storeUid = null,
      regDate = currentDate
    )
  }
}


fun batchInsertTags(dynamoDbClient: DynamoDbClient, tags: List<TagModel>) {
  dynamoDbClient.use {
    val batchSize = 25
    val regDate = nowLocalDateTimeStrMs()
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
                  "tagName" to AttributeValue.builder().s(tag.tagId).build(),
                  "regDate" to AttributeValue.builder().s(regDate).build(),
                  "status" to AttributeValue.builder().s(TagStatus.NORMAL.name).build()
                )
              ).build()
          ).build()
      }

      val batchRequest = BatchWriteItemRequest.builder()
        .requestItems(mapOf("tags" to putRequests))
        .build()

      dynamoDbClient.batchWriteItem(batchRequest).also { response ->
        if (response.hasUnprocessedItems() && response.unprocessedItems().isNotEmpty()) {
          println("Some items were not processed: ${response.unprocessedItems()}")
        }
      }
    }
  }
}

suspend fun readExcelAndProcess(filePath: String, processTagIds: (List<String>) -> Unit) {
    val tagIds = readTagIdsFromExcel(filePath)
    processTagIds(tagIds)
}

private suspend fun readTagIdsFromExcel(filePath: String): List<String> {
    return withContext(Dispatchers.IO) {
      File(filePath).inputStream().use { inputStream ->
        Retable.excel().read(inputStream)
          .records
          .mapNotNull { record -> record["UID"] }
          .toList()
      }
    }
}