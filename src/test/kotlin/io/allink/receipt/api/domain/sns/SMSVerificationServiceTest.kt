package io.allink.receipt.api.domain.sns

import io.allink.io.allink.receipt.admin.config.TestConfigLoader.loadTestConfig
import io.allink.receipt.api.util.DateUtil
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class SMSVerificationServiceTest {

  @Test
  fun `sendSms should return messageId when SNS publish is successful`() = runBlocking {
    // Arrange
    val phoneNumber = "+8201076042046"
    val message = "123456"
    val config = loadTestConfig()
    val smsVerificationServiceImpl = SMSVerificationServiceImpl(config!!)
    smsVerificationServiceImpl.sendVerificationMessage(phoneNumber, message, DateUtil.nowLocalDateTime().plusMinutes(5).format(
      java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    ))
  }

}