create table public.cpoint_history
(
    request_no        varchar     not null
        primary key,
    rlvt_de           varchar(10) not null,
    mthd              varchar(10) not null,
    encpt_cd          varchar(10),
    totcnt            integer,
    created_date      varchar(8),
    is_batch_complete boolean default false
);

comment on table public.cpoint_history is '탄소중립 실천내역';

comment on column public.cpoint_history.request_no is '환경공단생성고아이디';

comment on column public.cpoint_history.rlvt_de is '환경공단요청날짜';

comment on column public.cpoint_history.mthd is '사용자전송타입';

comment on column public.cpoint_history.encpt_cd is '데이터암호화여부';

comment on column public.cpoint_history.totcnt is '목록 수';

comment on column public.cpoint_history.created_date is '등록일';

comment on column public.cpoint_history.is_batch_complete is '배치 작업여부';

alter table public.cpoint_history
    owner to "allink-admin";

create table public.cpoint_user
(
    mtchg_id  varchar(10) not null
        primary key,
    ci        varchar(200),
    user_uuid varchar(36),
    reg_date  timestamp(6),
    mod_date  timestamp,
    delete_yn boolean
);

comment on table public.cpoint_user is '탄소중립 참여자';

comment on column public.cpoint_user.mtchg_id is '참여자 고유아이디';

comment on column public.cpoint_user.ci is 'CI';

comment on column public.cpoint_user.user_uuid is '유저고유아이디';

comment on column public.cpoint_user.reg_date is '등록일시';

comment on column public.cpoint_user.mod_date is '수정일시';

alter table public.cpoint_user
    owner to "allink-admin";

create unique index "IDX_cpoint_user_userUuid"
    on public.cpoint_user (user_uuid)
    where (user_uuid IS NOT NULL);

create index "IDX_cpoint_user_ci"
    on public.cpoint_user (ci);

create table public.issued_coupon_count
(
    coupon_id    varchar(36)       not null,
    issuer_id    varchar           not null,
    issuer_type  varchar           not null,
    issued_count integer default 0 not null,
    primary key (coupon_id, issuer_id, issuer_type)
);

alter table public.issued_coupon_count
    owner to "allink-admin";

create table public.receipt_carbon_reduction
(
    receipt_uuid     varchar not null
        constraint receipt_carbon_saving_receipt_uuid_pk
            primary key,
    carbon_reduction double precision
);

alter table public.receipt_carbon_reduction
    owner to "allink-admin";

create table public.receipt_item
(
    item_uid           varchar(255) not null,
    sale_count         integer      not null,
    sale_price         integer      not null,
    issued_receipt_uid varchar(36)  not null,
    store_uid          varchar(36)  not null,
    primary key (store_uid, issued_receipt_uid, item_uid)
);

alter table public.receipt_item
    owner to "allink-admin";

create table public.sale_item
(
    item_uid   varchar(255) not null
        primary key,
    item_price integer      not null,
    item_name  varchar      not null,
    sold_count integer      not null,
    item_code  varchar,
    reg_date   timestamp(6),
    mod_date   timestamp(6)
);

alter table public.sale_item
    owner to "allink-admin";

create table public."user"
(
    uuid             varchar(36) not null
        primary key,
    name             varchar(100),
    status           varchar(10),
    phone            varchar(50),
    gender           varchar(50),
    ci               varchar(255)
        constraint uk_user_ci
            unique,
    birthday         varchar(255),
    local_yn         varchar(1),
    email            varchar(255),
    last_login_date  timestamp(6),
    reg_date         timestamp(6),
    mod_date         timestamp(6),
    role             varchar(11),
    phone_carrier_cd varchar(2),
    join_social_type varchar(11),
    nickname         varchar(255),
    mtchg_id         varchar(10),
    cpoint_reg_type  varchar(10) default 'N'::character varying,
    cpoint_reg_date  timestamp(6)
);

alter table public."user"
    owner to "allink-admin";

create table public.user_browser
(
    uuid          varchar(36) not null,
    browser_id    varchar(36) not null,
    refresh_token varchar(200),
    social_type   varchar(10),
    social_id     varchar(100),
    status        varchar(2),
    reg_date      timestamp(6),
    mod_date      timestamp(6),
    use_yn        varchar(1),
    primary key (uuid, browser_id)
);

alter table public.user_browser
    owner to "allink-admin";

create table public.user_coupon
(
    user_uuid          varchar(36)           not null,
    issued_receipt_uid varchar               not null,
    coupon_id          varchar(36)           not null,
    coupon_title       varchar               not null,
    coupon_content     varchar               not null,
    icon_url           varchar               not null,
    expiry_date        date                  not null,
    use_yn             boolean default false not null,
    use_date           timestamp(6),
    use_start_date     date,
    coupon_issuer_uid  varchar(36),
    primary key (user_uuid, issued_receipt_uid, coupon_id)
);

alter table public.user_coupon
    owner to "allink-admin";

create table public.user_login_history
(
    seq          bigint default nextval('user_login_history_seq_seq'::regclass) not null
        primary key,
    login_type   varchar(2),
    login_id     varchar(100),
    login_result varchar(1),
    user_uuid    varchar(36),
    reg_date     timestamp(6)
);

alter table public.user_login_history
    owner to "allink-admin";

create table public.user_social
(
    uuid        varchar(36)                                              not null,
    social_type varchar(10),
    status      varchar(10),
    reg_date    timestamp(6),
    mod_date    timestamp(6),
    social_id   varchar(100),
    nickname    varchar(255),
    birthday    varchar(50),
    gender      varchar(50),
    name        varchar(255),
    email       varchar(255),
    seq         integer default nextval('user_social_seq_seq'::regclass) not null
        primary key,
    ci          varchar(255),
    phone       varchar(255)
);

