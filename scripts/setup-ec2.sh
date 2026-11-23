#!/usr/bin/env bash
set -euo pipefail

# Script idempotente para preparar uma instancia Ubuntu EC2 (t3.small+ / amd64)
# para receber deploys do CarToon via GitHub Actions + Docker Compose + Caddy.

TARGET_USER="${SUDO_USER:-$USER}"
APP_DIR="/opt/cartoon"
SWAP_FILE="/swapfile"

echo "==> Preparando EC2 para o CarToon (usuario: ${TARGET_USER})..."

# 1. Instalar Docker Engine e Docker Compose plugin (se ausentes)
if ! command -v docker >/dev/null 2>&1; then
  echo "==> Instalando Docker Engine e plugin Compose..."
  curl -fsSL https://get.docker.com | sudo sh
else
  echo "==> Docker ja instalado: $(docker --version)"
fi

sudo systemctl enable --now docker
sudo usermod -aG docker "${TARGET_USER}"

# 2. Garantir 2 GB de swap (protecao contra picos de RAM na instancia de 2 GB)
if [ ! -f "${SWAP_FILE}" ]; then
  echo "==> Criando swap de 2 GB em ${SWAP_FILE}..."
  sudo fallocate -l 2G "${SWAP_FILE}" || sudo dd if=/dev/zero of="${SWAP_FILE}" bs=1M count=2048 status=progress
  sudo chmod 600 "${SWAP_FILE}"
  sudo mkswap "${SWAP_FILE}"
  sudo swapon "${SWAP_FILE}"
else
  echo "==> Swap ja existe em ${SWAP_FILE}."
fi

if ! grep -q "^${SWAP_FILE} " /etc/fstab; then
  echo "${SWAP_FILE} none swap sw 0 0" | sudo tee -a /etc/fstab >/dev/null
fi

# 3. Criar diretorio da aplicacao
echo "==> Configurando diretorio ${APP_DIR}..."
sudo mkdir -p "${APP_DIR}"
sudo chown "${TARGET_USER}:${TARGET_USER}" "${APP_DIR}"

echo ""
echo "==> EC2 pronta para receber deploys!"
echo "    Docker Compose: $(docker compose version)"
echo "    Swap ativa:"
swapon --show
echo ""
echo "Proximo passo: configure os Secrets e Variables do environment 'production' no GitHub e dispare o workflow."
