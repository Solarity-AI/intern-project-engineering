#!/usr/bin/env pwsh
# Vercel Deployment Script for Windows PowerShell
# Run this script to deploy the Product Review App to Vercel

Write-Host "🚀 Product Review App - Vercel Deployment Script" -ForegroundColor Cyan
Write-Host "=================================================" -ForegroundColor Cyan
Write-Host ""

# Check if we're in the correct directory
if (!(Test-Path "package.json")) {
    Write-Host "❌ Error: Not in the mobile directory!" -ForegroundColor Red
    Write-Host "Please run: cd mobile" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Checking Node.js version..." -ForegroundColor Green
$nodeVersion = node --version
Write-Host "   Node version: $nodeVersion" -ForegroundColor White

Write-Host ""
Write-Host "✅ Installing dependencies..." -ForegroundColor Green
npm install

Write-Host ""
Write-Host "✅ Building production bundle..." -ForegroundColor Green
npm run build

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "✅ Build successful!" -ForegroundColor Green
    Write-Host ""
    Write-Host "📦 Ready to deploy to Vercel!" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Choose your deployment method:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Option 1: Vercel Dashboard (Recommended)" -ForegroundColor White
    Write-Host "  1. Push to GitHub: git push origin main" -ForegroundColor Gray
    Write-Host "  2. Go to https://vercel.com" -ForegroundColor Gray
    Write-Host "  3. Import your repository" -ForegroundColor Gray
    Write-Host "  4. Configure:" -ForegroundColor Gray
    Write-Host "     - Root Directory: mobile" -ForegroundColor Gray
    Write-Host "     - Build Command: npm run build" -ForegroundColor Gray
    Write-Host "     - Output Directory: dist" -ForegroundColor Gray
    Write-Host ""
    Write-Host "Option 2: Vercel CLI" -ForegroundColor White
    Write-Host "  Run: npx vercel --prod" -ForegroundColor Gray
    Write-Host ""

    $response = Read-Host "Deploy now with Vercel CLI? (y/n)"

    if ($response -eq "y" -or $response -eq "Y") {
        Write-Host ""
        Write-Host "🚀 Deploying to Vercel..." -ForegroundColor Cyan
        npx vercel --prod

        if ($LASTEXITCODE -eq 0) {
            Write-Host ""
            Write-Host "🎉 Deployment successful!" -ForegroundColor Green
            Write-Host ""
            Write-Host "Next steps:" -ForegroundColor Yellow
            Write-Host "  1. Test the production URL" -ForegroundColor White
            Write-Host "  2. Verify the new color palette" -ForegroundColor White
            Write-Host "  3. Check all features work correctly" -ForegroundColor White
            Write-Host "  4. Update README.md with the live URL" -ForegroundColor White
        } else {
            Write-Host ""
            Write-Host "❌ Deployment failed!" -ForegroundColor Red
            Write-Host "Check the error messages above and try again." -ForegroundColor Yellow
        }
    } else {
        Write-Host ""
        Write-Host "ℹ️  Deployment skipped." -ForegroundColor Yellow
        Write-Host "Run 'npx vercel --prod' when you're ready to deploy." -ForegroundColor White
    }
} else {
    Write-Host ""
    Write-Host "❌ Build failed!" -ForegroundColor Red
    Write-Host "Please fix the errors above before deploying." -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "📚 For more information, see:" -ForegroundColor Cyan
Write-Host "  - VERCEL_DEPLOYMENT_GUIDE.md" -ForegroundColor Gray
Write-Host "  - DEPLOYMENT_SUMMARY.md" -ForegroundColor Gray
Write-Host ""
