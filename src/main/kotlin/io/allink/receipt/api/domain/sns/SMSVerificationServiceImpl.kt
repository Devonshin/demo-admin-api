package io.allink.receipt.api.domain.sns

import com.typesafe.config.ConfigFactory
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest
import software.amazon.awssdk.services.sns.model.PublishResponse

/**
 * Package: io.allink.receipt.admin.domain.sns
 * Created: Devonshin
 * Date: 13/04/2025
 */

class SMSVerificationServiceImpl : VerificationService {

  private val config = ConfigFactory.load()
  private val snsClient: SnsClient = SnsClient.builder()
    .credentialsProvider {
      AwsBasicCredentials.create(
        config.getString("aws.accessKeyId"),
        config.getString("aws.secretKey")
      )
    }
    .region(Region.AP_NORTHEAST_1)
    .build()


  override suspend fun sendVerificationMessage(to: String, message: String, expired: String) {
    val template = config.getString("aws.smsTemplate")
    val replaced = template.replace("{code}", message).replace("{exp_date}", expired)
    sendSms(to, replaced)
  }

  /**
   * 문자를 보내는 함수
   * @param phoneNumber 수신자의 E.164 형식 전화번호
   * @param message 문자 메시지 내용 (최대 1600자)
   * @return 메시지 전송 ID 반환
   */
  fun sendSms(phoneNumber: String, message: String): String {
    val request: PublishRequest = PublishRequest.builder()
      .messageAttributes(
        mapOf()
      )
      .message(message)
      .phoneNumber("+82$phoneNumber")
      .build()

    // SNS 메시지 전송
    val response: PublishResponse = snsClient.publish(request)
    return response.messageId()
  }


}