
CREATE TABLE admin
(
    uuid        uuid         NOT NULL,
    login_id    varchar(100) NOT NULL,
    password    varchar(255),
    full_name   varchar(50)  NOT NULL,
    role        varchar(20)  NOT NULL,
    agency_uuid uuid        ,
    phone       varchar(15)  NOT NULL,
    email       varchar(100),
    status      varchar(20)  NOT NULL,
    reg_date    timestamp    NOT NULL,
    mod_date    timestamp   ,
    reg_by      uuid         NOT NULL,
    mod_by      uuid        ,
    PRIMARY KEY (uuid)
);

COMMENT ON TABLE admin IS '관리자 계정';

COMMENT ON COLUMN admin.uuid IS '고유아이디';

COMMENT ON COLUMN admin.login_id IS '로그인아이디';

COMMENT ON COLUMN admin.password IS '패스워드';

COMMENT ON COLUMN admin.full_name IS '이름';

COMMENT ON COLUMN admin.role IS '권한';

COMMENT ON COLUMN admin.agency_uuid IS '대리점 고유아이디';

COMMENT ON COLUMN admin.phone IS '전화번호';

COMMENT ON COLUMN admin.email IS '이메일';

COMMENT ON COLUMN admin.status IS '상태';

COMMENT ON COLUMN admin.reg_date IS '등록일시';

COMMENT ON COLUMN admin.mod_date IS '수정일시';

COMMENT ON COLUMN admin.reg_by IS '등록인 아이디';

COMMENT ON COLUMN admin.mod_by IS '수정인 아이디';

CREATE TABLE bz_agency
(
    uuid                    uuid         NOT NULL,
    agency_name             varchar(255) NOT NULL,
    business_no             varchar(20)  NOT NULL,
    addr1                   varchar(255),
    addr2                   varchar(255),
    tel                     varchar(20) ,
    ceo_name                varchar(50) ,
    ceo_phone               varchar(20) ,
    reg_application_file    bool        ,
    reg_bz_file             bool        ,
    reg_id_file             bool        ,
    reg_bank_file           bool        ,
    is_receipt_alliance     bool        ,
    infra_ratio             int         ,
    reward_base_ratio       int         ,
    reward_commission_ratio int         ,
    reward_package_ratio    int         ,
    advertisement_ratio     int         ,
    is_coupon_adv           bool        ,
    coupon_adv_ratio        int         ,
    tag_deposit             int         ,
    agency_deposit          int         ,
    settlement_bank         varchar(20) ,
    bank_account_name       varchar(50) ,
    bank_account_no         varchar(50) ,
    reg_date                timestamp    NOT NULL,
    reg_by                  uuid         NOT NULL,
    mod_date                timestamp   ,
    mod_by                  uuid        ,
    PRIMARY KEY (uuid)
);

COMMENT ON TABLE bz_agency IS ' 영업 대리점';

COMMENT ON COLUMN bz_agency.uuid IS '고유아이디';

COMMENT ON COLUMN bz_agency.agency_name IS '대리점명';

COMMENT ON COLUMN bz_agency.business_no IS '사업자번호';

COMMENT ON COLUMN bz_agency.addr1 IS '주소 1';

COMMENT ON COLUMN bz_agency.addr2 IS '주소 2';

COMMENT ON COLUMN bz_agency.tel IS '전화번호';

COMMENT ON COLUMN bz_agency.ceo_name IS '대표자명';

COMMENT ON COLUMN bz_agency.ceo_phone IS '대표자 전화번호';

COMMENT ON COLUMN bz_agency.reg_application_file IS '신청서 등록여부';

COMMENT ON COLUMN bz_agency.reg_bz_file IS '사업자등록증 등록여부';

COMMENT ON COLUMN bz_agency.reg_id_file IS '대표자 신분증 등록여부';

COMMENT ON COLUMN bz_agency.reg_bank_file IS '통장사본 등록여부';

COMMENT ON COLUMN bz_agency.is_receipt_alliance IS '영수증 제휴 여부';

COMMENT ON COLUMN bz_agency.infra_ratio IS '영수증 인프라 배분율';

COMMENT ON COLUMN bz_agency.reward_base_ratio IS '리워드 기본료 배분율';

COMMENT ON COLUMN bz_agency.reward_commission_ratio IS '리워드 수수료 배분율';

COMMENT ON COLUMN bz_agency.reward_package_ratio IS '999+ 배분율';

COMMENT ON COLUMN bz_agency.advertisement_ratio IS '영수증 광고 배분율';

COMMENT ON COLUMN bz_agency.is_coupon_adv IS '핫플 쿠폰 광고 진행여부';

COMMENT ON COLUMN bz_agency.coupon_adv_ratio IS '쿠폰광고 배분율';

