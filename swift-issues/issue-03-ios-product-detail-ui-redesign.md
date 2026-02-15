# feat: redesign ProductDetailView with cinematic hero and glass info bar

## Description

`ProductDetailView.swift` currently shows a 250pt-height image, blue category pills, blue price text, and basic gray card backgrounds (`Color(.systemGray6)`) for the AI summary and review sections. The web app (`mobile/src/screens/ProductDetailsScreen.tsx`) features a cinematic full-height hero image with a gradient fade overlay, a glass info bar that overlaps the hero's bottom edge, emerald accents throughout (category pills, rating chips, AI banner), and a purple-gradient AI summary card. This issue updates the existing view to match that visual design without touching any ViewModel logic.

---

## Ownership, Timeline, and Effort

- **Owner:** Cenk Eren Özbek
- **Given Date:** 14-02-2026
- **Deadline:** 20-02-2026 24:00
- **Hours:** 6
- **Value:** 3
- **Week:** 7

---

## Deliverables

- Pull request merged with the following file modified:
  - `ios/ProductReview/Presentation/Views/Product/ProductDetailView.swift`
  - Any sub-components used exclusively by this screen (e.g. `AISummaryCard`, `RatingBreakdownView`, `ReviewCardView` if they exist as separate files)

---

## Scope Definition

**In Scope:**

**Hero Image (update from 250pt height):**
- Increase hero image height to fill approximately 45–55% of screen height (use `UIScreen.main.bounds.height * 0.50`)
- Apply `AppGradients.hero` (`LinearGradient` from transparent at top to `AppColors.background` at bottom) as an overlay on the image using `.overlay(alignment: .bottom)`
- Remove existing hard border/placeholder background on the image container
- Image loads via existing `CachedAsyncImage` — keep as-is

**Back & Wishlist Buttons (update):**
- Back button: glass circle (`.glassCard(tier: .subtle)` + `AppRadius.full`), SF Symbol `chevron.left` in white
- Wishlist button: same glass circle; fill heart in `AppColors.primary` (emerald) when wishlisted, outline otherwise
- Both buttons positioned in a `ZStack` over the hero image, aligned to top-leading / top-trailing with safe area padding

**Product Info Overlay (update bottom of hero):**
- Category pill(s): `AppGradients.brand` background (replace current `Color.blue.opacity(0.1)`), white text, 11pt
- Product name: `AppColors.foreground`, 24pt bold, positioned over hero bottom via overlay
- Price chip: `AppColors.primary` text with `AppColors.primary.opacity(0.15)` background pill

**Glass Info Bar (new — overlap hero bottom):**
- `HStack` with `.glassCard()` modifier, negative `offset(y: -20)` to overlap the hero
- 3 sections separated by 1pt `Divider`: Rating (star icon + value) | Review Count | Price
- `AppGlow.primarySoft` shadow applied to the bar

**AI Summary Card (update from `Color.purple.opacity(0.1)`):**
- Background: `AppGradients.ai` (`LinearGradient`, purple) with reduced opacity (0.15) + `.glassCard(tier: .subtle)`
- Sparkle/AI icon in `AppColors.aiPurple`
- Title "AI Summary" in purple
- Expand/collapse behavior unchanged

**AI Assistant Banner (update):**
- Background: `AppGradients.ai` gradient
- "Ask AI about this product" label in white
- Arrow icon in white
- `AppGlow.ai` shadow
- Navigates to existing `AIAssistantView` (no change to navigation logic)

**Rating Breakdown (update `RatingBreakdownView`):**
- Progress bars: fill color → `AppColors.primary` (emerald), replace current blue/yellow
- Selected rating chip: `AppGradients.brand` background
- Unselected: glass subtle style

**Review Cards (update `ReviewCardView`):**
- Card background: `.glassCard()` modifier (replace `Color(.systemGray6)`)
- "Helpful" button: emerald outline when not voted; `AppGradients.brand` fill when voted
- Star display: unchanged (yellow)
- Reviewer name: `AppColors.foreground`, date: `AppColors.foreground.opacity(0.5)`

**Add Review FAB (update):**
- Replace solid blue circle with `AppGradients.brand` circle + `AppGlow.primary` shadow

**Out of Scope:**
- Changes to `ProductDetailViewModel` logic
- Review submission form content/validation
- AI chat functionality

---

## Acceptance Criteria

- [ ] Hero image spans ~50% of screen height; gradient overlay fades image into background color at bottom
- [ ] Back and wishlist buttons are glass circles overlaid on hero; wishlist heart is emerald when active
- [ ] Category pills have emerald gradient background
- [ ] Glass info bar overlaps hero bottom by ~20pt; displays rating, review count, price
- [ ] AI summary card has purple-tinted glass background; AI icon is purple
- [ ] AI assistant banner has purple gradient background with white text
- [ ] Rating breakdown progress bars are emerald
- [ ] Review cards use glass card background; helpful button is emerald when voted
- [ ] Add review FAB button has emerald gradient background with glow
- [ ] No regressions: review filtering, helpful vote, add review modal, AI navigation all work as before
- [ ] No new `Color.blue`, `Color.purple`, or `Color(.systemGray6)` hardcoded values introduced

---

## Domain-Specific Notes

- **Reference:** `mobile/src/screens/ProductDetailsScreen.tsx` — hero image, glass info bar, AI summary, review cards
- **File to modify:** `ios/ProductReview/Presentation/Views/Product/ProductDetailView.swift` (798-line equivalent in RN; Swift file is similarly large)
- **Existing `CachedAsyncImage`:** Already handles image loading with placeholder and error states — do NOT replace, only wrap in styled container
- **Existing `AISummaryCard` component** (in `ProductDetailView.swift` or `Components/`): update background color only
- **Existing `RatingBreakdownView`:** update bar fill color from yellow/blue to emerald; update selected chip to glass style
- **Constraint:** Do NOT modify `ProductDetailViewModel` — presentation changes only
- **Constraint:** Glass info bar overlap via `offset(y: -20)` + negative `Spacer` or `padding(.top, -20)` on the scroll content — test on multiple screen sizes
- **Dependency:** Issue 01 (`AppTheme.swift`) must be merged first

---

## Validation and Review Requirements

- **Validation:** Open any product — hero fills ~50% screen, gradient overlay visible, glass info bar overlaps
- **Validation:** Tap wishlist on detail screen — heart changes to emerald fill; product added to wishlist
- **Validation:** Rate breakdown: select 3★ filter — bar turns emerald; reviews filtered correctly
- **Validation:** Tap helpful on a review — button turns emerald filled
- **Reviewer:** Tech Lead / iOS reviewer
- **Definition of Done:** PR merged, cinematic hero visible, all emerald accents applied, glass info bar overlaps correctly, no functional regressions