comment on table public.user_social is '이용자 소셜';

comment on column public.user_social.uuid is '이용자 고유아이디';

comment on column public.user_social.social_type is '소셜 종류';

comment on column public.user_social.status is '상태';

comment on column public.user_social.reg_date is '등록일시';

comment on column public.user_social.mod_date is '수정일시';

comment on column public.user_social.social_id is '소셜 아이디';

comment on column public.user_social.nickname is '닉네임';

comment on column public.user_social.birthday is '생년월일';

comment on column public.user_social.gender is '성별';

comment on column public.user_social.name is '이름';

comment on column public.user_social.email is '이메일';

comment on column public.user_social.seq is '순번';

alter table public.user_social
    owner to "allink-admin";

create index "IDX_user_social_uuid"
    on public.user_social (uuid);

create table public.user_terms
(
    user_uuid  varchar(36) not null,
    type       varchar(10) not null,
    version    varchar(10) not null,
    agree_date timestamp(6),
    constraint user_term_pkey
        primary key (user_uuid, type, version)
);

comment on table public.user_terms is '이용자 약관';

comment on column public.user_terms.user_uuid is '이용자 고유아이디';

comment on column public.user_terms.type is '종류';

comment on column public.user_terms.version is '버전';

comment on column public.user_terms.agree_date is '동의일시';

alter table public.user_terms
    owner to "allink-admin";

create table public.user_verify_identity
(
    uuid              varchar(36) not null
        primary key,
    name              varchar(255),
    type              varchar(10),
    phone             varchar(50),
    mobile_carrier_cd varchar(2),
    gender            varchar(50),
    ci                varchar(255),
    birthday          varchar(50),
    local_yn          varchar(1),
    reg_date          timestamp(6),
    mod_date          timestamp(6)
);

alter table public.user_verify_identity
    owner to "allink-admin";

create table public.user_withdrawal
(
    user_uuid   varchar(36) not null
        primary key,
    reason_type varchar(1),
    reason      varchar(200),
    reg_date    timestamp(6)
);

comment on table public.user_withdrawal is '이용자 탈퇴';

comment on column public.user_withdrawal.user_uuid is '이용자 고유아이디';

comment on column public.user_withdrawal.reason_type is '탈퇴사유 구분';

comment on column public.user_withdrawal.reason is '탈퇴사유 기타시(사유)';

comment on column public.user_withdrawal.reg_date is '등록일시';

alter table public.user_withdrawal
    owner to "allink-admin";

create table public.batch_job_instance
(
    job_instance_id bigint       not null
        primary key,
    version         bigint,
    job_name        varchar(100) not null,
    job_key         varchar(32)  not null,
    constraint job_inst_un
        unique (job_name, job_key)
);

alter table public.batch_job_instance
    owner to "allink-admin";

create table public.batch_job_execution
(
    job_execution_id bigint    not null
        primary key,
    version          bigint,
    job_instance_id  bigint    not null
        constraint job_inst_exec_fk
            references public.batch_job_instance,
    create_time      timestamp not null,
    start_time       timestamp,
    end_time         timestamp,
    status           varchar(10),
    exit_code        varchar(2500),
    exit_message     varchar(2500),
    last_updated     timestamp
);

alter table public.batch_job_execution
    owner to "allink-admin";

create table public.batch_job_execution_params
(
    job_execution_id bigint       not null
        constraint job_exec_params_fk
            references public.batch_job_execution,
    parameter_name   varchar(100) not null,
    parameter_type   varchar(100) not null,
    parameter_value  varchar(2500),
    identifying      char         not null
);

alter table public.batch_job_execution_params
    owner to "allink-admin";

create table public.batch_step_execution
(
    step_execution_id  bigint       not null
        primary key,
    version            bigint       not null,
    step_name          varchar(100) not null,
    job_execution_id   bigint       not null
        constraint job_exec_step_fk
            references public.batch_job_execution,
    create_time        timestamp    not null,
    start_time         timestamp,
    end_time           timestamp,
    status             varchar(10),
    commit_count       bigint,
    read_count         bigint,
    filter_count       bigint,
    write_count        bigint,
    read_skip_count    bigint,
    write_skip_count   bigint,
    process_skip_count bigint,
    rollback_count     bigint,
    exit_code          varchar(2500),
    exit_message       varchar(2500),
    last_updated       timestamp
);

alter table public.batch_step_execution
    owner to "allink-admin";

create table public.batch_step_execution_context
(
    step_execution_id  bigint        not null
        primary key
        constraint step_exec_ctx_fk
            references public.batch_step_execution,
    short_context      varchar(2500) not null,
    serialized_context text
);

alter table public.batch_step_execution_context
    owner to "allink-admin";

create table public.batch_job_execution_context
(
    job_execution_id   bigint        not null
        primary key
        constraint job_exec_ctx_fk
            references public.batch_job_execution,
    short_context      varchar(2500) not null,
    serialized_context text
);

alter table public.batch_job_execution_context
    owner to "allink-admin";

create table public.n_point_pay_waiting
(
    seq          bigint      default nextval('n_point_pay_waiting_seq'::regclass) not null
        primary key,
    reg_date     timestamp(6)                                                     not null,
    receipt_uuid varchar                                                          not null,
    provide_case varchar(10) default 'REVIEW'::character varying                  not null
);

comment on table public.n_point_pay_waiting is '포인트 지급 연동 대기열';

comment on column public.n_point_pay_waiting.seq is '시퀀스';

comment on column public.n_point_pay_waiting.reg_date is '등록일시';