COMMENT ON COLUMN bz_agency.tag_deposit IS '태그 예치금';

COMMENT ON COLUMN bz_agency.agency_deposit IS '대리점 보증금';

COMMENT ON COLUMN bz_agency.settlement_bank IS '은행';

COMMENT ON COLUMN bz_agency.bank_account_name IS '예금주';

COMMENT ON COLUMN bz_agency.bank_account_no IS '계좌번호';

COMMENT ON COLUMN bz_agency.reg_date IS '등록일시';

COMMENT ON COLUMN bz_agency.reg_by IS '등록인아이디';

COMMENT ON COLUMN bz_agency.mod_date IS '수정일시';

COMMENT ON COLUMN bz_agency.mod_by IS '수정인 아이디';

CREATE TABLE bz_agency_store
(
    bz_agency_uuid uuid        NOT NULL,
    store_uid      varchar(36) NOT NULL,
    reg_date       timestamp   NOT NULL,
    mod_date       timestamp  ,
    reg_by         uuid        NOT NULL,
    mod_by         uuid       ,
    PRIMARY KEY (bz_agency_uuid, store_uid)
);

COMMENT ON TABLE bz_agency_store IS '영업 대리점 관리 가맹점';

COMMENT ON COLUMN bz_agency_store.bz_agency_uuid IS '영업 대리점 고유아이디';

COMMENT ON COLUMN bz_agency_store.store_uid IS '가맹점 고유아이디';

COMMENT ON COLUMN bz_agency_store.reg_date IS '등록일시';

COMMENT ON COLUMN bz_agency_store.mod_date IS '수정일시';

COMMENT ON COLUMN bz_agency_store.reg_by IS '등록인 아이디';

COMMENT ON COLUMN bz_agency_store.mod_by IS '수정인 아이디';

CREATE TABLE coupon
(
    uuid      uuid         NOT NULL,
    name      varchar(255),
    file_path varchar(255),
    reg_date  timestamp    NOT NULL,
    reg_by    uuid         NOT NULL,
    mod_date  timestamp    NOT NULL,
    mod_by    uuid         NOT NULL,
    PRIMARY KEY (uuid)
);

COMMENT ON TABLE coupon IS '쿠폰 ';

COMMENT ON COLUMN coupon.uuid IS '쿠폰 고유아이디';

COMMENT ON COLUMN coupon.name IS '쿠폰명';

COMMENT ON COLUMN coupon.file_path IS '파일 업로드 패스';

COMMENT ON COLUMN coupon.reg_date IS '등록일시';

COMMENT ON COLUMN coupon.reg_by IS '등록인 고유아이디';

COMMENT ON COLUMN coupon.mod_date IS '수정일시';

COMMENT ON COLUMN coupon.mod_by IS '수정인 고유아이디';

CREATE TABLE eReceipts
(
);

COMMENT ON TABLE eReceipts IS '영수증';

CREATE TABLE login_info
(
    login_uuid        uuid        NOT NULL,
    user_uuid         uuid        NOT NULL,
    verification_code varchar(10) NOT NULL,
    expire_date       timestamp   NOT NULL,
    status            varchar(20) NOT NULL,
    PRIMARY KEY (login_uuid)
);

COMMENT ON TABLE login_info IS '로그인정보';

COMMENT ON COLUMN login_info.login_uuid IS '로그인 고유아이디';

COMMENT ON COLUMN login_info.user_uuid IS '유저 uuid';

COMMENT ON COLUMN login_info.verification_code IS '인증코드';

COMMENT ON COLUMN login_info.expire_date IS '만료시간';

COMMENT ON COLUMN login_info.status IS '상태';

CREATE TABLE merchant_billing_token
(
    token_uuid         uuid         NOT NULL,
    merchant_info_uuid uuid         NOT NULL,
    token              varchar(255) NOT NULL,
    tokenInfo          varchar     ,
    status             varchar(10)  NOT NULL,
    reg_date           timestamp   ,
    mod_date           timestamp   ,
    reg_by             uuid         NOT NULL,
    mod_by             uuid         NOT NULL,
    PRIMARY KEY (token_uuid)
);

COMMENT ON TABLE merchant_billing_token IS '머천트 결제 토큰';

COMMENT ON COLUMN merchant_billing_token.token_uuid IS '토큰 고유아이디';

COMMENT ON COLUMN merchant_billing_token.merchant_info_uuid IS '계약자 고유아이디';

COMMENT ON COLUMN merchant_billing_token.token IS 'sk결제토큰';

COMMENT ON COLUMN merchant_billing_token.tokenInfo IS '토큰추가정보';

COMMENT ON COLUMN merchant_billing_token.status IS '상태';

