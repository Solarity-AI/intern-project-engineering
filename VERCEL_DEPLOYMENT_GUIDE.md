# 🚀 Full-Stack Deployment Guide

## Genel Bakış
Bu kılavuz, Product Review App'in hem backend'ini hem de frontend'ini ücretsiz cloud platformlara deploy etmek için gereken adımları açıklar.

**Deadline:** 2026-01-28 24:00  
**Owner:** @MehmetBegun  
**Status:** Deployment için hazır  

---

## 📦 Deployment Yapısı

### Backend → Render.com (Ücretsiz)
- **Platform:** Render.com
- **Runtime:** Java 17
- **Database:** H2 (in-memory)
- **URL:** `https://product-review-backend.onrender.com`

### Frontend → Vercel (Ücretsiz)
- **Platform:** Vercel
- **Runtime:** Node.js 20
- **Framework:** Expo Web (React Native)
- **URL:** `https://product-review-app.vercel.app`

---

## 🎯 Deployment Sırası

**ÖNEMLİ:** Backend'i önce deploy et, sonra frontend!

1. ✅ Backend'i Render.com'a deploy et
2. ✅ Backend URL'ini frontend koduna ekle
3. ✅ Frontend'i Vercel'e deploy et
4. ✅ Test et

---

## 🔧 ADIM 1: Backend Deployment (Render.com)

### Detaylı Kılavuz
👉 **[BACKEND_DEPLOYMENT_GUIDE.md](./BACKEND_DEPLOYMENT_GUIDE.md)** dosyasına bakın

### Hızlı Adımlar

1. **Render.com'a kaydol:** [render.com](https://render.com)
2. **New Web Service** oluştur
3. **Yapılandır:**
   ```
   Name: product-review-backend
   Root Directory: backend
   Build Command: ./mvnw clean package -DskipTests
   Start Command: java -Dserver.port=$PORT -jar target/*.jar
   ```
4. **Environment Variables:**
   ```
   JAVA_VERSION = 17
   OPENAI_API_KEY = <your-key> (opsiyonel)
   ```
5. **Deploy** et ve URL'i kopyala

### Backend Test
```bash
curl https://YOUR-BACKEND-URL.onrender.com/actuator/health
```

---

## 🔧 ADIM 2: Frontend API URL Güncelleme

Backend deploy edildikten sonra, frontend'deki API URL'ini güncelle:

```typescript
// mobile/src/services/api.ts
const BASE_URL = "https://YOUR-BACKEND-URL.onrender.com";
```

Commit et:
```bash
git add mobile/src/services/api.ts
git commit -m "chore: update backend URL to Render.com"
git push origin main
```

---

## 🔧 ADIM 3: Frontend Deployment (Vercel)

---

## ✅ Ön-Deployment Kontrol Listesi

**Backend:**
- [x] Render.com yapılandırması hazır
- [x] Spring Boot Actuator eklendi
- [x] Health check endpoint aktif
- [ ] **Backend'i Render.com'a deploy et** ⬅️ ÖNCELİK 1

**Frontend:**
- [x] Build scriptleri eklendi
- [x] Production build test edildi
- [x] Renk paleti güncellendi
- [x] Vercel yapılandırması oluşturuldu
- [x] GitHub Actions CI/CD hazır
- [ ] **Backend URL'ini güncelle** ⬅️ ÖNCELİK 2
- [ ] **Frontend'i Vercel'e deploy et** ⬅️ ÖNCELİK 3

---

## 🎯 Deployment Options

Choose **ONE** of the following methods:

### Option A: Vercel Dashboard (Recommended - Easiest)

#### Step 1: Push to GitHub
```bash
cd "C:\Stajlar\Solarity AI Staj\Repolar\ProductReviewApp"
git add .
git commit -m "feat: add production deployment configuration and update color palette"
git push origin main
```