comment on column public.n_point_pay_waiting.receipt_uuid is '영수증 고유아이디';

comment on column public.n_point_pay_waiting.provide_case is '지급 사유';

alter table public.n_point_pay_waiting
    owner to "allink-admin";

create index "IDX_n_point_pay_waiting_receiptUuid"
    on public.n_point_pay_waiting (receipt_uuid);

create index "IDX_n_point_pay_waiting_regDate"
    on public.n_point_pay_waiting (reg_date desc);

create table public.n_point_user
(
    user_uuid           varchar           not null
        constraint n_point_review_user_pkey
            primary key,
    total_review_points integer default 0 not null,
    total_review_count  integer default 0 not null,
    user_key            varchar,
    total_event_points  integer default 0,
    total_event_count   integer default 0
);

comment on table public.n_point_user is '네이버포인트리뷰유저';

comment on column public.n_point_user.user_uuid is '유저고유아이디';

comment on column public.n_point_user.total_review_points is '총 리뷰 포인트';

comment on column public.n_point_user.total_review_count is '총리뷰카운트';

comment on column public.n_point_user.user_key is '유저 연동 키';

comment on column public.n_point_user.total_event_points is '이벤트 포인트 합계';

comment on column public.n_point_user.total_event_count is '이벤트 참여 합계';

alter table public.n_point_user
    owner to "allink-admin";

create table public.n_point_store
(
    store_uid          varchar                                           not null
        primary key,
    reserved_points    integer      default 0                            not null,
    review_points      integer      default 0                            not null,
    cumulative_points  integer      default 0                            not null,
    status             varchar      default 'PENDING'::character varying not null,
    service_start_at   timestamp(6),
    service_end_at     timestamp(6),
    point_renewal_type varchar,
    reg_date           timestamp(6) default CURRENT_TIMESTAMP            not null,
    mod_date           timestamp(6)
);

comment on table public.n_point_store is '네이버포인트가맹점';

comment on column public.n_point_store.store_uid is '가맹점고유아이디';

comment on column public.n_point_store.reserved_points is '포인트 지급 용 적립 포인트';

comment on column public.n_point_store.review_points is '리뷰작성시 제공할 포인트';

comment on column public.n_point_store.cumulative_points is '누적 적립 포인트';

comment on column public.n_point_store.status is '상태 (ACTIVE,PENDING,STOP)';

comment on column public.n_point_store.service_start_at is '서비스 시작일시';

comment on column public.n_point_store.service_end_at is '서비스 종료일시';

comment on column public.n_point_store.point_renewal_type is '지급할 포인트 소진 시 갱신타입';

comment on column public.n_point_store.reg_date is '등록일시';

comment on column public.n_point_store.mod_date is '수정일시';

alter table public.n_point_store
    owner to "allink-admin";

create table public.n_point_store_pay_history
(
    store_uid    varchar           not null
        constraint fk_n_point_store_to_n_point_store_pay_history
            references public.n_point_store,
    points       integer           not null,
    amounts      integer default 0 not null,
    reg_date     timestamp(6)      not null,
    receipt_uuid varchar           not null
        primary key,
    expire_date  timestamp(6)      not null
);

comment on table public.n_point_store_pay_history is '네이버 포인트 가맹점 지급 이력';

comment on column public.n_point_store_pay_history.store_uid is '가맹점고유아이디';

comment on column public.n_point_store_pay_history.points is '지급 포인트 액수';

comment on column public.n_point_store_pay_history.amounts is '지급 당시 잔액';

comment on column public.n_point_store_pay_history.reg_date is '등록일시';

comment on column public.n_point_store_pay_history.receipt_uuid is '영수증 고유아이디';

comment on column public.n_point_store_pay_history.expire_date is '만료일시';

alter table public.n_point_store_pay_history
    owner to "allink-admin";

create index "IDX_n_point_store_pay_history_storeUid"
    on public.n_point_store_pay_history (store_uid);

create table public.n_point_tx_history
(
    tx_no       varchar,
    reg_date    timestamp(6),
    result_code varchar not null,
    waiting_seq bigint  not null
        primary key
        constraint fk_n_point_pay_waiting_to_n_point_tx_history
            references public.n_point_pay_waiting
);

comment on table public.n_point_tx_history is '포인트 지급 연동 이력';

comment on column public.n_point_tx_history.tx_no is '연동 거래번호';

comment on column public.n_point_tx_history.reg_date is '연동일시';

comment on column public.n_point_tx_history.result_code is '연동 결과 코드';

comment on column public.n_point_tx_history.waiting_seq is '대기열 시퀀스';

alter table public.n_point_tx_history
    owner to "allink-admin";

create table public.n_point_user_review
(
    user_uuid    varchar(36)  not null,
    store_uid    varchar(36)  not null,
    receipt_uuid varchar(36)  not null
        primary key,
    status       varchar(10)  not null,
    review_url   varchar(255),
    reg_date     timestamp(6) not null,
    mod_date     timestamp(6),
    points       integer,
    expire_date  timestamp
);

comment on table public.n_point_user_review is '네이버유저리뷰';

comment on column public.n_point_user_review.user_uuid is '유저고유아이디';

comment on column public.n_point_user_review.store_uid is '가맹점고유아이디';

comment on column public.n_point_user_review.receipt_uuid is '영수증 고유아이디';

comment on column public.n_point_user_review.status is '리뷰 작성 상태';

comment on column public.n_point_user_review.review_url is '리뷰 URL';

comment on column public.n_point_user_review.reg_date is '등록일시';

comment on column public.n_point_user_review.mod_date is '수정일시';

comment on column public.n_point_user_review.points is '지급 포인트';