COMMENT ON COLUMN merchant_billing_token.reg_date IS '등록일시';

COMMENT ON COLUMN merchant_billing_token.mod_date IS '수정일시';

COMMENT ON COLUMN merchant_billing_token.reg_by IS '등록인 고유아이디';

COMMENT ON COLUMN merchant_billing_token.mod_by IS '수정인 고유아이디';

CREATE TABLE merchant_group
(
    merchant_group_id   varchar(20)  NOT NULL,
    token_key           varchar(255) NOT NULL,
    merchant_group_name varchar(50) ,
    remote_ips          text         NOT NULL,
    service_start_at    timestamp    NOT NULL,
    service_end_at      timestamp    NOT NULL,
    status              varchar(10)  NOT NULL,
    receipt_width       int2        ,
    authorities         varchar(255) NOT NULL,
    receipt_type        varchar(20) ,
    reg_date            timestamp    NOT NULL,
    mod_date            timestamp   ,
    PRIMARY KEY (merchant_group_id)
);

COMMENT ON TABLE merchant_group IS '가맹점, 기업 개인';

COMMENT ON COLUMN merchant_group.merchant_group_id IS '머천트 그룹 아이디';

COMMENT ON COLUMN merchant_group.token_key IS '인증토큰|Koces는 가맹점uid';

COMMENT ON COLUMN merchant_group.merchant_group_name IS '머천트 그룹명';

COMMENT ON COLUMN merchant_group.remote_ips IS '접근가능아이피';

COMMENT ON COLUMN merchant_group.service_start_at IS '서비스 시작일시';

COMMENT ON COLUMN merchant_group.service_end_at IS '서비스 종료일시';

COMMENT ON COLUMN merchant_group.status IS '상태';

COMMENT ON COLUMN merchant_group.receipt_width IS '영수증 너비';

COMMENT ON COLUMN merchant_group.authorities IS '이용권한';

COMMENT ON COLUMN merchant_group.receipt_type IS '영수증전문타입';

COMMENT ON COLUMN merchant_group.reg_date IS '등록일시';

COMMENT ON COLUMN merchant_group.mod_date IS '수정일시';

CREATE TABLE merchant_info
(
    uuid           uuid         NOT NULL,
    bz_agency_uuid uuid         NOT NULL,
    business_no    varchar(50) ,
    ceo_name       varchar(50)  NOT NULL,
    ceo_ci         varchar(255) NOT NULL,
    ceo_citizen_no varchar(50)  NOT NULL,
    phone          varchar(50) ,
    phone_provider varchar(10)  NOT NULL,
    birthday       varchar(50)  NOT NULL,
    gender         varchar(10) ,
    reference_uuid varchar(36) ,
    corperate_no   varchar(50)  NOT NULL,
    reg_date       timestamp    NOT NULL,
    mod_date       timestamp   ,
    reg_by         uuid         NOT NULL,
    mod_by         uuid         NOT NULL,
    PRIMARY KEY (uuid)
);

COMMENT ON TABLE merchant_info IS '가맹점 계약 신청인 정보';

COMMENT ON COLUMN merchant_info.uuid IS '계약자 고유아이디';

COMMENT ON COLUMN merchant_info.bz_agency_uuid IS '영업 대리점 고유아이디';

COMMENT ON COLUMN merchant_info.business_no IS '사업자번호';

COMMENT ON COLUMN merchant_info.ceo_name IS '대표자명';

COMMENT ON COLUMN merchant_info.ceo_ci IS 'ci';

COMMENT ON COLUMN merchant_info.ceo_citizen_no IS '주민번호';

COMMENT ON COLUMN merchant_info.phone IS '전화번호';

COMMENT ON COLUMN merchant_info.phone_provider IS '통신사코드';

COMMENT ON COLUMN merchant_info.birthday IS '생년월일';

COMMENT ON COLUMN merchant_info.gender IS '성별';

COMMENT ON COLUMN merchant_info.reference_uuid IS '추천인아이디';

COMMENT ON COLUMN merchant_info.corperate_no IS '법인번호';

COMMENT ON COLUMN merchant_info.reg_date IS '등록일시';

COMMENT ON COLUMN merchant_info.mod_date IS '수정일시';

COMMENT ON COLUMN merchant_info.reg_by IS '등록인 고유아이디';

COMMENT ON COLUMN merchant_info.mod_by IS '수정인 고유아이디';

CREATE TABLE merchant_receipt
(
    receipt_uuid      varchar(36) NOT NULL,
    merchant_store_id varchar(36) NOT NULL,
    device_id         varchar(30) NOT NULL,
    reg_date          timestamp   NOT NULL,
    payload           json        NOT NULL,
    trx_id            varchar(36),
    PRIMARY KEY (receipt_uuid)
);

