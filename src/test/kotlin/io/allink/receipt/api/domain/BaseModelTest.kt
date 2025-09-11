package io.allink.receipt.api.domain

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import java.util.*

class BaseModelTest {

  // BaseModel을 상속한 테스트용 클래스
  data class TestModel(
    override var id: String? = null,
    val name: String = "test"
  ) : BaseModel<String>

  @Test
  fun `BaseModel should handle String ID correctly`() {
    // Given
    val testId = "test-id-123"
    val model = TestModel(id = testId, name = "Test Model")
    
    // When & Then
    assertEquals(testId, model.id)
    assertEquals("Test Model", model.name)
  }
  
  @Test
  fun `BaseModel should handle UUID ID correctly`() {
    // Given: UUID를 사용하는 모델
    data class UUIDModel(
      override var id: UUID? = null,
      val description: String = "uuid model"
    ) : BaseModel<UUID>
    
    val testUuid = UUID.randomUUID()
    val model = UUIDModel(id = testUuid, description = "UUID Test")
    
    // When & Then
    assertEquals(testUuid, model.id)
    assertEquals("UUID Test", model.description)
  }
  
  @Test
  fun `BaseModel should handle null ID correctly`() {
    // Given
    val model = TestModel(id = null, name = "Null ID Test")
    
    // When & Then
    assertEquals(null, model.id)
    assertEquals("Null ID Test", model.name)
  }
  
  @Test
  fun `BaseModel should support ID modification`() {
    // Given
    val model = TestModel(name = "Modifiable ID Test")
    
    // When
    model.id = "new-id-456"
    
    // Then
    assertEquals("new-id-456", model.id)
  }
  
  @Test
  fun `BaseModel should support data class operations`() {
    // Given
    val model1 = TestModel(id = "test-123", name = "Model 1")
    val model2 = TestModel(id = "test-123", name = "Model 1")
    val model3 = TestModel(id = "test-456", name = "Model 3")
    
    // When & Then: equals 및 hashCode 동작 검증
    assertEquals(model1, model2) // 같은 값을 가진 모델들은 equal
    assertEquals(model1.hashCode(), model2.hashCode()) // hashCode도 동일
    assertNotNull(model1.toString()) // toString 메서드 작동
    
    // copy 메서드 검증
    val copiedModel = model1.copy(name = "Copied Model")
    assertEquals("test-123", copiedModel.id)
    assertEquals("Copied Model", copiedModel.name)
  }
}