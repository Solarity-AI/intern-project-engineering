# Production Deployment Guide

## Overview
This guide explains how to deploy the Product Review App to Vercel.

## Prerequisites
- Node.js 20+
- npm or yarn
- Vercel account (free tier)

## Color Palette Update
The application now uses a professional, modern color palette that maintains WCAG AA contrast standards:

### Light Mode
- **Background**: `#FAFAFA` - Soft neutral for reduced eye strain
- **Foreground**: `#1A1A1A` - Near black for optimal readability
- **Primary**: `#0066FF` - Vibrant blue for trustworthy, modern feel
- **Secondary**: `#F5F5F5` - Subtle contrast
- **Accent**: `#EEF2FF` with `#4F46E5` foreground - Professional indigo accent

### Dark Mode
- **Background**: `#0A0A0A` - Near black for OLED optimization
- **Foreground**: `#FAFAFA` - Soft white for reduced glare
- **Primary**: `#3B82F6` - Softer blue for dark mode
- **Secondary**: `#262626` - Distinct from background
- **Accent**: `#1E1B4B` with `#C7D2FE` foreground - Deep indigo with high contrast

## Deployment Steps

### Option 1: Using Vercel CLI (Recommended)

1. Install Vercel CLI globally:
```bash
npm install -g vercel
```

2. Navigate to the mobile directory:
```bash
cd mobile
```

3. Login to Vercel:
```bash
vercel login
```

4. Deploy to production:
```bash
vercel --prod
```

### Option 2: Using Vercel Dashboard

1. Push your code to GitHub repository
2. Go to [vercel.com](https://vercel.com)
3. Click "Import Project"
4. Select your repository
5. Configure:
   - **Framework Preset**: Other
   - **Root Directory**: `mobile`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
6. Click "Deploy"

## Build Locally (Testing)

To test the production build locally:

```bash
npm run build
```

This will create a `dist` directory with static files.

## Environment Variables

No environment variables are required for basic deployment. The app uses a mock API service for development.

## Post-Deployment Verification

1. **Public Accessibility**: Visit the Vercel URL
2. **Visual Consistency**: Check that new colors are applied globally
3. **Responsive Design**: Test on mobile and desktop viewports
4. **Dark Mode**: Toggle theme and verify color transitions
5. **Navigation**: Test all routes (product list, details, wishlist, etc.)

## Troubleshooting

### Build Fails
- Check Node.js version (should be 20+)
- Clear cache: `rm -rf .expo node_modules && npm install`
- Verify all dependencies are installed

### Colors Not Applied
- Clear browser cache
- Check theme.ts file for correct color values
- Verify ThemeContext is properly wrapping the app

### Routing Issues on Vercel
- Ensure vercel.json has proper rewrites configuration
- All routes should redirect to index.html for SPA behavior

## CI/CD Integration

The project is configured for automatic deployments:
- Push to `main` branch triggers production deployment
- Pull requests create preview deployments

## Performance Optimization

The deployment includes:
- Static file caching (1 year)
- Optimized build output
- Lazy loading for routes
- Image optimization via Expo

## Support

For issues or questions:
- Check Vercel deployment logs
- Review Expo build output
- Contact: @MehmetBegun