COMMENT ON TABLE merchant_receipt IS '머천트 영수증';

COMMENT ON COLUMN merchant_receipt.receipt_uuid IS '영수증 고유아이디';

COMMENT ON COLUMN merchant_receipt.merchant_store_id IS '머천트 가맹점 아이디';

COMMENT ON COLUMN merchant_receipt.device_id IS '디바이스아이디';

COMMENT ON COLUMN merchant_receipt.reg_date IS '등록일시';

COMMENT ON COLUMN merchant_receipt.payload IS '영수증 전문';

COMMENT ON COLUMN merchant_receipt.trx_id IS '전문 고유아이디';

CREATE TABLE merchant_tag
(
    tag_id            varchar(36) NOT NULL,
    merchant_group_id varchar(20) NOT NULL,
    merchant_store_id varchar(36) NOT NULL,
    device_id         varchar(30) NOT NULL,
    store_uid         varchar(36),
    reg_date          timestamp   NOT NULL,
    mod_date          timestamp  ,
    reg_by            uuid        NOT NULL,
    mod_by            uuid       ,
    PRIMARY KEY (tag_id)
);

COMMENT ON TABLE merchant_tag IS '태그';

COMMENT ON COLUMN merchant_tag.tag_id IS '태그아이디';

COMMENT ON COLUMN merchant_tag.merchant_group_id IS '머천트 그룹 아이디';

COMMENT ON COLUMN merchant_tag.merchant_store_id IS '머천트가맹점아이디';

COMMENT ON COLUMN merchant_tag.device_id IS '디바이스아이디';

COMMENT ON COLUMN merchant_tag.store_uid IS '가맹점아이디';

COMMENT ON COLUMN merchant_tag.reg_date IS '등록일시';

COMMENT ON COLUMN merchant_tag.mod_date IS '수정일시';

COMMENT ON COLUMN merchant_tag.reg_by IS '등록인 아이디';

COMMENT ON COLUMN merchant_tag.mod_by IS '수정인 아이디';

CREATE TABLE n_point_pay_waiting
(
    seq          bigint      NOT NULL DEFAULT nextval,
    receipt_uuid varchar     NOT NULL,
    provide_case varchar(10) NOT NULL,
    reg_date     timestamp   NOT NULL,
    PRIMARY KEY (seq)
);

COMMENT ON TABLE n_point_pay_waiting IS '리뷰 포인트 지급 대기열';

COMMENT ON COLUMN n_point_pay_waiting.seq IS '시퀀스';

COMMENT ON COLUMN n_point_pay_waiting.receipt_uuid IS '영수증 고유아이디';

COMMENT ON COLUMN n_point_pay_waiting.provide_case IS '포인트 지급 타입';

COMMENT ON COLUMN n_point_pay_waiting.reg_date IS '등록일시';

CREATE TABLE n_point_store
(
    store_uid               varchar(36) NOT NULL,
    merchant_info_uuid      uuid        NOT NULL,
    reserved_points         integer     DEFAULT 0,
    review_points           integer     NOT NULL DEFAULT 0,
    cumulative_points       integer     NOT NULL DEFAULT 0,
    reqular_payment_amounts integer     NOT NULL,
    status                  varchar     NOT NULL DEFAULT PENDING,
    service_start_at        timestamp   NOT NULL,
    service_end_at          timestamp   NOT NULL,
    point_renewal_type      varchar     NOT NULL DEFAULT TEMP_STOP,
    reg_date                timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    mod_date                timestamp  ,
    reg_by                  uuid        NOT NULL,
    mod_by                  uuid        NOT NULL,
    PRIMARY KEY (store_uid)
);

COMMENT ON TABLE n_point_store IS '네이버포인트가맹점';

COMMENT ON COLUMN n_point_store.store_uid IS '가맹점 고유아이디';

COMMENT ON COLUMN n_point_store.merchant_info_uuid IS '계약자 고유아이디';

COMMENT ON COLUMN n_point_store.reserved_points IS '포인트 지급 용 적립 포인트';

COMMENT ON COLUMN n_point_store.review_points IS '리뷰작성시 제공할 포인트';

COMMENT ON COLUMN n_point_store.cumulative_points IS '누적 적립 포인트';

COMMENT ON COLUMN n_point_store.reqular_payment_amounts IS '정기결제요금액';

COMMENT ON COLUMN n_point_store.status IS '상태';

COMMENT ON COLUMN n_point_store.service_start_at IS '서비스 시작일시';

COMMENT ON COLUMN n_point_store.service_end_at IS '서비스 종료일시';

COMMENT ON COLUMN n_point_store.point_renewal_type IS '지급할 포인트 소진 시 갱신타입';

