# feat: redesign WishlistView with bento stats cards and glass product grid

## Description

`WishlistView.swift` currently replicates the same plain grid layout as `ProductListView` — identical flat `WishlistProductCard` components with blue price text, basic filter/sort bar, and no visual distinction from the product list. The web app (`mobile/src/screens/WishlistScreen.tsx`) gives the wishlist a distinct identity through a bento-style stats section (asymmetric 2+1 glass tiles showing Items, Average Rating, Total Value), glass product cards, and an emerald accent theme. This issue updates the existing `WishlistView.swift` to match that design.

---

## Ownership, Timeline, and Effort

- **Owner:** Cenk Eren Özbek
- **Given Date:** 14-02-2026
- **Deadline:** 20-02-2026 24:00
- **Hours:** 5
- **Value:** 2
- **Week:** 7

---

## Deliverables

- Pull request merged with the following file modified:
  - `ios/ProductReview/Presentation/Views/Wishlist/WishlistView.swift`

---

## Scope Definition

**In Scope:**

**Page Header (update):**
- "My Wishlist" title in `AppColors.foreground`, 28pt bold
- "heart.fill" icon in `AppColors.primary` (emerald) to the left of title
- Grid column toggle button: SF Symbol `square.grid.2x2` / `rectangle.grid.1x2` in emerald
- "Clear All" button: `AppColors.destructive` text, only visible when wishlist is non-empty

**Bento Stats Section (new — add below header):**
- Row 1: 2 equal-width glass tiles side by side (`HStack` with equal `frame(maxWidth: .infinity)`)
  - Tile A — Items: `heart.fill` icon (emerald), large count number, "Items" label
  - Tile B — Avg Rating: `star.fill` icon (`AppColors.starFilled`), average to 1 decimal place, "Avg Rating" label
- Row 2: 1 full-width glass tile
  - Tile C — Total Value: `banknote` icon (emerald), sum formatted as currency, "Total Value" label
- All tiles: `.glassCard()` modifier, `AppRadius.xl` corner radius, `AppSpacing.lg` internal padding
- Stats computed reactively from `WishlistViewModel.products` (no new API call needed)

**Wishlist Product Cards (update `WishlistProductCard`):**
- Apply `.glassCard()` modifier (replace flat `Color("CardBackground")`)
- Price text: `AppColors.primary` (emerald) — replace current blue
- Category badge: `AppGradients.brand` pill (same as ProductCard update in Issue 02)
- Remove button ("×"): `AppColors.destructive` color, visible as a small circle overlay (top-right or bottom-right)
- Selection checkmark overlay (for multi-select): emerald circle with white checkmark

**Selection Mode & Floating Bar (update):**
- Floating "Remove X items" bar: `AppColors.destructive` gradient or solid destructive background (this is a removal action, not add — do NOT use emerald here)
- Selection checkmark: emerald fill

**Empty State (update):**
- Already uses `EmptyStateView.wishlist()` preset — update icon color to emerald
- "Browse Products" button: `AppGradients.brand` background (use existing `LoadingButton` with `.primary` style updated to emerald via AccentColor)

**Category Filter & Sort (update — same as Issue 02):**
- Active chip: `AppGradients.brand` fill
- Inactive chip: `.glassCard(tier: .subtle)` background
- Sort button text: `AppColors.foreground` (remove any `Color.black`)

**Out of Scope:**
- Changes to `WishlistViewModel` logic (undo/retry system, optimistic updates)
- Adding new wishlist items from this screen
- Sorting/filtering behavior

---

## Acceptance Criteria

- [ ] "My Wishlist" header with emerald heart icon visible at top
- [ ] Bento stats section shows 2 tiles on row 1 + 1 full-width tile on row 2, all with glass backgrounds
- [ ] Stats values (item count, avg rating, total value) update immediately when an item is removed
- [ ] Wishlist product cards use glass background; price is emerald
- [ ] Category badge on each card has emerald gradient background
- [ ] Empty state "Browse Products" button has emerald gradient background
- [ ] Active filter chip is emerald gradient; inactive is glass style
- [ ] Floating removal bar has destructive (red) background — not emerald
- [ ] Selection checkmark is emerald
- [ ] No regressions: undo removal, retry on failure, pagination all work as before
- [ ] No new `Color.blue` hardcoded values introduced

---

## Domain-Specific Notes

- **Reference:** `mobile/src/screens/WishlistScreen.tsx` — bento layout, glass tiles, stats computation
- **File to modify:** `ios/ProductReview/Presentation/Views/Wishlist/WishlistView.swift`
- **Bento layout:** Use `VStack(spacing: AppSpacing.md)` containing an `HStack` (row 1) and a single card (row 2) — do NOT use `LazyVGrid` for the bento section
- **Stats computation:** Derive `itemCount`, `averageRating`, `totalValue` as computed properties on `WishlistViewModel.products` array — no API call required
- **Existing undo/retry system:** `WishlistViewModel` already handles optimistic removal with 3-second undo window and retry on failure — do NOT modify this logic
- **Constraint:** Do NOT modify `WishlistViewModel` — presentation changes only
- **Dependency:** Issue 01 (`AppTheme.swift`) must be merged first; Issue 02 can be in parallel (same category chip pattern)

---

## Validation and Review Requirements

- **Validation:** Add 4 items to wishlist; open Wishlist — bento tiles show correct count, avg rating, and total
- **Validation:** Remove an item — bento stats update immediately without screen reload
- **Validation:** Empty wishlist — empty state shows with emerald "Browse Products" button
- **Reviewer:** Tech Lead / iOS reviewer
- **Definition of Done:** PR merged, bento stats section visible and reactive, glass cards applied, emerald accents correct, undo/retry system unaffected
