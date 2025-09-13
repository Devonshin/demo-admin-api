package io.allink.receipt.api.domain.user

import io.allink.receipt.api.common.Constant
import io.allink.receipt.api.domain.Page
import io.allink.receipt.api.domain.Sorter
import io.allink.receipt.api.testsupport.PostgresContainerBase
import io.allink.receipt.api.testsupport.DbTestUtil
import io.allink.receipt.api.testsupport.R2dbcExposedInit
import io.allink.receipt.api.util.AES256Util
import io.allink.receipt.api.repository.TransactionUtil
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * @file UserRepositoryIT.kt
 * @brief Testcontainers PostgreSQL + Exposed(v1 r2dbc) 기반 UserRepository 통합 테스트
 * @author Devonshin
 * @date 2025-09-12
 */
class UserRepositoryIT : PostgresContainerBase() {

  private lateinit var repository: UserRepository

  @BeforeEach
  fun setUp() {
    // 스키마 생성(DDL)
    runJdbc { stmt ->
      DbTestUtil.dropCoreTables(stmt)
      DbTestUtil.createStoreTables(stmt)
      stmt.execute(
        // user는 예약어이므로 반드시 이스케이프
        """
        CREATE TABLE IF NOT EXISTS "user" (
          uuid VARCHAR(36) PRIMARY KEY,
          name VARCHAR(255) NULL,
          status VARCHAR(20) NULL,
          phone VARCHAR(50) NULL,
          gender VARCHAR(50) NULL,
          ci VARCHAR(255) NULL,
          birthday VARCHAR(50) NULL,
          local_yn VARCHAR(1) NULL,
          email VARCHAR(255) NULL,
          reg_date TIMESTAMP NULL,
          mod_date TIMESTAMP NULL,
          role VARCHAR(20) NULL,
          join_social_type VARCHAR(20) NULL,
          nickname VARCHAR(255) NULL,
          mtchg_id VARCHAR(255) NULL,
          cpoint_reg_type VARCHAR(20) NULL,
          cpoint_reg_date TIMESTAMP NULL
        )
        """.trimIndent()
      )
    }

    // Exposed v1 r2dbc 초기화
    R2dbcExposedInit.init(
      host = container.host,
      port = container.getMappedPort(5432),
      database = container.databaseName,
      username = container.username,
      password = container.password
    )

    repository = UserRepositoryImpl(UserTable)
  }

  @Test
  fun `Find should return one user`() = runBlocking {
    // given: 데이터 삽입(JDBC)
    val encPhone = AES256Util.encrypt("01099998888", Constant.AES256_KEY)
    val encEmail = AES256Util.encrypt("a@b.c", Constant.AES256_KEY)
    val encNickname = AES256Util.encrypt("길동", Constant.AES256_KEY)

    runJdbc { stmt ->
      stmt.execute(
        """
        INSERT INTO "user" (uuid, name, status, phone, gender, ci, birthday, local_yn, email, reg_date, mod_date, role, join_social_type, nickname)
        VALUES ('u-100', '홍길동', 'ACTIVE', '$encPhone', 'M', NULL, '1990-01-01', 'Y', '$encEmail', NULL, NULL, 'USER', 'NAVER', '$encNickname')
        """.trimIndent()
      )
    }

    // when
    val entity = TransactionUtil.withTransaction { repository.find("u-100") }

    // then
    assertThat(entity).isNotNull()
    assertThat(entity!!.id).isEqualTo("u-100")
    assertThat(entity.name).isEqualTo("홍길동")
    assertThat(entity.status).isEqualTo(UserStatus.ACTIVE)
    assertThat(entity.phone).isEqualTo("01099998888")
    assertThat(entity.email).isEqualTo("a@b.c")
    assertThat(entity.nickname).isEqualTo("길동")

  }

  @Test
  fun `findAll_should_filter_sort_and_paginate`() = runBlocking {
    // given: 더미 데이터 3건
    fun ins(id: String, name: String, phone: String, nickname: String, gender: String = "M") {
      val encPhone = AES256Util.encrypt(phone, Constant.AES256_KEY)
      val encEmail = AES256Util.encrypt("$id@ex.com", Constant.AES256_KEY)
      val encNickname = AES256Util.encrypt(nickname, Constant.AES256_KEY)
      runJdbc { stmt ->
        stmt.execute(
          """
          INSERT INTO "user" (uuid, name, status, phone, gender, ci, birthday, local_yn, email, reg_date, mod_date, role, join_social_type, nickname)
          VALUES ('$id', '$name', 'ACTIVE', '$encPhone', '$gender', NULL, '1990-01-01', 'Y', '$encEmail', NULL, NULL, 'USER', 'NAVER', '$encNickname')
          """.trimIndent()
        )
      }
    }
    ins("u-1", "홍길동", "01011112222", "길동")
    ins("u-2", "홍길서", "01011113333", "길서")
    ins("u-3", "이순신", "01011114444", "순신")

    val filter = UserFilter(
      phone = null,
      name = "홍", // name starts with
      nickName = null,
      age = null,
      gender = null,
      sort = listOf(Sorter(field = "name", direction = "asc")),
      page = Page(page = 1, pageSize = 10)
    )

    // when
    val page = TransactionUtil.withTransaction { repository.findAll(filter) }

    // then
    assertThat(page.totalCount).isEqualTo(2)
    assertThat(page.items.map { it.name }).containsExactly("홍길동", "홍길서")
    assertThat(page.items.first().phone).isEqualTo("01011112222")
  }
}