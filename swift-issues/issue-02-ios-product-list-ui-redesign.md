# feat: redesign ProductListView to match web glassmorphism aesthetic

## Description

`ProductListView.swift` currently renders a functional but visually plain product grid: flat card backgrounds using `Color("CardBackground")`, blue-tinted category pills, a basic search bar, and no hero section. The web app (`mobile/src/screens/ProductListScreen.tsx`) features a full-bleed hero with mesh gradient background and 3 decorative radial orbs, a floating glass stats bar overlapping the hero, pill search bar, glass product cards with category badge, and emerald accent throughout.

This issue updates the existing `ProductListView.swift` (and its sub-components `ProductCardView`, `StatsHeaderView`) to visually match the web design using the `AppTheme` tokens introduced in Issue 01.

---

## Ownership, Timeline, and Effort

- **Owner:** Cenk Eren Özbek
- **Given Date:** 14-02-2026
- **Deadline:** 20-02-2026 24:00
- **Hours:** 8
- **Value:** 3
- **Week:** 7

---

## Deliverables

- Pull request merged with the following files modified:
  - `ios/ProductReview/Presentation/Views/Product/ProductListView.swift`
  - `ios/ProductReview/Presentation/Components/` — any product card or stats header sub-components used by this screen

---

## Scope Definition

**In Scope:**

**Hero Section (new — add above scroll content):**
- Full-width header area (~260pt height) with `AppGradients.hero` as base background
- 3 decorative `RadialGradient` orbs placed absolutely (using `AppGradients.meshOrb`):
  - Orb 1: emerald, 0.12 opacity, top-left area (~300pt radius)
  - Orb 2: purple, 0.08 opacity, center-right (~250pt radius)
  - Orb 3: teal, 0.06 opacity, bottom area (~200pt radius)
- App brand logo / title centered in the hero
- "Product Reviews" subtitle in `AppColors.foreground.opacity(0.6)`

**Floating Stats Bar (replaces current `StatsHeaderView`):**
- Glass card (`.glassCard()` modifier) positioned at hero bottom, overlapping scroll content by ~20pt
- 3 columns in `HStack`: AVG RATING | REVIEWS | PRODUCTS
- Values from `ProductListViewModel.globalStats` (existing API call already present)
- Each stat: small label in `SecondaryText` color, large value in `AppColors.foreground`, emerald accent underline on the center stat (AVG RATING)
- Subtle `AppGlow.primarySoft` shadow on the stats bar

**Search Bar (update existing):**
- Pill shape with `.glassCard(tier: .subtle)` background (replace current gray fill)
- Magnifying glass icon in `AppColors.primary` (emerald)
- Placeholder text in `AppColors.foreground.opacity(0.4)`
- Clear button in emerald when text is present

**Category Filter Chips (update existing):**
- Inactive chip: `.glassCard(tier: .subtle)` background, `AppColors.foreground.opacity(0.7)` text
- Active chip: `AppGradients.brand` fill background, white text, `AppGlow.primarySoft` shadow
- Replace current `Color.blue.opacity(0.1)` background

**Sort Button (update existing):**
- Same glass style as inactive category chip
- Remove hardcoded `Color.black` text → use `AppColors.foreground`

**Product Cards (update `ProductCardView`):**
- Card background: `.glassCard()` modifier (replaces `Color("CardBackground")`)
- Category badge (top-left): `AppGradients.brand` pill, white text, 10pt font
- Price text: `AppColors.primary` (emerald) — replace current `.blue`
- Star rating: keep `.yellow` / `AppColors.starFilled` (no change needed)
- `AnimatedHeartButton` wishlist icon: keep existing animation; update fill color to `AppColors.primary`
- Subtle `AppShadow.soft` on each card
- On dark background: card border `Color.white.opacity(0.07)`

**Multi-select Floating Bar (update existing):**
- `AppGradients.brand` background (replace current default)
- "Add X to Wishlist" label in white

**Out of Scope:**
- Changes to `ProductListViewModel` logic
- Pagination behavior
- Search debounce logic
- Navigation to ProductDetailView

---

## Acceptance Criteria

- [ ] Hero section visible at top of screen with mesh gradient background and 3 decorative orbs
- [ ] Floating glass stats bar overlaps hero bottom; displays live AVG RATING, REVIEWS, PRODUCTS values
- [ ] Search bar uses glass background with emerald icon; hardcoded gray fill is removed
- [ ] Active category chip renders with emerald gradient fill and glow; inactive uses glass style
- [ ] Sort button text is no longer hardcoded `Color.black`; renders correctly in both dark/light mode
- [ ] Product cards use glass card background; price text is emerald (`#10B981`); category badge is emerald gradient pill
- [ ] Wishlist heart fill color is emerald when product is in wishlist
- [ ] Multi-select floating bar has emerald gradient background
- [ ] No regressions: pagination, search, filter, sort, multi-select functionality all work as before
- [ ] All colors sourced from `AppTheme`; no new hardcoded hex or `Color.blue` / `Color.black` values

---

## Domain-Specific Notes

- **Reference:** `mobile/src/screens/ProductListScreen.tsx` — visual reference for hero, stats bar, card layout
- **Files to modify:** `ProductListView.swift` (main screen), `ProductCardView` (sub-component within same file or `Components/`)
- **Existing stats fetch:** `ProductListViewModel` already calls `GET /api/products/stats` — reuse existing data, only update presentation
- **Existing category filter:** `CategoryFilter` uses `Color.blue.opacity(0.1)` for all chips — update active/inactive states
- **Constraint:** Do NOT modify `ProductListViewModel` — presentation changes only
- **Constraint:** Hero section must be part of the `ScrollView` as a sticky header or pinned header — not a separate `ZStack` layer that obscures navigation
- **Dependency:** Issue 01 (`AppTheme.swift`, `GlassCardModifier`, `GlowModifier`) must be merged first

---

## Validation and Review Requirements

- **Validation:** Side-by-side screenshot comparison with web app ProductList screen
- **Validation:** Tap category chip → active chip turns emerald; tap another → previous returns to glass style
- **Validation:** Switch to light mode → no `Color.black` text visible on white background
- **Validation:** Multi-select 2 products → floating bar appears with emerald background
- **Reviewer:** Tech Lead / iOS reviewer
- **Definition of Done:** PR merged, hero visible, all accent colors are emerald, glass cards applied, no functional regressions
