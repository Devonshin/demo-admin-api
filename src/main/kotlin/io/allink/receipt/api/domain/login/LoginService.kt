package io.allink.receipt.api.domain.login

import io.ktor.server.auth.jwt.*

interface LoginService {

  suspend fun generateVerificationCode(verificationCodeRequest: VerificationCodeRequest): VerificationCode

  suspend fun checkVerificationCode(checkRequest: VerificationCheckRequest): Jwt

  suspend fun renewalJwt(principal: JWTPrincipal): Jwt


}