comment on column public.n_point_user_review.expire_date is '리뷰등록가능만료일시';

alter table public.n_point_user_review
    owner to "allink-admin";

create index "IDX_n_point_user_review_userUuid"
    on public.n_point_user_review (user_uuid, receipt_uuid);

create index "IDX_n_point_user_review_storeUid"
    on public.n_point_user_review (store_uid);

create table public.service_code
(
    service_code  varchar(30)  not null
        primary key,
    service_group varchar(10)  not null,
    service_name  varchar(255) not null,
    price         integer,
    status        varchar(10)
);

comment on table public.service_code is '서비스 코드 목록';

comment on column public.service_code.service_code is '서비스 아이디';

comment on column public.service_code.service_group is '서비스그룹';

comment on column public.service_code.service_name is '서비스명';

comment on column public.service_code.price is '이용료';

comment on column public.service_code.status is '상태';

alter table public.service_code
    owner to "allink-admin";

create table public.merchant_application
(
    merchant_application_uuid varchar(36) not null
        constraint store_contract_application_pkey
            primary key,
    business_no               varchar(50) not null,
    ceo_name                  varchar(50),
    ceo_ci                    varchar(255),
    ceo_citizen_no            varchar(50),
    phone                     varchar(50),
    reference_uuid            varchar(36),
    corperate_no              varchar(50),
    reg_date                  timestamp   not null,
    gender                    varchar(1),
    phone_provider            varchar(10),
    birthday                  varchar(50),
    mod_date                  timestamp
);

comment on table public.merchant_application is '가맹점 가입';

comment on column public.merchant_application.merchant_application_uuid is '고유아이디';

comment on column public.merchant_application.business_no is '사업자번호';

comment on column public.merchant_application.ceo_name is '대표자명';

comment on column public.merchant_application.ceo_ci is 'ci';

comment on column public.merchant_application.ceo_citizen_no is '주민번호';

comment on column public.merchant_application.phone is '전화번호';

comment on column public.merchant_application.reference_uuid is '추천인아이디';

comment on column public.merchant_application.corperate_no is '법인번호';

comment on column public.merchant_application.reg_date is '등록일시';

alter table public.merchant_application
    owner to "allink-admin";

create index "IDX_store_contract_application_bzno"
    on public.merchant_application (business_no);

create table public.store_contract
(
    contract_uuid  uuid        not null,
    service_code   varchar(10) not null,
    service_charge integer     not null,
    status         varchar(10) not null,
    reg_date       timestamp,
    mod_date       timestamp,
    constraint store_contract_service_pkey
        primary key (contract_uuid, service_code)
);

comment on table public.store_contract is '가맹점 이용 서비스';

comment on column public.store_contract.contract_uuid is '계약 고유번호';

comment on column public.store_contract.service_code is '서비스 아이디';

comment on column public.store_contract.service_charge is '서비스 이용 비용';

comment on column public.store_contract.status is '상태';

alter table public.store_contract
    owner to "allink-admin";

create index "store_contract_service_serviceId"
    on public.store_contract (service_code);

create table public.merchant_billing_token
(
    token_uuid                uuid         not null
        primary key,
    merchant_application_uuid uuid         not null,
    token                     varchar(255) not null,
    tokeninfo                 varchar(255),
    status                    varchar(10)  not null,
    reg_date                  timestamp
);

comment on table public.merchant_billing_token is '가맹점 결제 토큰';

comment on column public.merchant_billing_token.token_uuid is '토큰 고유아이디';

comment on column public.merchant_billing_token.merchant_application_uuid is '계약자 고유아이디';

comment on column public.merchant_billing_token.token is 'sk결제토큰';

comment on column public.merchant_billing_token.tokeninfo is '토큰추가정보';

comment on column public.merchant_billing_token.status is '상태';

comment on column public.merchant_billing_token.reg_date is '등록일시';

alter table public.merchant_billing_token
    owner to "allink-admin";

create index "IDX_merchant_billing_token_Mid"
    on public.merchant_billing_token (merchant_application_uuid);

create table public.contract
(
    contract_uuid     uuid        not null
        primary key,
    token_uuid        uuid        not null,
    store_uid         varchar(36) not null,
    service_amount    integer     not null,
    reward_commission integer     not null,
    reward_deposit    integer     not null,
    status            varchar(10) not null,
    billing_day       varchar(2),
    bank_code         varchar(10),
    bank_account      varchar(50),
    reg_date          timestamp   not null,
    mod_date          timestamp,
    reward_points     integer
);

alter table public.contract
    owner to "allink-admin";

create index "IDX_contract_storeUid"
    on public.contract (store_uid);

create index "IDX_contract_tokenId"
    on public.contract (token_uuid);

create table public.kspay_request
(
    request_seq        integer      default nextval('kspay_request_request_seq_seq'::regclass) not null
        primary key,
    store_uid          varchar(64)                                                             not null,
    merchant_userid    varchar(20),
    business_number    varchar(10),
    token_data         varchar(96)                                                             not null,
    trd_type           char(2)                                                                 not null,
    installment_period varchar(2)   default '00'::character varying                            not null,
    trx_amount         integer      default 0                                                  not null,
    extra_filler       varchar(198),
    state_code         char(2)      default '00'::bpchar                                       not null,
    cancle_stat_code   char(2)      default '00'::bpchar                                       not null,
    reg_date           timestamp(6) default CURRENT_TIMESTAMP                                  not null,
    mod_date           timestamp(6)
);

comment on table public.kspay_request is 'Koces SKPAY 요청 테이블';

comment on column public.kspay_request.request_seq is '자동 증가하는 기본 키';

comment on column public.kspay_request.store_uid is '가맹점 ID';

