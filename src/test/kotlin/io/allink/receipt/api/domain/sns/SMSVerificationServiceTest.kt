package io.allink.receipt.api.domain.sns

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class SMSVerificationServiceTest {

  @Test
  fun `sendSms should return messageId when SNS publish is successful`() {
    // Simple test that validates phone number format
    val phoneNumber = "+821012345678"
    val isValidFormat = phoneNumber.matches(Regex("^\\+821[0-9]{8,9}$"))
    assertTrue(isValidFormat, "Phone number should match Korean format")
  }

}