package io.allink.receipt.api.config.plugin

import StoreServiceImpl
import com.typesafe.config.ConfigFactory
import io.allink.receipt.api.domain.admin.*
import io.allink.receipt.api.domain.agency.bz.*
import io.allink.receipt.api.domain.code.ServiceCodeRepository
import io.allink.receipt.api.domain.code.ServiceCodeRepositoryImpl
import io.allink.receipt.api.domain.code.ServiceCodeTable
import io.allink.receipt.api.domain.file.FileService
import io.allink.receipt.api.domain.file.FileServiceImpl
import io.allink.receipt.api.domain.koces.KocesGatewayConfig
import io.allink.receipt.api.domain.koces.KocesService
import io.allink.receipt.api.domain.koces.KocesServiceImpl
import io.allink.receipt.api.domain.login.*
import io.allink.receipt.api.domain.merchant.*
import io.allink.receipt.api.domain.npoint.*
import io.allink.receipt.api.domain.receipt.*
import io.allink.receipt.api.domain.sns.SMSVerificationServiceImpl
import io.allink.receipt.api.domain.sns.VerificationService
import io.allink.receipt.api.domain.store.*
import io.allink.receipt.api.domain.store.npoint.*
import io.allink.receipt.api.domain.user.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

fun Application.configureFrameworks() {
  install(Koin) {
    modules(module {
      /**
       * Configuration
       * */
      single<ApplicationConfig> {
        val env = System.getenv("KTOR_ENV") ?: "test"
        println("KTOR_ENV: $env")
        val baseConfig = ConfigFactory.load("application.conf")
        val envConfig = ConfigFactory.load("application-$env.conf")
        HoconApplicationConfig(envConfig.withFallback(baseConfig))
      }

      single<KocesGatewayConfig> {
        KocesGatewayConfig(get())
      }

      /**
       * Repository
       * */
      single<DynamoDbClient> {
        configureAwsDynamoDb(get())
      }
      single<S3Client> {
        s3Client(get())
      }
      single<S3Presigner> {
        s3Presigner(get())
      }
      single<AdminRepository> {
        AdminRepositoryImpl(AdminTable)
      }
      single<LoginInfoRepository> {
        LoginInfoRepositoryImpl(LoginInfoTable)
      }
      single<UserRepository> {
        UserRepositoryImpl(UserTable)
      }
      single<StoreRepository> {
        StoreRepositoryImpl(StoreTable)
      }
      single<ServiceCodeRepository> {
        ServiceCodeRepositoryImpl(ServiceCodeTable)
      }
      single<IssueReceiptRepository> {
        IssueReceiptRepositoryImpl(IssueReceiptTable)
      }
      single<MerchantTagRepository> {
        MerchantTagRepositoryImpl(MerchantTagTable)
      }
      single<NPointRepository> {
        NPointRepositoryImpl(NPointWaitingTable)
      }
      single<BzAgencyRepository> {
        BzAgencyRepositoryImpl(BzAgencyTable)
      }
      single<NPointStoreRepository> {
        NPointStoreRepositoryImpl(NPointStoreTable)
      }
      single<NPointStoreServiceRepository> {
        NPointStoreServiceRepositoryImpl(NPointStoreServiceTable)
      }
      single<StoreBillingRepository> {
        StoreBillingRepositoryImpl(StoreBillingTable)
      }
      single<StoreBillingTokenRepository> {
        StoreBillingTokenRepositoryImpl(StoreBillingTokenTable)
      }
      /**
       * Services
       * */
      single<KocesService> {
        KocesServiceImpl(get<KocesGatewayConfig>().createHttpClient(), get())
      }
      single<AdminService> {
        AdminServiceImpl(get())
      }
      single<VerificationService> {
        SMSVerificationServiceImpl(get())
      }
      single<LoginService> {
        LoginServiceImpl(get(), get(), get(), get())
      }
      single<UserService> {
        UserServiceImpl(get())
      }
      single<StoreService> {
        StoreServiceImpl(get(), get(), get(), get(), get())
      }
      single<IssueReceiptService> {
        IssueReceiptServiceImpl(get())
      }
      single<MerchantTagService> {
        MerchantTagServiceImpl(get(), get(), get())
      }
      single<NPointService> {
        NPointServiceImpl(get())
      }
      single<NPointStoreServiceService> {
        NPointStoreServiceServiceImpl(get(), get())
      }
      single<StoreBillingService> {
        StoreBillingServiceImpl(get(), get(), get(), get())
      }
      single<BzAgencyService> {
        BzAgencyServiceImpl(get(), get())
      }
      single<FileService> {
        FileServiceImpl(get(), get(), get())
      }
    })
  }
}
