# Product Review – Full Stack Application (Mobile + Backend)

## Overview
This repository contains a **full-stack Product Review application** built with:
- **Mobile Frontend:** React Native (Expo, TypeScript)
- **Backend API:** Spring Boot (Java, REST, JPA)
- **AI Feature:** Server-generated review summaries (cached and invalidated on review changes)

The codebase is designed both as:
- a real-world portfolio project
- an intern onboarding learning system
- a production-style architecture exercise (cache, pagination, release track)

---

## Architecture (System View)

```
Mobile (Expo)
  └─ calls REST API (Spring Boot)
        ├─ Business Logic (Services)
        ├─ Persistence (Repositories + DB)
        └─ AI Summary Engine (cached)
```

---

## Repository Structure

```
.
├── mobile/                 # React Native (Expo) app
├── backend/                # Spring Boot REST API
├── README.md               # This file (system overview)
├── README_FRONTEND.md      # Detailed mobile architecture
└── README_BACKEND.md       # Detailed backend architecture
```

---

## End-to-End Data Flow (Critical)

### 1) Product list (paginated, filter/sort/search)
1. Mobile requests paginated list with filters/sort
2. Backend returns `{items, page, pageSize, total, totalPages, hasNext}`
3. Mobile caches results (server-state) and updates UI
4. Filter/sort/search changes change the query key → new cache entry

### 2) Product details + reviews + AI summary
1. Mobile opens product detail screen
2. Mobile requests:
   - product details
   - reviews (paginated)
   - AI summary (cached on backend)
3. Backend returns stable DTOs
4. After a review is created:
   - backend invalidates AI summary cache
   - mobile invalidates related queries (details, reviews, list, summary)

---

## Running Locally

### Backend
```bash
cd backend
./mvnw spring-boot:run
```

### Mobile
```bash
cd mobile
npm install
npx expo start
```

---

## Environments & Release Track

### Why environments matter
- dev/staging/prod should not share credentials or base URLs
- issues need observability to debug quickly

### Recommended environment variables (Expo)
- `EXPO_PUBLIC_API_BASE_URL` (dev/preview/prod)

### Observability (recommended)
- Add Sentry (or similar) for:
  - runtime errors
  - network failures
  - navigation breadcrumbs

---

## Web Deployment (Expo Web) – Recommended Checklist
1. Configure production base URL
2. Build web output
3. Deploy to a static host (e.g., Vercel)
4. Smoke test:
   - product list paging
   - product details + reviews
   - AI summary fetch
   - wishlist + multi-select
   - notifications

---

## Intern Learning Path (Start Here)
1. Read `README_BACKEND.md` → understand API surface and caching
2. Read `README_FRONTEND.md` → understand UI state vs server state separation
3. Trace one request end-to-end:
   - Product List → Product Controller → Product Service → Repository → DB
4. Implement a small improvement using established patterns
   - keep controllers thin
   - keep server state cached & invalidated
   - keep UI state in contexts

---

## Documentation
- Mobile: `README_FRONTEND.md`
- Backend: `README_BACKEND.md`
