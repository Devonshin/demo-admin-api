package io.allink.receipt.api.config.plugin

import StoreServiceImpl
import com.typesafe.config.ConfigFactory
import io.allink.receipt.api.domain.admin.AdminRepository
import io.allink.receipt.api.domain.admin.AdminRepositoryImpl
import io.allink.receipt.api.domain.admin.AdminService
import io.allink.receipt.api.domain.admin.AdminServiceImpl
import io.allink.receipt.api.domain.admin.AdminTable
import io.allink.receipt.api.domain.agency.bz.BzAgencyRepository
import io.allink.receipt.api.domain.agency.bz.BzAgencyRepositoryImpl
import io.allink.receipt.api.domain.agency.bz.BzAgencyService
import io.allink.receipt.api.domain.agency.bz.BzAgencyServiceImpl
import io.allink.receipt.api.domain.agency.bz.BzAgencyTable
import io.allink.receipt.api.domain.code.ServiceCodeRepository
import io.allink.receipt.api.domain.code.ServiceCodeRepositoryImpl
import io.allink.receipt.api.domain.code.ServiceCodeTable
import io.allink.receipt.api.domain.file.FileService
import io.allink.receipt.api.domain.file.FileServiceImpl
import io.allink.receipt.api.domain.login.LoginInfoRepository
import io.allink.receipt.api.domain.login.LoginInfoRepositoryImpl
import io.allink.receipt.api.domain.login.LoginInfoTable
import io.allink.receipt.api.domain.login.LoginService
import io.allink.receipt.api.domain.login.LoginServiceImpl
import io.allink.receipt.api.domain.merchant.MerchantTagRepository
import io.allink.receipt.api.domain.merchant.MerchantTagRepositoryImpl
import io.allink.receipt.api.domain.merchant.MerchantTagService
import io.allink.receipt.api.domain.merchant.MerchantTagServiceImpl
import io.allink.receipt.api.domain.merchant.MerchantTagTable
import io.allink.receipt.api.domain.npoint.NPointRepository
import io.allink.receipt.api.domain.npoint.NPointRepositoryImpl
import io.allink.receipt.api.domain.npoint.NPointService
import io.allink.receipt.api.domain.npoint.NPointServiceImpl
import io.allink.receipt.api.domain.npoint.NPointWaitingTable
import io.allink.receipt.api.domain.receipt.IssueReceiptRepository
import io.allink.receipt.api.domain.receipt.IssueReceiptRepositoryImpl
import io.allink.receipt.api.domain.receipt.IssueReceiptService
import io.allink.receipt.api.domain.receipt.IssueReceiptServiceImpl
import io.allink.receipt.api.domain.receipt.IssueReceiptTable
import io.allink.receipt.api.domain.sns.SMSVerificationServiceImpl
import io.allink.receipt.api.domain.sns.VerificationService
import io.allink.receipt.api.domain.store.StoreRepository
import io.allink.receipt.api.domain.store.StoreRepositoryImpl
import io.allink.receipt.api.domain.store.StoreService
import io.allink.receipt.api.domain.store.StoreTable
import io.allink.receipt.api.domain.user.UserRepository
import io.allink.receipt.api.domain.user.UserRepositoryImpl
import io.allink.receipt.api.domain.user.UserService
import io.allink.receipt.api.domain.user.UserServiceImpl
import io.allink.receipt.api.domain.user.UserTable
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.config.HoconApplicationConfig
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

fun Application.configureFrameworks() {
  install(Koin) {
    modules(module {
      /**
       * Repository
       * */
      single<ApplicationConfig> {
        val env = System.getenv("KTOR_ENV") ?: "prod"
        val baseConfig = ConfigFactory.load("application.conf")
        val envConfig = ConfigFactory.load("application-$env.conf")
        HoconApplicationConfig(envConfig.withFallback(baseConfig))
      }

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
      single<FileService>{
        FileServiceImpl(get(), get(), get())
      }
      /**
       * Services
       * */
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
        StoreServiceImpl(get())
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
      single<BzAgencyService> {
        BzAgencyServiceImpl(get(), get())
      }
    })
  }
}
