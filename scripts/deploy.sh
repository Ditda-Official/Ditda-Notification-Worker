#!/bin/bash
set -euo pipefail

# === 필수 환경변수 ===
AWS_REGION="${AWS_REGION:-ap-northeast-2}"
ECR_REGISTRY="${ECR_REGISTRY:?ERROR: ECR_REGISTRY must be set}"
IMAGE_TAG="${IMAGE_TAG:-latest}"

APP_DIR="/home/ubuntu/notification-worker"
mkdir -p "$APP_DIR"
cd "$APP_DIR"

echo "=== Ditda Notification Worker 배포 ==="
echo "Image: ${ECR_REGISTRY}:${IMAGE_TAG}"


# === ECR 로그인 ===
echo "[1/5] ECR 로그인"
REGISTRY_HOST="${ECR_REGISTRY%%/*}"
aws ecr get-login-password --region "${AWS_REGION}" \
  | docker login --username AWS --password-stdin "${REGISTRY_HOST}"

# === 새 이미지 pull ===
echo "[2/5] 이미지 pull"
docker pull "${ECR_REGISTRY}:${IMAGE_TAG}"

# === Parameter Store에서 환경변수 fetch ===
echo "[3/5] 환경변수 로드"
> .env
chmod 600 .env

cat >> .env << EOF
SPRING_PROFILES_ACTIVE=prod
EOF

aws ssm get-parameters-by-path \
  --path "/ditda/prod/worker/" \
  --with-decryption \
  --region "${AWS_REGION}" \
  --query "Parameters[*].[Name,Value]" \
  --output text \
  | while IFS=$'\t' read -r name value; do
      key="${name##*/}"
      echo "${key}=${value}" >> .env
    done

PARAM_COUNT=$(grep -c '=' .env || true)
echo "    로드된 환경변수: ${PARAM_COUNT}개"

# === 필수 환경변수 검증 ===
REQUIRED_VARS=(

)

MISSING=()
for var in "${REQUIRED_VARS[@]}"; do
  if ! grep -qE "^${var}=.+" .env; then
    MISSING+=("$var")
  fi
done

if [ ${#MISSING[@]} -gt 0 ]; then
  echo "❌ 필수 환경변수 누락 또는 값 비어있음:"
  for var in "${MISSING[@]}"; do
    echo "    - ${var}"
  done
  exit 1
fi

echo "    필수 환경변수 ${#REQUIRED_VARS[@]}개 모두 존재 확인"

# === 컨테이너 교체 ===
echo "[4/5] 컨테이너 교체"
docker rm -f ditda-notification-worker 2>/dev/null || true
docker run -d --name ditda-notification-worker \
  --network ditda-net \
  --restart unless-stopped \
  --env-file .env \
  -p 127.0.0.1:8083:8080 \
  "${ECR_REGISTRY}:${IMAGE_TAG}"


# === Health Check ===
echo "[5/5]  Health Check"
HEALTH_OK=false
for i in $(seq 1 60); do
  if curl -fs "http://localhost:8083/actuator/health" > /dev/null 2>&1; then
    echo "    [$i/60]  Health Check 성공"
    docker image prune -f > /dev/null
    echo "✅ 배포 완료"
    exit 0
  fi
  echo "    [$i/60] 대기중..."
  sleep 3
done

echo "❌ Health Check 실패. 컨테이너 로그:"
docker logs ditda-notification-worker --tail 50
exit 1