comment on column public.kspay_request.merchant_userid is '상점 userid (숫자만)';

comment on column public.kspay_request.business_number is '사업자 번호';

comment on column public.kspay_request.token_data is '토큰 데이터';

comment on column public.kspay_request.trd_type is '업무구분 (S3: 승인요청, S4: 취소요청)';

comment on column public.kspay_request.installment_period is '할부기간';

comment on column public.kspay_request.trx_amount is '거래 금액';

comment on column public.kspay_request.extra_filler is '여유 필드';

comment on column public.kspay_request.state_code is '상태코드 (00: 요청 완료, 10: 응답 완료, 99: 미사용)';

comment on column public.kspay_request.cancle_stat_code is '취소 상태코드 (00: 기본, 01: ACK 망상취소, 02: EOT 망상취소)';

comment on column public.kspay_request.reg_date is '등록 일시';

comment on column public.kspay_request.mod_date is '수정 일시';

alter table public.kspay_request
    owner to "allink-admin";

create table public.kspay_response
(
    response_seq            integer      default nextval('kspay_response_response_seq_seq'::regclass) not null
        primary key,
    request_seq             bigint                                                                    not null,
    store_uid               varchar(64)                                                               not null,
    merchant_userid         varchar(20),
    business_number         varchar(10),
    trd_type                char(2)                                                                   not null,
    trx_amount              integer      default 0                                                    not null,
    trd_no                  varchar(10)                                                               not null,
    mch_data                varchar(20),
    ans_code                varchar(4)                                                                not null,
    van_trx_id              varchar(12),
    approval_number         varchar(12),
    card_response_code      char(2),
    response_message        varchar(32),
    issuer_code             varchar(12),
    issuer_name             varchar(12),
    acquier_code            varchar(4),
    acquirer_name           varchar(8),
    merchant_number         varchar(16),
    ddc_flag                char,
    edi_flag                char,
    card_classification     char,
    van_req_seq_no          varchar(20),
    answer_code             varchar(8),
    ans_msg                 varchar(200),
    van_code                char(2),
    card_expiry_date        varchar(64),
    masked_card_number      varchar(32),
    plcc                    varchar(10),
    van_generated_unique_id varchar(20),
    approval_van_trx_id     varchar(12),
    filler                  varchar(60),
    state_code              char(2)      default '00'::bpchar                                         not null,
    cancle_stat_code        char(2)      default '00'::bpchar                                         not null,
    cancle_request_seq      bigint,
    reg_date                timestamp(6) default CURRENT_TIMESTAMP                                    not null,
    mod_date                timestamp(6),
    approval_time           varchar(14)
);

comment on table public.kspay_response is 'Koces SKPAY 응답 테이블';

comment on column public.kspay_response.response_seq is '자동 증가하는 기본 키';

comment on column public.kspay_response.request_seq is '요청 sequence 번호';

comment on column public.kspay_response.store_uid is '가맹점 ID';

comment on column public.kspay_response.merchant_userid is '상점 userid (숫자만)';

comment on column public.kspay_response.business_number is '사업자 번호';

comment on column public.kspay_response.trd_type is '업무구분 (S3: 승인요청, S4: 취소요청)';

comment on column public.kspay_response.trx_amount is '거래 금액';

comment on column public.kspay_response.trd_no is '거래일련번호';

comment on column public.kspay_response.mch_data is '가맹점 데이터(고객이름 또는 고객 관리 번호)';

comment on column public.kspay_response.ans_code is '응답 코드';

comment on column public.kspay_response.van_trx_id is 'Van 서 부여하는 거래 고유번호';

comment on column public.kspay_response.approval_number is '승인번호';

comment on column public.kspay_response.card_response_code is '카드사 응답 코드';

comment on column public.kspay_response.response_message is '응답 메시지';

comment on column public.kspay_response.issuer_code is '발급사 코드';

comment on column public.kspay_response.issuer_name is '발급사 명';

comment on column public.kspay_response.acquier_code is '매입사 코드';

comment on column public.kspay_response.acquirer_name is '매입사 명';

comment on column public.kspay_response.merchant_number is '가맹점 번호';

comment on column public.kspay_response.ddc_flag is 'DDC 여부';

comment on column public.kspay_response.edi_flag is 'EDI 여부';

comment on column public.kspay_response.card_classification is '카드구분(1:신용, 2:체크, 3:기프트, 4:선불)';

comment on column public.kspay_response.van_req_seq_no is 'VAN 요청 번호(SK 전송값)';

comment on column public.kspay_response.answer_code is '응답코드';

comment on column public.kspay_response.ans_msg is '기관 응답메시지';

comment on column public.kspay_response.van_code is 'VAN사 코드 (01:KOCES, 02:KIS, 03:스마트로)';

comment on column public.kspay_response.card_expiry_date is '카드유효기간';

comment on column public.kspay_response.masked_card_number is '마스킹 결제수단';

comment on column public.kspay_response.plcc is '제휴 결제수단 구분번호';

comment on column public.kspay_response.van_generated_unique_id is 'VAN 에서 SK로 요청 시에 생성하는 고유번호';

comment on column public.kspay_response.approval_van_trx_id is '승인시 VAN 거래고유번호( VAN (11ST)에서 승인시 넘어온 VAN 거래고유번호(이 값으로 일반 전문으로 거래고유키취소 진행))';

comment on column public.kspay_response.filler is '여유필드';

comment on column public.kspay_response.state_code is '상태코드(00:요청, 10:응답, 99:미사용)';

comment on column public.kspay_response.cancle_stat_code is '취소상태코드 (00:default, 01: ACK 망상취소, 02: EOT 망상취소)';