COMMENT ON COLUMN n_point_store.reg_date IS '등록일시';

COMMENT ON COLUMN n_point_store.mod_date IS '수정일시';

COMMENT ON COLUMN n_point_store.reg_by IS '등록인 고유아이디';

COMMENT ON COLUMN n_point_store.mod_by IS '수정인 고유아이디';

CREATE TABLE n_point_store_pay_history
(
    store_uid    varchar(36) NOT NULL,
    receipt_uuid varchar     NOT NULL,
    points       integer     NOT NULL,
    amounts      integer     NOT NULL DEFAULT 0,
    reg_date     timestamp   NOT NULL,
    mod_date     timestamp  ,
    expire_date  timestamp   NOT NULL,
    PRIMARY KEY (store_uid, receipt_uuid)
);

COMMENT ON TABLE n_point_store_pay_history IS '네이버 포인트 가맹점 지급 이력';

COMMENT ON COLUMN n_point_store_pay_history.store_uid IS '가맹점고유아이디';

COMMENT ON COLUMN n_point_store_pay_history.receipt_uuid IS '리뷰 고유아이디';

COMMENT ON COLUMN n_point_store_pay_history.points IS '포인트 금액';

COMMENT ON COLUMN n_point_store_pay_history.amounts IS '잔액';

COMMENT ON COLUMN n_point_store_pay_history.reg_date IS '등록일시';

COMMENT ON COLUMN n_point_store_pay_history.mod_date IS '수정일시';

COMMENT ON COLUMN n_point_store_pay_history.expire_date IS '만료일시';

CREATE TABLE n_point_store_service
(
    store_uid          varchar(36) NOT NULL,
    service_code       varchar(36) NOT NULL,
    service_charge     int4        NOT NULL,
    reward_deposit     int4       ,
    reward_point       int4       ,
    service_commission int4       ,
    status             varchar     NOT NULL,
    reg_date           timestamp   NOT NULL,
    mod_date           timestamp  ,
    reg_by             uuid        NOT NULL,
    mod_by             uuid        NOT NULL,
    PRIMARY KEY (store_uid, service_code)
);

COMMENT ON TABLE n_point_store_service IS '가맹점 이용 서비스';

COMMENT ON COLUMN n_point_store_service.store_uid IS '가맹점 고유아이디';

COMMENT ON COLUMN n_point_store_service.service_code IS '서비스 아이디';

COMMENT ON COLUMN n_point_store_service.service_charge IS '서비스 기본료';

COMMENT ON COLUMN n_point_store_service.reward_deposit IS '보증금';

COMMENT ON COLUMN n_point_store_service.reward_point IS '지급 포인트';

COMMENT ON COLUMN n_point_store_service.service_commission IS '수수료';

COMMENT ON COLUMN n_point_store_service.status IS '상태';

COMMENT ON COLUMN n_point_store_service.reg_date IS '등록일시';

COMMENT ON COLUMN n_point_store_service.mod_date IS '수정일시';

COMMENT ON COLUMN n_point_store_service.reg_by IS '등록인 고유아이디';

COMMENT ON COLUMN n_point_store_service.mod_by IS '수정인 고유아이디';

CREATE TABLE n_point_tx_history
(
    waiting_seq bigint      NOT NULL DEFAULT nextval,
    tx_no       varchar    ,
    reg_date    timestamp  ,
    result_code varchar(20) NOT NULL,
    PRIMARY KEY (waiting_seq)
);

COMMENT ON TABLE n_point_tx_history IS '포인트 지급 연동 이력';

COMMENT ON COLUMN n_point_tx_history.waiting_seq IS '대기열 시퀀스';

COMMENT ON COLUMN n_point_tx_history.tx_no IS '연동 거래번호';

COMMENT ON COLUMN n_point_tx_history.reg_date IS '연동일시';

COMMENT ON COLUMN n_point_tx_history.result_code IS '연동 결과코드';

CREATE TABLE n_point_user
(
    user_uuid           varchar NOT NULL,
    user_key            varchar,
    total_review_points integer NOT NULL DEFAULT 0,
    total_review_count  integer DEFAULT 0,
    total_event_points  integer NOT NULL DEFAULT 0,
    total_event_count   integer NOT NULL DEFAULT 0,
    PRIMARY KEY (user_uuid)
);

COMMENT ON TABLE n_point_user IS '네이버포인트리뷰유저';

COMMENT ON COLUMN n_point_user.user_uuid IS '유저고유아이디';

COMMENT ON COLUMN n_point_user.user_key IS '유저 연동 키';

COMMENT ON COLUMN n_point_user.total_review_points IS '총 리뷰 포인트';

COMMENT ON COLUMN n_point_user.total_review_count IS '총리뷰카운트';

