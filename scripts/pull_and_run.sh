#!/usr/bin/env bash
#
# @file pull_and_run.sh
# @brief GHCR 로그인 후 docker compose로 서비스 갱신. 토큰/계정은 인스턴스 환경/파일에서 주입.
# @author Devonshin
# @date 2025-09-04
#
set -euo pipefail

# 실행 사용자/시간 로깅 (디버깅용)
{ 
  printf '%s ' "$(date -Is)";
  echo "user=$(id -u -n) uid=$(id -u) gid=$(id -g)"
} | tee -a /home/ec2-user/.pull_and_run.log >/dev/null 2>&1 || true

: "${GHCR_USERNAME:=beizix}"
# /home/ec2-user/.ghcr_token 파일을 배치해 주세요.
: "${GHCR_PAT:=}"
GHCR_PAT_FILE="${GHCR_PAT_FILE:-/home/ec2-user/.ghcr_token}"

if [ -z "${GHCR_USERNAME}" ]; then
  echo "[ERROR] GHCR_USERNAME 값이 필요합니다. 환경변수 GHCR_USERNAME을 설정하세요." >&2
  exit 1
fi

if [ -n "${GHCR_PAT}" ]; then
  TOKEN="$GHCR_PAT"
elif [ -f "$GHCR_PAT_FILE" ]; then
  TOKEN="$(cat "$GHCR_PAT_FILE")"
else
  echo "[ERROR] GHCR PAT을 찾을 수 없습니다. GHCR_PAT 환경변수 또는 $GHCR_PAT_FILE 파일을 제공하세요." >&2
  exit 1
fi

# GHCR 로그인
printf '%s' "$TOKEN" | docker login ghcr.io -u "$GHCR_USERNAME" --password-stdin

# 사용할 compose 선택 (없으면 기본 docker-compose.yml)
if [ -f docker-compose.production.yml ]; then
  COMPOSE_FILE="docker-compose.production.yml"
elif [ -f docker-compose.staging.yml ]; then
  COMPOSE_FILE="docker-compose.staging.yml"
elif [ -f docker-compose.yml ]; then
  COMPOSE_FILE="docker-compose.yml"
else
  echo "[ERROR] docker-compose*.yml 파일을 찾을 수 없습니다." >&2
  exit 1
fi

echo "[INFO] Using compose file: $COMPOSE_FILE"

# 선택: 태그 오버라이드(롤백/고정 배포)
# IMAGE_TAG_OVERRIDE 환경변수가 설정되어 있으면, override 파일을 생성해 해당 태그로 이미지 고정
COMPOSE_ARGS=("-f" "$COMPOSE_FILE")
OVERRIDE_FILE="docker-compose.override.ephemeral.yml"
if [ -n "${IMAGE_TAG_OVERRIDE:-}" ]; then
  # COMPOSE 파일에서 base image 추출(첫 image 라인 기준)
  BASE_IMAGE=$(awk '/^[[:space:]]*image:[[:space:]]/ {gsub(/"|\x27/, ""); print $2; exit}' "$COMPOSE_FILE" | sed 's/:.*$//')
  if [ -z "$BASE_IMAGE" ]; then
    echo "[WARN] BASE_IMAGE를 COMPOSE 파일에서 찾지 못했습니다. 오버라이드를 건너뜁니다."
  else
    cat > "$OVERRIDE_FILE" <<EOF
services:
  e-receipt-admin-api:
    image: ${BASE_IMAGE}:${IMAGE_TAG_OVERRIDE}
EOF
    COMPOSE_ARGS+=("-f" "$OVERRIDE_FILE")
    echo "[INFO] Tag override enabled: ${BASE_IMAGE}:${IMAGE_TAG_OVERRIDE}"
  fi
fi

# Compose 실행 바이너리 탐지
COMPOSE_BIN=()
if docker compose version >/dev/null 2>&1; then
  COMPOSE_BIN=(docker compose)
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE_BIN=(docker-compose)
else
  echo "[ERROR] docker compose(plugin) 또는 docker-compose(V1)가 설치되어 있지 않습니다." >&2
  exit 1
fi

# 버전 정보 출력(디버그)
(docker --version || true)
(${COMPOSE_BIN[@]} version || true)

# Compose 실행
set +e
"${COMPOSE_BIN[@]}" "${COMPOSE_ARGS[@]}" pull e-receipt-admin-api
PULL_RC=$?
"${COMPOSE_BIN[@]}" "${COMPOSE_ARGS[@]}" up -d
UP_RC=$?
set -e

if [ $PULL_RC -ne 0 ] || [ $UP_RC -ne 0 ]; then
  echo "[ERROR] compose pull/up 실패(PULL_RC=$PULL_RC, UP_RC=$UP_RC)" >&2
  exit 1
fi

# 임시 override 파일 정리
[ -f "$OVERRIDE_FILE" ] && rm -f "$OVERRIDE_FILE" || true

# 사용하지 않는 이미지 정리 (실패 무시)
(docker image prune --all --force) || true

# 새로운 아이피를 대상 그룹에 등록

TOKEN=$(curl -sX PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 60")
PRIVATE_IP=$(curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/local-ipv4)
TARGET_GROUP_ARN="$(cat /home/ec2-user/.target_group_arn)"
APP_PORT=8080
REGION=ap-northeast-2

sleep 30

{
  echo "Registering target: TARGET_GROUP_ARN=${TARGET_GROUP_ARN}, PRIVATE_IP=${PRIVATE_IP}, APP_PORT=${APP_PORT}, REGION=${REGION}"
  aws elbv2 register-targets \
    --target-group-arn "$TARGET_GROUP_ARN" \
    --targets Id="$PRIVATE_IP",Port=$APP_PORT,AvailabilityZone=all \
    --region $REGION
} >> /home/ec2-user/.pull_and_run.log 2>&1

cat > /home/ec2-user/deregister-on-shutdown.sh << 'EOF'
  aws elbv2 deregister-targets \
    --target-group-arn TARGET_GROUP_ARN_PLACEHOLDER \
    --targets Id=PRIVATE_IP_PLACEHOLDER,Port=APP_PORT_PLACEHOLDER,AvailabilityZone=all \
    --region REGION_PLACEHOLDER
EOF

# 변수 치환
sed -i "s|TARGET_GROUP_ARN_PLACEHOLDER|$TARGET_GROUP_ARN|g" /opt/deregister-on-shutdown.sh
sed -i "s|APP_PORT_PLACEHOLDER|$APP_PORT|g" /opt/deregister-on-shutdown.sh
sed -i "s|REGION_PLACEHOLDER|$REGION|g" /opt/deregister-on-shutdown.sh
sed -i "s|PRIVATE_IP_PLACEHOLDER|$PRIVATE_IP|g" /opt/deregister-on-shutdown.sh

chmod +x /home/ec2-user/deregister-on-shutdown.sh

# 종료 시 자동 실행 등록
sudo cp /home/ec2-user/deregister-on-shutdown.sh /opt/deregister-on-shutdown.sh
sudo chmod +x /opt/deregister-on-shutdown.sh

cat <<EOF | sudo tee /etc/systemd/system/deregister-on-shutdown.service
[Unit]
Description=Deregister from ALB on shutdown
DefaultDependencies=no
Before=shutdown.target

[Service]
Type=oneshot
ExecStart=/opt/deregister-on-shutdown.sh
RemainAfterExit=yes

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable deregister-on-shutdown.service