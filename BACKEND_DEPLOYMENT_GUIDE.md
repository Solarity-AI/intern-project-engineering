# 🚀 Backend Deployment Guide - Render.com

## Genel Bakış
Bu kılavuz, Product Review Backend'ini **ücretsiz** Render.com platformuna deploy etmek için gereken adımları açıklar.

**Not:** Heroku artık ücretsiz plan sunmuyor, bu yüzden Render.com'a geçiyoruz.

---

## ✅ Neden Render.com?

- ✅ **Tamamen Ücretsiz** (750 saat/ay)
- ✅ Otomatik HTTPS
- ✅ GitHub entegrasyonu
- ✅ Kolay yapılandırma
- ✅ Health check desteği
- ✅ Otomatik deploy

**Kısıtlamalar:**
- İnaktif olunca uyuyor (ilk istek 30-60 saniye sürebilir)
- 512 MB RAM
- Aylık 100 GB bandwidth

---

## 🎯 Deployment Adımları

### Yöntem 1: Render Dashboard (Önerilen)

#### Adım 1: GitHub'a Push
```bash
cd "C:\Stajlar\Solarity AI Staj\Repolar\ProductReviewApp"
git add .
git commit -m "feat: add Render.com deployment configuration"
git push origin main
```

#### Adım 2: Render.com'a Kaydol
1. [render.com](https://render.com) adresine git
2. "Get Started for Free" tıkla
3. GitHub ile giriş yap

#### Adım 3: Yeni Web Service Oluştur
1. Dashboard'da **"New +"** butonuna tıkla
2. **"Web Service"** seç
3. GitHub repository'ni bağla ve `ProductReviewApp` seç
4. **"Connect"** tıkla

#### Adım 4: Service Yapılandırması

**Basic Settings:**
- **Name:** `product-review-backend`
- **Region:** Europe (Frankfurt) veya en yakın
- **Branch:** `main`
- **Root Directory:** `backend`
- **Runtime:** Java
- **Build Command:** `./mvnw clean package -DskipTests`
- **Start Command:** `java -Dserver.port=$PORT -jar target/*.jar`

**Advanced Settings:**
- **Instance Type:** Free
- **Auto-Deploy:** Yes (her push'ta otomatik deploy)

**Environment Variables:**
```
PORT = 10000 (otomatik eklenir)
JAVA_VERSION = 17
OPENAI_API_KEY = <your-openai-api-key>
```

#### Adım 5: Deploy
1. **"Create Web Service"** butonuna tıkla
2. Build sürecini izle (~3-5 dakika)
3. Deploy tamamlanınca URL'i kopyala:
   - Örnek: `https://product-review-backend.onrender.com`

#### Adım 6: Health Check Test
```bash
curl https://product-review-backend.onrender.com/actuator/health
```

Beklenen yanıt:
```json
{"status":"UP"}
```

---

### Yöntem 2: render.yaml ile Deploy (Gelişmiş)

Proje kök dizininde `render.yaml` dosyası mevcut. Render dashboard'da "New +" → "Blueprint" seçerek otomatik yapılandırma yapılabilir.

---

## 🔄 Frontend API URL Güncelleme

Backend deploy edildikten sonra, frontend'deki API URL'ini güncelle:

### Adım 1: API Service Dosyasını Güncelle
```typescript
// mobile/src/services/api.ts

// Eski Heroku URL'i kaldır:
// const BASE_URL = "https://product-review-app-solarityai-a391ad53d79a.herokuapp.com";

// Yeni Render.com URL'i ekle:
const BASE_URL = "https://product-review-backend.onrender.com";
```

### Adım 2: Değişiklikleri Commit Et
```bash
cd "C:\Stajlar\Solarity AI Staj\Repolar\ProductReviewApp"
git add mobile/src/services/api.ts
git commit -m "chore: update backend API URL to Render.com"
git push origin main
```

---

## 🧪 Deployment Sonrası Test

### 1. Health Check
```bash
curl https://product-review-backend.onrender.com/actuator/health
```

### 2. Products Endpoint
```bash
curl https://product-review-backend.onrender.com/api/products
```

### 3. Stats Endpoint
```bash
curl https://product-review-backend.onrender.com/api/products/stats
```

### 4. Frontend Bağlantısı
1. Frontend'i yerel olarak çalıştır: `npm start`
2. Ürünlerin yüklendiğini doğrula
3. Tüm özelliklerin çalıştığını kontrol et

---

## 📊 Beklenen Sonuçlar

### Build Çıktısı
```
[INFO] Building jar: /opt/render/project/src/backend/target/product-review-0.0.1-SNAPSHOT.jar
[INFO] BUILD SUCCESS
==> Starting service...
2026-01-28 10:00:00.123  INFO 1 --- [main] c.e.p.ProductReviewApplication : Started ProductReviewApplication in 3.456 seconds
```

### Live URL
```
✅ Backend Live: https://product-review-backend.onrender.com
✅ API Docs: https://product-review-backend.onrender.com/api/products
✅ Health: https://product-review-backend.onrender.com/actuator/health
```

---

## 🐛 Sorun Giderme

### Build Başarısız

**Problem:** Maven build hatası
**Çözüm:**
1. `backend/pom.xml` dosyasını kontrol et
2. Java 17 kullanıldığından emin ol
3. Render log'larını incele

### Service Çalışmıyor

**Problem:** Service başlamıyor
**Çözüm:**
1. Start command'i doğrula: `java -Dserver.port=$PORT -jar target/*.jar`
2. PORT environment variable'ın set olduğunu kontrol et
3. Logs'tan detaylı hata mesajını oku

### İlk İstek Çok Yavaş

**Problem:** Uygulama 30-60 saniye sonra yanıt veriyor
**Çözüm:** Bu normaldir! Render.com free tier'da inaktif servisler uyur. İlk istek servisi uyandırır. Sonraki istekler hızlı olacaktır.

**Çözüm (opsiyonel):** 
- Cron job ile her 10 dakikada bir health check yap
- Ücretli plan'a geç (7$/ay)

### CORS Hatası

**Problem:** Frontend'den API'ye erişim hatası
**Çözüm:**
Backend'de `@CrossOrigin(origins = "*")` zaten mevcut. Eğer sorun devam ederse:
1. Browser console'da hatayı kontrol et
2. API URL'in doğru olduğunu doğrula

---

## 🎯 Environment Variables

Render Dashboard'da şu environment variable'ları ekle:

| Variable | Value | Required |
|----------|-------|----------|
| `PORT` | `10000` | ✅ Otomatik |
| `JAVA_VERSION` | `17` | ✅ Gerekli |
| `OPENAI_API_KEY` | `sk-...` | ⚠️ AI özellikleri için |

**Not:** OpenAI API anahtarı olmadan da uygulama çalışır, sadece AI özellikleri devre dışı olur.

---

## 📈 Performans İpuçları

### 1. Database Persistence
Şu anda H2 in-memory database kullanılıyor. Her deploy'da veriler sıfırlanır.

**Gelecek İyileştirme:** PostgreSQL'e geç (Render.com ücretsiz PostgreSQL sunuyor)

### 2. Cold Start Azaltma
Free tier'da servis 15 dakika inaktif kalınca uyur.

**Çözümler:**
- Uptime monitoring (UptimeRobot, cron-job.org)
- Ücretli plana geç

### 3. Caching
Backend'de Caffeine cache zaten aktif (AI summary'ler için).

---

## 🔐 Güvenlik

### HTTPS
Render.com otomatik HTTPS sağlar. Ek yapılandırma gerekmez.

### Secrets
Hassas bilgileri (API keys) environment variables'da sakla, **asla** koda commit etme.

### CORS
Production'da `@CrossOrigin(origins = "https://your-frontend-domain.vercel.app")` ile sınırla.

---

## 📝 Deployment Sonrası Checklist

- [ ] Backend başarıyla deploy edildi
- [ ] Health check endpoint çalışıyor
- [ ] API endpoints erişilebilir
- [ ] Frontend API URL'i güncellendi
- [ ] CORS yapılandırması doğru
- [ ] Environment variables set edildi
- [ ] Tüm özellikler test edildi
- [ ] README.md güncellendi

---

## 🎉 Başarı Kriterleri

✅ Backend Render.com'da live  
✅ API endpoints erişilebilir  
✅ Frontend backend'e bağlanıyor  
✅ Tüm CRUD işlemleri çalışıyor  
✅ AI özellikleri çalışıyor (API key varsa)  

---

## 📞 Destek

**Deployment sorunları için:**
1. Render Dashboard → Logs'u incele
2. [Render Docs](https://render.com/docs) oku
3. Community forum'a sor

**Proje sorunları için:**
- İletişim: @MehmetBegun
- Repository: ProductReviewApp

---

## 🔄 Alternatif Platformlar

Eğer Render.com işe yaramazsa:

### Railway.app
- Ücretsiz: 500 saat/ay
- Kurulum: Render'a benzer
-장점: Daha hızlı cold start

### Fly.io
- Ücretsiz: 3 shared-cpu VM
- Kurulum: CLI gerekir
- 장점: Edge deployment

### Koyeb
- Ücretsiz: 1 web service
- Kurulum: Git entegrasyonu
- 장점: Global CDN

---

**Deployment Tarihi:** 2026-01-28  
**Platform:** Render.com (Free Tier)  
**Backend URL:** [Deploy sonrası eklenecek]  
**Status:** ✅ Hazır  