COMMENT ON COLUMN n_point_user.total_event_points IS '총수령포인트';

COMMENT ON COLUMN n_point_user.total_event_count IS '총이벤트카운트';

CREATE TABLE n_point_user_review
(
    receipt_uuid varchar   NOT NULL,
    user_uuid    varchar   NOT NULL,
    store_uid    varchar   NOT NULL,
    points       integer   NOT NULL,
    status       varchar   NOT NULL,
    review_url   varchar  ,
    reg_date     timestamp NOT NULL,
    mod_date     timestamp,
    expire_date  timestamp,
    PRIMARY KEY (receipt_uuid)
);

COMMENT ON TABLE n_point_user_review IS '네이버유저리뷰';

COMMENT ON COLUMN n_point_user_review.receipt_uuid IS '영수증 고유아이디';

COMMENT ON COLUMN n_point_user_review.user_uuid IS '유저고유아이디';

COMMENT ON COLUMN n_point_user_review.store_uid IS '가맹점고유아이디';

COMMENT ON COLUMN n_point_user_review.points IS '포인트 액수';

COMMENT ON COLUMN n_point_user_review.status IS '리뷰 작성 상태';

COMMENT ON COLUMN n_point_user_review.review_url IS '리뷰 URL';

COMMENT ON COLUMN n_point_user_review.reg_date IS '등록일시';

COMMENT ON COLUMN n_point_user_review.mod_date IS '수정일시';

COMMENT ON COLUMN n_point_user_review.expire_date IS '리뷰입력가능만료일';

CREATE TABLE service_code
(
    service_code  varchar(36)  NOT NULL,
    service_group varchar(10)  NOT NULL,
    service_name  varchar(255) NOT NULL,
    price         int4        ,
    PRIMARY KEY (service_code)
);

COMMENT ON TABLE service_code IS '서비스 코드 목록';

COMMENT ON COLUMN service_code.service_code IS '서비스 아이디';

COMMENT ON COLUMN service_code.service_group IS '서비스그룹';

COMMENT ON COLUMN service_code.service_name IS '서비스명';

COMMENT ON COLUMN service_code.price IS '이용료';

CREATE TABLE store
(
    store_uid      varchar(36)  NOT NULL,
    store_name     varchar(255) NOT NULL,
    business_no    varchar(50) ,
    ceo_name       varchar(50)  NOT NULL,
    ceo_ci         varchar(255) NOT NULL,
    ceo_citizen_no varchar(50)  NOT NULL,
    phone          varchar(50) ,
    phone_provider varchar(10)  NOT NULL,
    reference_uuid varchar(36) ,
    corperate_no   varchar(50)  NOT NULL,
    reg_date       timestamp    NOT NULL,
    ...                        ,
    PRIMARY KEY (store_uid)
);

COMMENT ON TABLE store IS '가맹점';

COMMENT ON COLUMN store.store_uid IS '가맹점 고유아이디';

COMMENT ON COLUMN store.store_name IS '가맹점명';

COMMENT ON COLUMN store.business_no IS '사업자번호';

COMMENT ON COLUMN store.ceo_name IS '대표자명';

COMMENT ON COLUMN store.ceo_ci IS 'ci';

COMMENT ON COLUMN store.ceo_citizen_no IS '주민번호';

COMMENT ON COLUMN store.phone IS '전화번호';

COMMENT ON COLUMN store.phone_provider IS '통신사코드';

COMMENT ON COLUMN store.reference_uuid IS '추천인아이디';

COMMENT ON COLUMN store.corperate_no IS '법인번호';

COMMENT ON COLUMN store.reg_date IS '등록일시';

CREATE TABLE store_billing
(
    billing_seq       int8        NOT NULL,
    store_uid         varchar(36) NOT NULL,
    token_uuid        uuid        NOT NULL,
    billing_amount    int4        NOT NULL,
    status            varchar(10) NOT NULL,
    bank_code         varchar(20),
    bank_account_no   varchar(50),
    bank_account_name varchar(50),
    mod_date          timestamp  ,
    reg_by            uuid        NOT NULL,
    mod_by            uuid       ,
    reg_date          timestamp   NOT NULL,
    PRIMARY KEY (billing_seq)
);

CREATE INDEX "idx_store_billing_store_uid" ON "public"."store_billing" USING BTREE ("store_uid");

COMMENT ON TABLE store_billing IS '가맹점 결제 정보 테이블';

COMMENT ON COLUMN store_billing.billing_seq IS '시퀀스';

COMMENT ON COLUMN store_billing.store_uid IS '가맹점 고유아이디';

COMMENT ON COLUMN store_billing.token_uuid IS '토큰 고유아이디';

COMMENT ON COLUMN store_billing.billing_amount IS '결제금액';

