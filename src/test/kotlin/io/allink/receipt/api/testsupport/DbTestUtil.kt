package io.allink.receipt.api.testsupport

import java.sql.Statement

/**
 * 공용 테스트 DDL 유틸리티
 * - 통합 테스트에서 반복되는 CREATE/DROP TABLE 구문을 일관되게 관리합니다.
 * - PostgresContainerBase.runJdbc와 함께 사용하세요.
 *
 * 용어 설명:
 * - DDL(Data Definition Language): 테이블 등 스키마 객체 생성/변경/삭제 명령어 모음
 */
/**
 * @author Devonshin
 * @date 2025-09-13
 */
object DbTestUtil {
  fun dropCoreTables(stmt: Statement) {
    stmt.execute("DROP TABLE IF EXISTS \"store\" CASCADE")
    stmt.execute("DROP TABLE IF EXISTS \"store_billing\" CASCADE")
    stmt.execute("DROP TABLE IF EXISTS \"store_billing_token\" CASCADE")
    stmt.execute("DROP TABLE IF EXISTS \"n_point_store\" CASCADE")
    stmt.execute("DROP TABLE IF EXISTS \"n_point_store_service\" CASCADE")
    stmt.execute("DROP TABLE IF EXISTS \"bz_agency\" CASCADE")
    stmt.execute("DROP TABLE IF EXISTS \"admin\" CASCADE")
    stmt.execute("DROP TABLE IF EXISTS \"login_info\" CASCADE")
  }