comment on column public.kspay_response.reg_date is '등록 일시';

comment on column public.kspay_response.mod_date is '수정 일시';

comment on column public.kspay_response.approval_time is '승인 시간';

alter table public.kspay_response
    owner to "allink-admin";

create table public.merchant_group
(
    token_key           varchar(255)                not null,
    merchant_group_id   varchar(36)                 not null
        constraint receipt_merchants_copy1_pkey
            primary key,
    remote_ips          text                        not null,
    service_start_at    timestamp                   not null,
    service_end_at      timestamp(6) with time zone not null,
    status              varchar(10)                 not null,
    receipt_width       smallint,
    authorities         varchar(255)                not null,
    reg_date            timestamp                   not null,
    mod_date            timestamp,
    receipt_type        varchar(20),
    merchant_group_name varchar(50),
    app_link            varchar(255),
    expire_sec          integer
);

comment on column public.merchant_group.app_link is '자체 앱 링크';

comment on column public.merchant_group.expire_sec is '유효 세턴드';

alter table public.merchant_group
    owner to "allink-admin";

create index "IDX_merchant_group_tokenKey"
    on public.merchant_group (token_key);

create table public.merchant_receipt
(
    receipt_uuid      uuid         not null
        constraint receipt_merchants_payloads_copy1_pkey
            primary key,
    reg_date          timestamp(6) not null,
    merchant_store_id varchar(50)  not null,
    payload           json         not null,
    device_id         varchar(50)  not null,
    trx_id            varchar(36)
);

alter table public.merchant_receipt
    owner to "allink-admin";

create index receipt_merchants_payloads_merchant_id_pos_no_copy1
    on public.merchant_receipt (merchant_store_id, device_id);

create index "IDX_merchant_receipt_trxId"
    on public.merchant_receipt (trx_id)
    where (trx_id IS NOT NULL);

create table public.contract_billing
(
    billing_seq    bigint default nextval('contract_billing_pk_seq'::regclass) not null
        primary key,
    contract_uuid  varchar(36)                                                 not null,
    billing_amount integer                                                     not null,
    billing_type   varchar(10)                                                 not null,
    status         varchar(10)                                                 not null,
    reg_date       timestamp                                                   not null,
    mod_date       timestamp
);

comment on table public.contract_billing is '계약건 결제 내역';

comment on column public.contract_billing.billing_seq is '순번';

comment on column public.contract_billing.contract_uuid is '계약 고유번호';

comment on column public.contract_billing.billing_amount is '결제금액';

comment on column public.contract_billing.billing_type is '결제 사유(SERVICE|RECHARGE)';

comment on column public.contract_billing.status is '상태';

comment on column public.contract_billing.reg_date is '등록일시';

comment on column public.contract_billing.mod_date is '수정일시';

alter table public.contract_billing
    owner to "allink-admin";

create table public.merchant_receipt_received
(
    merchant_receipt_uuid uuid not null
        primary key,
    reg_date              timestamp
);

alter table public.merchant_receipt_received
    owner to "allink-admin";

create table public.admin_merchant
(
    admin_uuid    uuid not null,
    merchant_uuid uuid not null
);

comment on table public.admin_merchant is '관리자 머천트 목록';

comment on column public.admin_merchant.admin_uuid is '고유아이디';

comment on column public.admin_merchant.merchant_uuid is '머천트 고유아이디';

alter table public.admin_merchant
    owner to "allink-admin";

create table public.admin
(
    uuid        uuid        not null
        constraint admin_user_pkey
            primary key,
    login_id    varchar(100),
    password    varchar(255),
    full_name   varchar(50) not null,
    role        varchar(20) not null,
    phone       varchar(15) not null,
    email       varchar(100),
    reg_date    timestamp   not null,
    mod_date    timestamp,
    status      varchar(20) not null,
    reg_by      uuid,
    mod_by      uuid,
    agency_uuid uuid
);

comment on table public.admin is '관리자 계정';

comment on column public.admin.uuid is '고유아이디';

comment on column public.admin.login_id is '로그인아이디';

comment on column public.admin.password is '패스워드';

comment on column public.admin.full_name is '이름';

comment on column public.admin.role is '권한';

comment on column public.admin.phone is '전화번호';

comment on column public.admin.email is '이메일';

comment on column public.admin.reg_date is '등록일시';

comment on column public.admin.mod_date is '수정일시';

alter table public.admin
    owner to "allink-admin";

create index "IDX_admin_agencyUuid"
    on public.admin (agency_uuid)
    where (agency_uuid IS NOT NULL);

create unique index "IDX_admin_phone"
    on public.admin (phone)
    where (phone IS NOT NULL);

create table public.login_info
(
    login_uuid        uuid        not null
        constraint login_user_pkey
            primary key,
    user_uuid         uuid        not null,
    verification_code varchar(10) not null,
    expire_date       timestamp   not null,
    status            varchar(20) not null,
    login_date        timestamp
);

comment on table public.login_info is '로그인 인증코드';

comment on column public.login_info.login_uuid is '로그인 고유아이디';

comment on column public.login_info.user_uuid is '유저 uuid';

comment on column public.login_info.verification_code is '인증코드';

comment on column public.login_info.expire_date is '만료시간';

comment on column public.login_info.status is '상태';

comment on column public.login_info.login_date is '로그인시간';

alter table public.login_info
    owner to "allink-admin";

create index "IDX_login_info_userUuid"
    on public.login_info (user_uuid);