COMMENT ON COLUMN store_billing.status IS '상태';

COMMENT ON COLUMN store_billing.bank_code IS '환불 은행 코드';

COMMENT ON COLUMN store_billing.bank_account_no IS '계좌번호';

COMMENT ON COLUMN store_billing.bank_account_name IS '예금주';

COMMENT ON COLUMN store_billing.mod_date IS '수정일시';

COMMENT ON COLUMN store_billing.reg_by IS '등록인 고유아이디';

COMMENT ON COLUMN store_billing.mod_by IS '수정인 고유아이디';

COMMENT ON COLUMN store_billing.reg_date IS '등록일시';

CREATE TABLE store_coupon
(
    store_uid   varchar(36) NOT NULL,
    coupon_uuid uuid        NOT NULL,
    status      varchar(20) NOT NULL,
    commission  int4       ,
    reg_date    timestamp   NOT NULL,
    reg_by      uuid        NOT NULL,
    mod_date    timestamp   NOT NULL,
    mod_by      uuid        NOT NULL,
    PRIMARY KEY (store_uid)
);

COMMENT ON TABLE store_coupon IS '가맹점 쿠폰';

COMMENT ON COLUMN store_coupon.store_uid IS '가맹점 고유아이디';

COMMENT ON COLUMN store_coupon.coupon_uuid IS '쿠폰 고유아이디';

COMMENT ON COLUMN store_coupon.status IS '상태 ';

COMMENT ON COLUMN store_coupon.commission IS '수수료';

COMMENT ON COLUMN store_coupon.reg_date IS '등록일시';

COMMENT ON COLUMN store_coupon.reg_by IS '등록인 고유아이디';

COMMENT ON COLUMN store_coupon.mod_date IS '수정일시';

COMMENT ON COLUMN store_coupon.mod_by IS '수정인 고유아이디';

CREATE TABLE stores
(
    uid varchar(36) NOT NULL
);

COMMENT ON TABLE stores IS '다이나모 디비 가맹점';

COMMENT ON COLUMN stores.uid IS '가맹점 고유아이디';

CREATE TABLE tags
(
    uuid     varchar(36) NOT NULL,
    tagId    varchar     NOT NULL,
    storeUid varchar(36)
);

COMMENT ON TABLE tags IS '다이나모 디비 테이블';

COMMENT ON COLUMN tags.uuid IS '태그 고유아이디';

COMMENT ON COLUMN tags.tagId IS '태그아이디';

COMMENT ON COLUMN tags.storeUid IS '가맹점 아이디';

CREATE TABLE user_event_point
(
    waiting_seq         bigint       NOT NULL DEFAULT nextval,
    user_uuid           varchar(36)  NOT NULL,
    event_session_id    varchar(36)  NOT NULL,
    transaction_id      varchar(36)  NOT NULL,
    receipt_uuid        varchar(36)  NOT NULL,
    points              int4         NOT NULL,
    reg_date            timestamp    NOT NULL,
    advertisement_title varchar(255) NOT NULL,
    PRIMARY KEY (waiting_seq)
);

COMMENT ON TABLE user_event_point IS '사용자 이벤트 포인트 적립';

COMMENT ON COLUMN user_event_point.waiting_seq IS '시퀀스';

COMMENT ON COLUMN user_event_point.user_uuid IS '사용자 고유아이디';

COMMENT ON COLUMN user_event_point.event_session_id IS '이벤트 세션 고유아이디';

COMMENT ON COLUMN user_event_point.transaction_id IS '트랜잭션 아이디';

COMMENT ON COLUMN user_event_point.receipt_uuid IS '영수증 고유아이디';

COMMENT ON COLUMN user_event_point.points IS '적립 포인트';

COMMENT ON COLUMN user_event_point.reg_date IS '등록일시';

COMMENT ON COLUMN user_event_point.advertisement_title IS '광고명';

CREATE TABLE user_event_session
(
    user_uuid            varchar(36) NOT NULL,
    event_session_id     varchar(36) NOT NULL,
    advertisement_id     varchar(36) NOT NULL,
    begin_at            timestamp   NOT NULL,
    expire_at            timestamp  ,
    total_reserved_point int4       ,
    status               varchar(10) NOT NULL,
    reg_date             timestamp   NOT NULL,
    mod_date             time       ,
    PRIMARY KEY (user_uuid, event_session_id)
);

COMMENT ON TABLE user_event_session IS '사용자 이벤트 세션';

COMMENT ON COLUMN user_event_session.user_uuid IS '사용자 고유아이디';

COMMENT ON COLUMN user_event_session.event_session_id IS '이벤트 세션 고유아이디';

COMMENT ON COLUMN user_event_session.advertisement_id IS '광고명';