  fun createStoreTables(stmt: Statement) {
    // store
    stmt.execute(
      """
      CREATE TABLE IF NOT EXISTS "store" (
        store_uid VARCHAR(36) PRIMARY KEY,
        store_name VARCHAR(255) NOT NULL,
        store_type VARCHAR(255) NULL,
        zone_code VARCHAR(20) NULL,
        addr1 VARCHAR(255) NULL,
        addr2 VARCHAR(255) NULL,
        icon_url VARCHAR(255) NULL,
        logo_url VARCHAR(255) NULL,
        franchise_code VARCHAR(30) NULL,
        map_url VARCHAR(255) NULL,
        lat VARCHAR(20) NULL,
        lon VARCHAR(20) NULL,
        tel VARCHAR(15) NULL,
        mobile VARCHAR(15) NULL,
        manager_name VARCHAR(30) NULL,
        site_link VARCHAR(255) NULL,
        receipt_width_inch VARCHAR(2) NULL,
        status VARCHAR(20) NULL,
        work_type VARCHAR(30) NULL,
        business_no VARCHAR(30) NULL,
        partner_login_id VARCHAR(50) NULL,
        partner_login_pword VARCHAR(255) NULL,
        ceo_name VARCHAR(30) NULL,
        business_type VARCHAR(255) NULL,
        event_type VARCHAR(255) NULL,
        email VARCHAR(255) NULL,
        business_no_law VARCHAR(30) NULL,
        coupon_ad_yn BOOLEAN NULL,
        application_file_path VARCHAR(255) NULL,
        bz_file_path VARCHAR(255) NULL,
        id_file_path VARCHAR(255) NULL,
        bank_file_path VARCHAR(255) NULL,
        bz_agency_uuid UUID NULL,
        reg_date TIMESTAMP NULL,
        reg_by UUID NULL,
        mod_date TIMESTAMP NULL,
        mod_by UUID NULL,
        delete_date TIMESTAMP NULL
      )
      """.trimIndent()
    )

    // store_billing
    stmt.execute(
      """
      CREATE TABLE IF NOT EXISTS "store_billing" (
        billing_seq BIGSERIAL PRIMARY KEY,
        store_uid VARCHAR(36) NOT NULL,
        store_service_seq INT NOT NULL,
        token_uuid UUID NOT NULL,
        billing_amount INT NULL,
        status VARCHAR(10) NOT NULL,
        bank_code VARCHAR(32) NULL,
        bank_account_no VARCHAR(32) NULL,
        bank_account_name VARCHAR(32) NULL,
        reg_by UUID NOT NULL,
        reg_date TIMESTAMP NOT NULL
      )
      """.trimIndent()
    )

    // store_billing_token
    stmt.execute(
      """
      CREATE TABLE IF NOT EXISTS "store_billing_token" (
        token_uuid UUID PRIMARY KEY,
        business_no VARCHAR(12) NOT NULL,
        token VARCHAR(255) NOT NULL,
        token_info VARCHAR(255) NULL,
        status VARCHAR(20) NOT NULL,
        reg_date TIMESTAMP NULL,
        reg_by UUID NOT NULL
      )
      """.trimIndent()
    )

    // n_point_store
    stmt.execute(
      """
      CREATE TABLE IF NOT EXISTS "n_point_store" (
        store_uid VARCHAR(36) PRIMARY KEY,
        reserved_points INT NULL,
        review_points INT DEFAULT 0,
        cumulative_points INT DEFAULT 0,
        regular_payment_amounts INT NOT NULL,
        status VARCHAR(20) NOT NULL,
        service_start_at TIMESTAMP NULL,
        service_end_at TIMESTAMP NULL,
        point_renewal_type VARCHAR(20) NOT NULL,
        reg_date TIMESTAMP NOT NULL,
        mod_date TIMESTAMP NULL,
        reg_by UUID NOT NULL,
        mod_by UUID NULL
      )
      """.trimIndent()
    )

    // n_point_store_service (조인키: store_uid, service_code 등)
    stmt.execute(
      """
      CREATE TABLE IF NOT EXISTS "n_point_store_service" (
        store_service_seq INT NOT NULL,
        store_uid VARCHAR(36) NOT NULL,
        service_code VARCHAR(50) NOT NULL,
        service_charge INT NOT NULL,
        reward_deposit INT NULL,
        reward_point INT NULL,
        service_commission INT NULL,
        status VARCHAR(20) NOT NULL,
        reg_date TIMESTAMP NOT NULL,
        mod_date TIMESTAMP NULL,
        reg_by UUID NOT NULL,
        mod_by UUID NULL,
        PRIMARY KEY (store_service_seq, store_uid, service_code)
      )
      """.trimIndent()
    )

    // bz_agency (전체 스키마; Repository 조인 시 selectAll 매핑 일치 필요)
    stmt.execute(
      """
      CREATE TABLE IF NOT EXISTS "bz_agency" (
        uuid UUID PRIMARY KEY,
        agency_name VARCHAR(255) NULL,
        business_no VARCHAR(20) NULL,
        addr1 VARCHAR(255) NULL,
        addr2 VARCHAR(255) NULL,
        tel VARCHAR(20) NULL,
        ceo_name VARCHAR(50) NULL,
        ceo_phone VARCHAR(20) NULL,
        application_file_path VARCHAR(255) NULL,
        bz_file_path VARCHAR(255) NULL,
        id_file_path VARCHAR(255) NULL,
        bank_file_path VARCHAR(255) NULL,
        is_receipt_alliance BOOLEAN NULL,
        infra_ratio INT NULL,
        reward_base_ratio INT NULL,
        reward_commission_ratio INT NULL,
        reward_package_ratio INT NULL,
        advertisement_ratio INT NULL,
        is_coupon_adv BOOLEAN NULL,
        coupon_adv_ratio INT NULL,
        tag_deposit INT NULL,
        agency_deposit INT NULL,
        settlement_bank VARCHAR(50) NULL,
        bank_account_name VARCHAR(50) NULL,
        bank_account_no VARCHAR(50) NULL,
        status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
        reg_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        reg_by UUID NOT NULL DEFAULT '00000000-0000-0000-0000-000000000000',
        mod_date TIMESTAMP NULL,
        mod_by UUID NULL
      )
      """.trimIndent()
    )

    // admin (로그인 도메인 연계용 최소 컬럼)
    stmt.execute(
      """
      CREATE TABLE IF NOT EXISTS "admin" (
        uuid UUID PRIMARY KEY,
        login_id VARCHAR(100) NULL,
        password VARCHAR(255) NULL,
        full_name VARCHAR(50) NOT NULL,
        role VARCHAR(20) NOT NULL,
        phone VARCHAR(15) NOT NULL,
        email VARCHAR(100) NULL,
        status VARCHAR(20) NOT NULL,
        reg_date TIMESTAMP NULL,
        mod_date TIMESTAMP NULL,
        reg_by UUID NULL,
        mod_by UUID NULL,
        agency_uuid UUID NULL
      )
      """.trimIndent()
    )

    // login_info
    stmt.execute(
      """
      CREATE TABLE IF NOT EXISTS "login_info" (
        login_uuid UUID PRIMARY KEY,
        user_uuid UUID NOT NULL,
        verification_code TEXT NOT NULL,
        expire_date TIMESTAMP NOT NULL,
        status VARCHAR(20) NOT NULL,
        login_date TIMESTAMP NULL
      )
      """.trimIndent()
    )
  }
}
