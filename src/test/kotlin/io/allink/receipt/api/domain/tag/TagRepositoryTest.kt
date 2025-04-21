package io.allink.io.allink.receipt.api.domain.tag

import io.allink.receipt.api.config.plugin.dynamoDbClient
import io.allink.receipt.api.domain.tag.insertTagsFromExcelToDynamoDb
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

/**
 * Package: io.allink.io.allink.receipt.api.domain.tag
 * Created: Devonshin
 * Date: 18/04/2025
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TagRepositoryTest {

  private lateinit var dynamoDbClient: DynamoDbClient

  @BeforeAll
  fun init() {
//    dynamoDbClient = localDynamoDbClient()
    dynamoDbClient = dynamoDbClient()
  }

  //태그 등록 리스트
/// Volumes/Workspaces/workspace/projects/Allink/app/e-receipt-api/upload/ DB등록_250417.xlsx",
// /Volumes/Workspaces/workspace/projects/Allink/app/e-receipt-api/upload/07-7. 1000(Non encoded)[39].xls,
// /Volumes/Workspaces/workspace/projects/Allink/app/e-receipt-api/upload/07-8. I CODE X39000 (Encoded)[6].xls,
  val tag39000 = "/Volumes/Workspaces/workspace/projects/Allink/app/e-receipt-api/upload/DB등록_250417.xlsx"
  val tag39000new =
    "/Volumes/Workspaces/workspace/projects/Allink/app/e-receipt-api/upload/07-7. 1000(Non encoded)[39].xls"
  val tag100new =
    "/Volumes/Workspaces/workspace/projects/Allink/app/e-receipt-api/upload/07-7. 1000(Non encoded)[39].xls"

  @Test
  fun `should batch insert tags without unprocessed items`() = testApplication {
    insertTagsFromExcelToDynamoDb(tag100new, dynamoDbClient)
  }
}