create table public.receipt_issue
(
    store_uid          varchar(36)  not null,
    issued_receipt_uid varchar(36)  not null,
    issue_date         timestamp(6) not null,
    user_uid           varchar(36)  not null,
    receipt_type       varchar(20)  not null,
    receipt_amount     integer,
    origin_issue_id    varchar(30),
    tag_id             varchar(30),
    advertisement_uuid uuid,
    primary key (issued_receipt_uid, store_uid)
);

comment on column public.receipt_issue.advertisement_uuid is '광고아이디';

alter table public.receipt_issue
    owner to "allink-admin";

create unique index "IDX_receipt_issue_originId"
    on public.receipt_issue (origin_issue_id)
    where (origin_issue_id IS NOT NULL);

create table public.receipt_issue_trace
(
    reg_date         timestamp(6) not null,
    user_uuid        varchar(36)  not null,
    partner_req_uuid varchar(36),
    receipt_uuid     varchar(36),
    cpoint_reg_yn    boolean,
    mod_date         timestamp(6),
    is_deleted       boolean,
    created_at       timestamp(6) default CURRENT_TIMESTAMP,
    cpoint_reg_type  varchar(10),
    constraint receipt_issue_user_pkey
        primary key (user_uuid, reg_date)
);

alter table public.receipt_issue_trace
    owner to "allink-admin";

create index "IDX_receipt_issue_trace_receiptUuid"
    on public.receipt_issue_trace (receipt_uuid);

create index "IDX_receipt_issue_trace_regDate"
    on public.receipt_issue_trace (reg_date);

create index receipt_issue_trace_partner_req_uuid_index
    on public.receipt_issue_trace (partner_req_uuid);

create table public.advertisement
(
    uuid              uuid        not null
        primary key,
    merchant_group_id varchar(20) not null,
    title             varchar(255),
    reg_date          timestamp   not null,
    mod_date          timestamp
);

comment on table public.advertisement is '광고 ';

comment on column public.advertisement.uuid is '광고 고유아이디';

comment on column public.advertisement.merchant_group_id is '머천트 그룹 아이디';

comment on column public.advertisement.title is '광고명';

comment on column public.advertisement.reg_date is '등록일시';

comment on column public.advertisement.mod_date is '수정일시';

alter table public.advertisement
    owner to "allink-admin";

create table public.kakao_bill
(
    receipt_uuid     varchar(36) not null,
    envelop_id       varchar(100),
    response_code    varchar(30),
    reg_date         timestamp(6),
    partner_req_uuid varchar(50) not null
        constraint naver_bill_copy1_pkey
            primary key,
    user_id          varchar(36) not null
);

alter table public.kakao_bill
    owner to "allink-admin";

create index "IDX_kakao_bill_userUuidRegdate"
    on public.kakao_bill (user_id, reg_date);

create table public.naver_bill
(
    receipt_uuid     varchar(36) not null,
    naver_doc_id     varchar(100),
    response_code    varchar(30),
    reg_date         timestamp(6),
    partner_req_uuid varchar(50) not null
        primary key,
    user_id          varchar(36) not null
);

alter table public.naver_bill
    owner to "allink-admin";

create index "IDX_naver_bill_userUuidRegdate"
    on public.naver_bill (user_id, reg_date);

create table public.merchant_tag
(
    tag_id            varchar(50) not null
        constraint receipt_merchants_tags_copy1_pkey
            primary key,
    merchant_store_id varchar(50),
    device_id         varchar(50),
    store_uid         varchar(36),
    reg_date          timestamp(6) default CURRENT_TIMESTAMP,
    mod_date          timestamp(6),
    merchant_group_id varchar(30),
    tag_name          varchar(50),
    reg_by            uuid,
    mod_by            uuid
);

alter table public.merchant_tag
    owner to "allink-admin";

create index "IDX_merchant_tag_merchantId_posId"
    on public.merchant_tag (device_id);

create table public.store
(
    store_uid           varchar(36) default gen_random_uuid() not null
        primary key,
    store_name          varchar                               not null,
    store_type          varchar     default 'DEF'::character varying,
    zone_code           varchar,
    addr1               varchar,
    addr2               varchar,
    reg_date            timestamp(6),
    delete_date         timestamp(6),
    icon_url            varchar(255),
    logo_url            varchar(255),
    franchise_code      varchar(30),
    map_url             varchar(255),
    lat                 varchar(20),
    lon                 varchar(20),
    tel                 varchar(15),
    mobile              varchar(15),
    manager_name        varchar(30),
    site_link           varchar(255),
    receipt_width_inch  varchar(2),
    work_type           varchar(100),
    business_no         varchar(30),
    ceo_name            varchar(30),
    business_type       varchar(255),
    event_type          varchar(255),
    email               varchar(255),
    partner_login_id    varchar(50),
    partner_login_pword varchar(255),
    business_no_law     varchar(30),
    mod_date            timestamp(6),
    status              varchar(10)
);

comment on column public.store.business_no is '사업자번호';

alter table public.store
    owner to "allink-admin";

create index idx_store_business_no
    on public.store (business_no)
    where (business_no IS NOT NULL);

create table public.user_event_session
(
    user_uuid            varchar(36) not null,
    event_session_id     varchar(36) not null,
    partner_req_uuid     varchar(36) not null,
    advertisement_id     varchar(36) not null,
    status               varchar(10) not null,
    begin_at             timestamp   not null,
    expire_at            timestamp,
    total_reserved_point integer,
    reg_date             timestamp   not null,
    mod_date             timestamp,
    primary key (user_uuid, event_session_id)
);

comment on table public.user_event_session is '사용자 이벤트 세션';

comment on column public.user_event_session.user_uuid is '사용자 고유아이디';

comment on column public.user_event_session.event_session_id is '이벤트 세션 고유아이디';

comment on column public.user_event_session.partner_req_uuid is '고유아이디';