#### Step 2: Connect to Vercel
1. Go to [vercel.com](https://vercel.com)
2. Sign in with GitHub account
3. Click **"Add New"** → **"Project"**
4. Select your repository: `ProductReviewApp`
5. Click **"Import"**

#### Step 3: Configure Project
- **Framework Preset:** Other (or detect automatically)
- **Root Directory:** `mobile`
- **Build Command:** `npm run build`
- **Output Directory:** `dist`
- **Install Command:** `npm install`
- **Node.js Version:** 20.x

#### Step 4: Deploy
1. Click **"Deploy"**
2. Wait 2-3 minutes for build to complete
3. Once deployed, you'll get a production URL like:
   - `https://product-review-app-[random].vercel.app`

#### Step 5: Set Up Custom Settings (Optional)
1. Go to Project Settings → Domains
2. Add a custom domain if desired (optional)
3. Configure environment variables if needed (none required for this project)

---

### Option B: Vercel CLI (Advanced)

#### Step 1: Install Vercel CLI
```bash
npm install -g vercel
```

#### Step 2: Login to Vercel
```bash
vercel login
```
- Follow the prompts to authenticate via email or GitHub

#### Step 3: Navigate to Mobile Directory
```bash
cd "C:\Stajlar\Solarity AI Staj\Repolar\ProductReviewApp\mobile"
```

#### Step 4: Deploy to Production
```bash
vercel --prod
```

#### Step 5: Answer Configuration Questions
- **Set up and deploy?** Y
- **Which scope?** [Select your account]
- **Link to existing project?** N (first time) or Y (if project exists)
- **What's your project's name?** product-review-app
- **In which directory is your code located?** ./
- **Want to override the settings?** N

#### Step 6: Get Production URL
After deployment completes, you'll see:
```
✅ Production: https://product-review-app-[random].vercel.app [copied to clipboard]
```

---

## 🔍 Post-Deployment Verification

### 1. Test Public Accessibility
- [ ] Open the Vercel URL in a browser
- [ ] Verify the app loads without errors
- [ ] Check browser console for any issues

### 2. Test Visual Consistency
- [ ] Verify new color palette is applied:
  - Light mode primary: Blue (#0066FF)
  - Dark mode primary: Blue (#3B82F6)
- [ ] Toggle between light and dark modes
- [ ] Check that all UI elements use the new colors

### 3. Test Responsive Design
- [ ] Open DevTools (F12)
- [ ] Test mobile viewport (375px width)
- [ ] Test tablet viewport (768px width)
- [ ] Test desktop viewport (1920px width)
- [ ] Verify layout adapts correctly

### 4. Test Core Features
- [ ] Product list loads and displays
- [ ] Product details page works
- [ ] Search functionality works
- [ ] Filter by category works
- [ ] Wishlist add/remove works
- [ ] Review submission works
- [ ] AI Assistant opens and responds

### 5. Test Routing
- [ ] Navigate to different pages
- [ ] Refresh the page (should not show 404)
- [ ] Browser back/forward buttons work
- [ ] Deep links work (share a product URL)

---

## 📊 Expected Results

### Build Output
```
✓ Exported: dist
✓ Total Size: ~1.5 MB
✓ Assets: 30+ font files, images
✓ Bundle: index-[hash].js
```

### Performance Metrics (Target)
- First Contentful Paint: < 2s
- Time to Interactive: < 3s
- Total Blocking Time: < 300ms

### Vercel Deployment Success
```
✅ Deployment Ready
✅ Build Completed
✅ Domain Active
✅ SSL Certificate Active
```

---

## 🐛 Troubleshooting

### Build Fails on Vercel

**Problem:** `expo export` fails
**Solution:** 
1. Check Node.js version is 20.x
2. Clear Vercel build cache in Project Settings
3. Verify package.json scripts are correct

### 404 on Page Refresh

**Problem:** Direct URLs return 404
**Solution:** 
- Verify `vercel.json` has correct rewrites
- File should be in `mobile/` directory
- Redeploy after adding the file

### Colors Not Applied

**Problem:** Old colors still showing
**Solution:**
1. Hard refresh (Ctrl+Shift+R or Cmd+Shift+R)
2. Clear browser cache
3. Check theme.ts file was deployed correctly

### Slow Loading

**Problem:** App takes too long to load
**Solution:**
- Check Vercel Analytics for bottlenecks
- Verify assets are being cached
- Check bundle size in build output

---

## 📝 Documentation Updates

After successful deployment, update:

1. **README.md** - Add the live production URL
2. **Project Dashboard** - Document the deployment
3. **DEPLOYMENT.md** - Note any issues or learnings

Example:
```markdown
🌐 **Live Production URL:** https://product-review-app.vercel.app
```

---

## 🎉 Success Criteria

✅ **Definition of Done:**
- [ ] Public URL is accessible and functional
- [ ] New color palette is visible across all pages
- [ ] App works on mobile and desktop viewports
- [ ] Dark mode toggle works correctly
- [ ] All core features are operational
- [ ] Production URL is documented
- [ ] Changes are committed to main branch

---

## 🔐 Security Notes

- No sensitive environment variables are required
- API uses mock data (no real database)
- All secrets should be in Vercel Environment Variables (if needed in future)
- Never commit `.env` files to Git

---

## 📞 Support

If you encounter issues:
1. Check Vercel deployment logs
2. Review build output for errors
3. Test locally with `npm run build` first
4. Contact: @MehmetBegun

---

## 🎯 Next Steps After Deployment

1. Share the production URL with stakeholders
2. Test on multiple devices and browsers
3. Gather feedback on the new color palette
4. Monitor Vercel Analytics for performance
5. Set up automatic deployments via GitHub Actions

---

**Deployment Date:** 2026-01-28
**Deployed By:** @MehmetBegun
**Production URL:** [To be added after deployment]
**Status:** ✅ Ready to Deploy