COMMENT ON COLUMN user_event_session.begin_at IS '시작일시';

COMMENT ON COLUMN user_event_session.expire_at IS '종료일시';

COMMENT ON COLUMN user_event_session.total_reserved_point IS '예약 포인트포인트';

COMMENT ON COLUMN user_event_session.status IS '진행상태';

COMMENT ON COLUMN user_event_session.reg_date IS '등록일시';

COMMENT ON COLUMN user_event_session.mod_date IS '수정일시';

ALTER TABLE n_point_user_review
    ADD CONSTRAINT FK_n_point_user_TO_n_point_user_review
        FOREIGN KEY (user_uuid)
            REFERENCES n_point_user (user_uuid);

ALTER TABLE n_point_tx_history
    ADD CONSTRAINT FK_n_point_pay_waiting_TO_n_point_tx_history
        FOREIGN KEY (waiting_seq)
            REFERENCES n_point_pay_waiting (seq);

ALTER TABLE n_point_store_service
    ADD CONSTRAINT FK_service_code_TO_n_point_store_service
        FOREIGN KEY (service_code)
            REFERENCES service_code (service_code);

ALTER TABLE merchant_receipt
    ADD CONSTRAINT FK_merchant_tag_TO_merchant_receipt
        FOREIGN KEY (merchant_store_id)
            REFERENCES merchant_tag (tag_id);

ALTER TABLE login_info
    ADD CONSTRAINT FK_admin_TO_login_info
        FOREIGN KEY (login_uuid)
            REFERENCES admin (uuid);

ALTER TABLE n_point_pay_waiting
    ADD CONSTRAINT FK_n_point_user_review_TO_n_point_pay_waiting
        FOREIGN KEY (receipt_uuid)
            REFERENCES n_point_user_review (receipt_uuid);

ALTER TABLE user_event_point
    ADD CONSTRAINT FK_user_event_session_TO_user_event_point
        FOREIGN KEY (user_uuid, event_session_id)
            REFERENCES user_event_session (user_uuid, event_session_id);

ALTER TABLE user_event_point
    ADD CONSTRAINT FK_n_point_pay_waiting_TO_user_event_point
        FOREIGN KEY (waiting_seq)
            REFERENCES n_point_pay_waiting (seq);

ALTER TABLE admin
    ADD CONSTRAINT FK_bz_agency_TO_admin
        FOREIGN KEY (agency_uuid)
            REFERENCES bz_agency (uuid);

ALTER TABLE bz_agency_store
    ADD CONSTRAINT FK_bz_agency_TO_bz_agency_store
        FOREIGN KEY (bz_agency_uuid)
            REFERENCES bz_agency (uuid);

ALTER TABLE n_point_store
    ADD CONSTRAINT FK_store_TO_n_point_store
        FOREIGN KEY (store_uid)
            REFERENCES store (store_uid);

ALTER TABLE n_point_store_service
    ADD CONSTRAINT FK_n_point_store_TO_n_point_store_service
        FOREIGN KEY (store_uid)
            REFERENCES n_point_store (store_uid);

ALTER TABLE merchant_billing_token
    ADD CONSTRAINT FK_merchant_info_TO_merchant_billing_token
        FOREIGN KEY (merchant_info_uuid)
            REFERENCES merchant_info (uuid);

ALTER TABLE n_point_store
    ADD CONSTRAINT FK_merchant_info_TO_n_point_store
        FOREIGN KEY (merchant_info_uuid)
            REFERENCES merchant_info (uuid);

ALTER TABLE merchant_info
    ADD CONSTRAINT FK_bz_agency_TO_merchant_info
        FOREIGN KEY (bz_agency_uuid)
            REFERENCES bz_agency (uuid);

ALTER TABLE merchant_tag
    ADD CONSTRAINT FK_merchant_group_TO_merchant_tag
        FOREIGN KEY (merchant_group_id)
            REFERENCES merchant_group (merchant_group_id);

ALTER TABLE store_billing
    ADD CONSTRAINT FK_store_TO_store_billing
        FOREIGN KEY (store_uid)
            REFERENCES store (store_uid);

ALTER TABLE store_billing
    ADD CONSTRAINT FK_merchant_billing_token_TO_store_billing
        FOREIGN KEY (token_uuid)
            REFERENCES merchant_billing_token (token_uuid);

ALTER TABLE store_coupon
    ADD CONSTRAINT FK_coupon_TO_store_coupon
        FOREIGN KEY (coupon_uuid)
            REFERENCES coupon (uuid);

ALTER TABLE store_coupon
    ADD CONSTRAINT FK_store_TO_store_coupon
        FOREIGN KEY (store_uid)
            REFERENCES store (store_uid);