comment on column public.user_event_session.advertisement_id is '광고명';

comment on column public.user_event_session.begin_at is '시작일시';

comment on column public.user_event_session.expire_at is '종료일시';

comment on column public.user_event_session.total_reserved_point is '예약 포인트포인트';

comment on column public.user_event_session.reg_date is '등록일시';

comment on column public.user_event_session.mod_date is '수정일시';

alter table public.user_event_session
    owner to "allink-admin";

create table public.user_event_point
(
    waiting_seq         bigint      not null
        primary key,
    user_uuid           varchar(36) not null,
    event_session_id    varchar(36) not null,
    transaction_id      varchar(36) not null,
    receipt_uuid        varchar(36) not null,
    points              integer     not null,
    advertisement_title varchar(255),
    reg_date            timestamp   not null
);

comment on table public.user_event_point is '사용자 이벤트 포인트 적립';

comment on column public.user_event_point.waiting_seq is '대기열 시퀀스';

comment on column public.user_event_point.user_uuid is '사용자 고유아이디';

comment on column public.user_event_point.event_session_id is '이벤트 세션 고유아이디';

comment on column public.user_event_point.transaction_id is '트랜잭션 아이디';

comment on column public.user_event_point.receipt_uuid is '영수증 고유아이디';

comment on column public.user_event_point.points is '적립 포인트';

comment on column public.user_event_point.advertisement_title is '광고명';

comment on column public.user_event_point.reg_date is '등록일시';

alter table public.user_event_point
    owner to "allink-admin";

create index "user_event_point_userUuidEventSessionId"
    on public.user_event_point (user_uuid, event_session_id);

create table public.bz_agency
(
    uuid                    uuid      not null
        primary key,
    agency_name             varchar(255),
    business_no             varchar(20),
    addr1                   varchar(255),
    addr2                   varchar(255),
    tel                     varchar(20),
    ceo_name                varchar(50),
    ceo_phone               varchar(20),
    application_file_path   varchar(255),
    bz_file_path            varchar(255),
    id_file_path            varchar(255),
    bank_file_path          varchar(255),
    is_receipt_alliance     boolean,
    infra_ratio             integer,
    reward_base_ratio       integer,
    reward_commission_ratio integer,
    reward_package_ratio    integer,
    advertisement_ratio     integer,
    is_coupon_adv           boolean,
    coupon_adv_ratio        integer,
    tag_deposit             integer,
    agency_deposit          integer,
    settlement_bank         varchar(20),
    bank_account_name       varchar(50),
    bank_account_no         varchar(50),
    reg_date                timestamp not null,
    reg_by                  uuid      not null,
    mod_date                timestamp,
    mod_by                  uuid,
    status                  varchar(10)
);

comment on table public.bz_agency is ' 영업 대리점';

comment on column public.bz_agency.uuid is '고유아이디';

comment on column public.bz_agency.agency_name is '대리점명';

comment on column public.bz_agency.business_no is '사업자번호';

comment on column public.bz_agency.addr1 is '주소 1';

comment on column public.bz_agency.addr2 is '주소 2';

comment on column public.bz_agency.tel is '전화번호';

comment on column public.bz_agency.ceo_name is '대표자명';

comment on column public.bz_agency.ceo_phone is '대표자 전화번호';

comment on column public.bz_agency.application_file_path is '신청서 등록여부';

comment on column public.bz_agency.bz_file_path is '사업자등록증 등록여부';

comment on column public.bz_agency.id_file_path is '대표자 신분증 등록여부';

comment on column public.bz_agency.bank_file_path is '통장사본 등록여부';

comment on column public.bz_agency.is_receipt_alliance is '영수증 제휴 여부';

comment on column public.bz_agency.infra_ratio is '영수증 인프라 배분율';

comment on column public.bz_agency.reward_base_ratio is '리워드 기본료 배분율';

comment on column public.bz_agency.reward_commission_ratio is '리워드 수수료 배분율';

comment on column public.bz_agency.reward_package_ratio is '999+ 배분율';

comment on column public.bz_agency.advertisement_ratio is '영수증 광고 배분율';

comment on column public.bz_agency.is_coupon_adv is '핫플 쿠폰 광고 진행여부';

comment on column public.bz_agency.coupon_adv_ratio is '쿠폰광고 배분율';

comment on column public.bz_agency.tag_deposit is '태그 예치금';

comment on column public.bz_agency.agency_deposit is '대리점 보증금';

comment on column public.bz_agency.settlement_bank is '은행';

comment on column public.bz_agency.bank_account_name is '예금주';

comment on column public.bz_agency.bank_account_no is '계좌번호';

comment on column public.bz_agency.reg_date is '등록일시';

comment on column public.bz_agency.reg_by is '등록인아이디';

comment on column public.bz_agency.mod_date is '수정일시';

comment on column public.bz_agency.mod_by is '수정인 아이디';

comment on column public.bz_agency.status is '상태';

alter table public.bz_agency
    owner to "allink-admin";

create table public.bz_agency_store
(
    bz_agency_uuid uuid        not null,
    store_uid      varchar(36) not null,
    reg_date       timestamp   not null,
    reg_by         uuid        not null,
    primary key (bz_agency_uuid, store_uid)
);

comment on table public.bz_agency_store is '영업 대리점 관리 가맹점';

comment on column public.bz_agency_store.bz_agency_uuid is '영업 대리점 고유아이디';

comment on column public.bz_agency_store.store_uid is '가맹점 고유아이디';

comment on column public.bz_agency_store.reg_date is '등록일시';

comment on column public.bz_agency_store.reg_by is '등록인 아이디';

alter table public.bz_agency_store
    owner to "DbUser";

