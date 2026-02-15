# feat: redesign AIAssistantView with purple gradient header and glass bubbles

## Description

`AIAssistantView.swift` currently renders a functional but plain chat UI: a standard navigation title, predefined question buttons with `Color.blue.opacity(0.1)` backgrounds, a basic `.roundedBorder` text input, and message bubbles without distinct glass styling. The web app (`mobile/src/screens/AIAssistantScreen.tsx`) has a purple gradient header with AI icon, decorative background orbs, role-differentiated message bubbles (user = emerald right-aligned, assistant = glass card left-aligned with purple left border), a 2-column option grid with emerald left-accent lines, and Yes/No single-row layout for followup questions.

This issue updates the existing `AIAssistantView.swift` to match the web aesthetic without changing any chat logic or state management.

---

## Ownership, Timeline, and Effort

- **Owner:** Cenk Eren Özbek
- **Given Date:** 14-02-2026
- **Deadline:** 20-02-2026 24:00
- **Hours:** 5
- **Value:** 3
- **Week:** 7

---

## Deliverables

- Pull request merged with the following file modified:
  - `ios/ProductReview/Presentation/Views/AI/AIAssistantView.swift`

---

## Scope Definition

**In Scope:**

**Header (replace navigation title):**
- Custom header `VStack` or `HStack` with `AppGradients.ai` background (purple → indigo `LinearGradient`)
- Centered layout: sparkle/wand SF Symbol icon (32pt, white) above title
- Title "AI Assistant" in white, 20pt bold
- Subtitle: product name in `Color.white.opacity(0.7)`, 14pt
- Review count label: "Based on X reviews" in `Color.white.opacity(0.55)`, 12pt
- Remove default `NavigationTitle`; use `.toolbarBackground(.hidden, for: .navigationBar)` + custom inlined header
- `AppGlow.ai` shadow on header (purple glow)

**Decorative Background:**
- 2 fixed-position `RadialGradient` orbs in the scroll background (not interactive):
  - Orb 1: `AppColors.aiPurple` at opacity 0.08, ~280pt radius, top-right area
  - Orb 2: `AppColors.primary` (emerald) at opacity 0.05, ~220pt radius, bottom-left area
- Implemented as a `ZStack` background behind the `ScrollView`

**User Message Bubble (update):**
- Background: `AppGradients.brand` (emerald gradient)
- Alignment: trailing (right-aligned)
- Corner radius: `AppRadius.xl` on all corners except bottom-trailing (4pt) → simulate chat bubble
- "You" label above bubble: `AppColors.foreground.opacity(0.5)`, 11pt
- Timestamp below: `AppColors.foreground.opacity(0.4)`, 10pt

**Assistant Message Bubble (update):**
- Background: `.glassCard()` modifier
- Left 3pt accent `Rectangle` in `AppColors.aiPurple` (purple)
- AI icon (wand.and.stars or sparkles): 20pt, `AppColors.aiPurple`, top-left of bubble
- Text: `AppColors.foreground`, 15pt
- Alignment: leading (left-aligned)
- Timestamp below: `AppColors.foreground.opacity(0.4)`, 10pt

**Predefined Option Buttons — Initial State (update 2-column grid):**
- Layout: 2-column `LazyVGrid` (replace existing `VStack` or single-column layout if applicable)
- Each button:
  - Background: `.glassCard(tier: .subtle)`
  - Leading 2pt `Rectangle` accent in `AppColors.primary` (emerald)
  - Button text: `AppColors.foreground`, 14pt, leading-aligned
  - Disabled state: opacity 0.5, non-interactive during loading

**Followup Option Buttons — Yes/No State (update):**
- Layout: single `HStack` row (not grid)
- Same glass + emerald accent styling as above
- Determined by `waitingForMore` flag in the API response (existing logic)

**Loading / Typing Indicator (update):**
- Existing spinner → replace with 3-dot animated pulse indicator:
  - 3 circles (8pt each), `AppColors.aiPurple` fill
  - Staggered `withAnimation(.easeInOut(duration: 0.5).repeatForever())` opacity animation (0.3 → 1.0 → 0.3)
  - Wrapped in a glass card bubble (assistant bubble style, no text)

**Text Input Bar (update):**
- Background: `.glassCard(tier: .subtle)` (replace `.roundedBorder`)
- Placeholder: `AppColors.foreground.opacity(0.4)`
- Send button: `AppGradients.brand` circle, SF Symbol `arrow.up` in white
- Disabled state when loading: send button opacity 0.4

**Out of Scope:**
- Chat logic, message state, or `AIAssistantViewModel` changes
- Request deduplication logic (already implemented)
- Auto-scroll behavior (already implemented)

---

## Acceptance Criteria

- [ ] Purple gradient header visible with AI icon, "AI Assistant" title, product name, and review count
- [ ] Two decorative background orbs visible (purple top-right, emerald bottom-left) at low opacity
- [ ] User message bubbles render with emerald gradient background, right-aligned
- [ ] Assistant message bubbles render as glass cards with purple left accent, left-aligned
- [ ] Initial predefined options display in 2-column grid layout with glass + emerald left-accent styling
- [ ] All option buttons are disabled (opacity 0.5) while an API call is in progress
- [ ] Yes/No followup options appear in a single horizontal row
- [ ] 3-dot animated loading indicator appears while awaiting AI response
- [ ] Text input field uses glass background; send button is emerald circle
- [ ] Send button is visually disabled while loading
- [ ] No regressions: conversation flow, option selection, auto-scroll, error handling all work as before
- [ ] No `Color.blue.opacity(0.1)` or `.roundedBorder` style used

---

## Domain-Specific Notes

- **Reference:** `mobile/src/screens/AIAssistantScreen.tsx` — header, orbs, bubble styles, option grid
- **File to modify:** `ios/ProductReview/Presentation/Views/AI/AIAssistantView.swift`
- **Existing logic to preserve:** `isProcessing`/`isLoading` flag, `waitingForMore` flag for option layout, `Task.cancel()` / dedup pattern, auto-scroll to latest message
- **Constraint:** Header must NOT use `NavigationTitle` — use `.toolbar(.hidden, for: .navigationBar)` and render a custom header as the first element inside the view body
- **Constraint:** 3-dot animation — use `@State var dotOpacity: [Double] = [1, 1, 1]` with staggered `Task.sleep` delays in a `.task` modifier; do NOT use third-party animation library
- **Note:** `waitingForMore` field name — verify exact property name in the existing `ChatResponse` DTO in `ios/ProductReview/Data/Network/DTOs.swift`
- **Dependency:** Issue 01 (`AppTheme.swift`) must be merged first

---

## Validation and Review Requirements

- **Validation:** Open AI assistant from any product detail — purple gradient header visible with product name
- **Validation:** Tap a predefined option — user bubble (emerald) appears → 3-dot loader → assistant bubble (glass + purple accent) appears
- **Validation:** While loading — all option buttons are non-interactive (test by rapidly tapping during load)
- **Validation:** Input bar — type text → send button becomes active (emerald) → tap → sends message
- **Reviewer:** Tech Lead / iOS reviewer
- **Definition of Done:** PR merged, full visual redesign applied, all bubble types render correctly, loading indicator animated, no logic regressions
