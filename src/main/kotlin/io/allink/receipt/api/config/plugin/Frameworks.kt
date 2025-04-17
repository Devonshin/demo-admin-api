package io.allink.receipt.api.config.plugin

import StoreServiceImpl
import io.allink.receipt.api.domain.admin.AdminRepository
import io.allink.receipt.api.domain.admin.AdminRepositoryImpl
import io.allink.receipt.api.domain.admin.AdminService
import io.allink.receipt.api.domain.admin.AdminServiceImpl
import io.allink.receipt.api.domain.admin.AdminTable
import io.allink.receipt.api.domain.code.ServiceCodeRepository
import io.allink.receipt.api.domain.code.ServiceCodeRepositoryImpl
import io.allink.receipt.api.domain.code.ServiceCodeTable
import io.allink.receipt.api.domain.login.LoginInfoRepository
import io.allink.receipt.api.domain.login.LoginInfoRepositoryImpl
import io.allink.receipt.api.domain.login.LoginInfoTable
import io.allink.receipt.api.domain.login.LoginService
import io.allink.receipt.api.domain.login.LoginServiceImpl
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
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureFrameworks() {
  install(Koin) {
    slf4jLogger()
    modules(module {
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
      single<AdminService> {
        AdminServiceImpl(get())
      }
      single<VerificationService> {
        SMSVerificationServiceImpl()
      }
      single<LoginService> {
        LoginServiceImpl(get(), get(), get(), environment.config)
      }
      single<UserService> {
        UserServiceImpl(get())
      }
      single<StoreService> {
        StoreServiceImpl(get())
      }
    })
  }
}
