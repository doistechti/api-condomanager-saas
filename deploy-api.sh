#!/bin/bash

set -e

echo "==> Entrando na pasta do projeto..."
cd ~/api-condomanager-saas

echo "==> Atualizando código..."
git pull

echo "==> Rebuildando imagem da API..."
docker compose build api-condomanager-saas

echo "==> Subindo container da API..."
docker compose up -d api-condomanager-saas

echo "==> Aguardando aplicação subir..."
sleep 10

echo "==> Testando health..."
curl -k https://condoapi.doistech.com.br/health || true

echo "==> Mostrando containers ativos..."
docker ps

echo "==> Últimos logs da API..."
docker compose logs --tail=50 api-condomanager-saas

echo "==> Deploy concluído."