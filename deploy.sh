#!/bin/bash

# Smart Campus Backend - Deployment Script
# Ubuntu VM Deployment için hazırlanmıştır

set -e

echo "🚀 Smart Campus Backend Deployment Başlatılıyor..."

# Renkler
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Kontroller
echo -e "${YELLOW}📋 Ön Kontroller Yapılıyor...${NC}"

# Docker kontrolü
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker yüklü değil! Lütfen Docker'ı yükleyin.${NC}"
    exit 1
fi

# Docker Compose kontrolü
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose yüklü değil! Lütfen Docker Compose'u yükleyin.${NC}"
    exit 1
fi

# .env dosyası kontrolü
if [ ! -f .env ]; then
    echo -e "${YELLOW}⚠️  .env dosyası bulunamadı!${NC}"
    echo -e "${YELLOW}📝 .env.example dosyasından .env oluşturuluyor...${NC}"
    if [ -f .env.example ]; then
        cp .env.example .env
        echo -e "${YELLOW}⚠️  Lütfen .env dosyasını düzenleyin ve gerekli değerleri girin!${NC}"
        exit 1
    else
        echo -e "${RED}❌ .env.example dosyası bulunamadı!${NC}"
        exit 1
    fi
fi

echo -e "${GREEN}✅ Ön kontroller tamamlandı!${NC}"

# Eski container'ları durdur
echo -e "${YELLOW}🛑 Eski container'lar durduruluyor...${NC}"
docker-compose down || true

# Eski image'ları temizle (opsiyonel)
read -p "Eski image'ları temizlemek istiyor musunuz? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo -e "${YELLOW}🧹 Eski image'lar temizleniyor...${NC}"
    docker-compose down --rmi all || true
fi

# Yeni image'ları build et
echo -e "${YELLOW}🔨 Docker image'ları build ediliyor...${NC}"
docker-compose build --no-cache

# Container'ları başlat
echo -e "${YELLOW}🚀 Container'lar başlatılıyor...${NC}"
docker-compose up -d

# Health check bekleme
echo -e "${YELLOW}⏳ Servislerin hazır olması bekleniyor...${NC}"
sleep 10

# Health check
echo -e "${YELLOW}🏥 Health check yapılıyor...${NC}"

# API Gateway health check
if curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ API Gateway çalışıyor!${NC}"
else
    echo -e "${RED}❌ API Gateway health check başarısız!${NC}"
fi

# Auth Service health check
if curl -f http://localhost:8081/actuator/health > /dev/null 2>&1; then
    echo -e "${GREEN}✅ Auth Service çalışıyor!${NC}"
else
    echo -e "${RED}❌ Auth Service health check başarısız!${NC}"
fi

# Logları göster
echo -e "${GREEN}📋 Son 20 log satırı:${NC}"
docker-compose logs --tail=20

echo ""
echo -e "${GREEN}✅ Deployment tamamlandı!${NC}"
echo ""
echo -e "${GREEN}🌐 API Gateway: http://138.68.99.35:8080${NC}"
echo -e "${GREEN}🌐 Auth Service: http://138.68.99.35:8081${NC}"
echo -e "${GREEN}📚 Swagger UI: http://138.68.99.35:8081/swagger-ui.html${NC}"
echo ""
echo -e "${YELLOW}📝 Logları görmek için: docker-compose logs -f${NC}"
echo -e "${YELLOW}🛑 Durdurmak için: docker-compose down${NC}"

