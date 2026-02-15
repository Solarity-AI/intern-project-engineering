# fix: replace all hardcoded colors with AppTheme tokens across iOS codebase

## Description

A codebase audit of the existing iOS Swift app reveals multiple instances of hardcoded color values scattered across views and components that bypass the theme system: `Color.blue` (price labels, category pills, sort icon tint, helpful button), `Color.black` (sort direction arrow text — breaks dark mode), `Color.purple.opacity(0.1)` (AI summary background), `Color(.systemGray6)` (review card backgrounds), and `Color.red` (wishlist heart fill — conflicts with emerald accent identity). In addition, `Assets.xcassets/AccentColor.colorset` is not set to the target emerald `#10B981`. These inconsistencies cause color drift, dark mode regressions, and prevent the design token system (Issue 01) from being the single source of truth.

This issue is a targeted cleanup pass that replaces all hardcoded color references with `AppTheme` tokens across the entire iOS codebase.

---

## Ownership, Timeline, and Effort

- **Owner:** Cenk Eren Özbek
- **Given Date:** 14-02-2026
- **Deadline:** 20-02-2026 24:00
- **Hours:** 3
- **Value:** 1
- **Week:** 7

---

## Deliverables

- Pull request merged containing:
  - All hardcoded color replacements across `ios/ProductReview/Presentation/`
  - Updated `Assets.xcassets/AccentColor.colorset` → emerald `#10B981` (Dark) / `#059669` (Light)
  - No new hardcoded color values introduced

---

## Scope Definition

**In Scope — Known hardcoded colors to fix:**

| Location | Hardcoded Value | Replace With |
|----------|-----------------|--------------|
| `ProductListView` / `ProductCardView` — price label | `Color.blue` | `AppColors.primary` |
| `ProductListView` — category chip (inactive) | `Color.blue.opacity(0.1)` | `.glassCard(tier: .subtle)` or `AppColors.primary.opacity(0.1)` |
| `ProductListView` — sort direction button text | `Color.black` | `AppColors.foreground` |
| `ProductDetailView` — price label | `Color.blue` | `AppColors.primary` |
| `ProductDetailView` — category pill background | `Color.blue.opacity(0.1)` | `AppColors.primary.opacity(0.15)` |
| `ProductDetailView` — AI summary card bg | `Color.purple.opacity(0.1)` | `AppColors.aiPurple.opacity(0.15)` |
| `ProductDetailView` — review card background | `Color(.systemGray6)` | `AppColors.card` (or `.glassCard()`) |
| `ProductDetailView` — "Helpful" button (voted) | default secondary | `AppColors.primary` tint |
| `AIAssistantView` — predefined question buttons | `Color.blue.opacity(0.1)` | `AppColors.primary.opacity(0.1)` |
| `WishlistView` / `WishlistProductCard` — price | `Color.blue` | `AppColors.primary` |
| Any file — `Color.black` used for text | `Color.black` | `AppColors.foreground` |
| Any file — `Color.red` for wishlist heart | `Color.red` | `AppColors.primary` (emerald) |
| `Assets.xcassets/AccentColor` | Unknown (likely blue) | `#10B981` dark / `#059669` light |

**Audit process:**
- Run `grep -r "Color.blue\|Color.black\|Color.red\|systemGray\|purple.opacity" ios/ProductReview/Presentation/` to find all instances
- Fix each occurrence
- Re-run grep to confirm 0 remaining hits

**Out of Scope:**
- `Color.yellow` / `Color(.systemYellow)` for star ratings — intentional, keep as-is or map to `AppColors.starFilled`
- `Color.green` system color if used for status indicators (keep unless clearly wrong)
- Any color inside `Assets.xcassets` other than `AccentColor`
- Design changes beyond color replacement (layout, sizing, etc.)

---

## Acceptance Criteria

- [ ] `grep -r "Color\.blue" ios/ProductReview/Presentation/` returns 0 results
- [ ] `grep -r "Color\.black" ios/ProductReview/Presentation/` returns 0 results
- [ ] `grep -rE "Color\(.systemGray" ios/ProductReview/Presentation/` returns 0 results
- [ ] `grep -r "purple\.opacity" ios/ProductReview/Presentation/` returns 0 results
- [ ] `Assets.xcassets/AccentColor.colorset` resolves to `#10B981` in Dark mode
- [ ] App builds with 0 errors and 0 warnings after changes
- [ ] Price labels across ProductList, ProductDetail, and Wishlist all display in emerald (#10B981)
- [ ] Sort direction button text is visible in both dark mode and light mode
- [ ] AI summary card background is purple-tinted (not blue)
- [ ] Review cards use card background color from `AppTheme`, not `systemGray6`

---

## Domain-Specific Notes

- **Audit command:** `grep -rn "Color\.blue\|Color\.black\|Color\.red\|systemGray6\|purple\.opacity\|Color\.purple" ios/ProductReview/Presentation/ --include="*.swift"`
- **`AppColors` availability:** This issue depends on Issue 01 (`AppTheme.swift`) being merged. If running in parallel, use placeholder constants and update imports when Issue 01 merges.
- **`Color.red` for wishlist:** The existing `AnimatedHeartButton` uses `.red` fill. Update to `AppColors.primary` to align with emerald identity. Verify this does not conflict with the destructive action color for removal buttons.
- **`AccentColor` in Xcode:** Modifying `AccentColor.colorset` affects all default SwiftUI controls (default button tint, `Toggle`, `ProgressView`, etc.) — verify no unintended visual regressions on the Settings screen.
- **Constraint:** This is a mechanical replacement issue — do NOT redesign layouts. Smallest possible diff.
- **Dependency:** Issue 01 (`AppTheme.swift`) must be merged or available in branch before this work begins

---

## Validation and Review Requirements

- **Validation:** Run the grep audit commands listed in Acceptance Criteria — all must return 0 results
- **Validation:** Visual review: launch app in Dark mode — all text readable, no black text on dark background
- **Validation:** Visual review: launch app in Light mode — no regressions from theme changes
- **Validation:** Settings screen: `Toggle` and picker controls use emerald AccentColor tint
- **Reviewer:** Tech Lead / iOS reviewer
- **Definition of Done:** PR merged, 0 grep hits for banned color literals, AccentColor is emerald, no dark mode regressions, app builds cleanly
