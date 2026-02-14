# feat: add AppTheme design token system with glass and glow modifiers

## Description

The existing iOS app at `ios/ProductReview/App/Core/` has only 7 basic color assets in `Assets.xcassets` (AppBackground, CardBackground, AccentColor, Border, PrimaryText, SecondaryText, SurfaceMuted) and no gradient, glassmorphism, or glow definitions. The web frontend (`mobile/src/constants/theme.ts`) defines a comprehensive design token system — Colors, Gradients, Glass, Glow, Spacing, FontSize, BorderRadius, Shadow — that drives the entire glassmorphic dark-mode aesthetic with emerald (#10B981) as the primary accent.

This issue introduces a single source-of-truth `AppTheme.swift` file and companion SwiftUI `ViewModifier`s to make glass cards, brand gradients, and glow effects reusable across all screens. It is the prerequisite for all subsequent UI redesign issues (02–07).

---

## Ownership, Timeline, and Effort

- **Owner:** Cenk Eren Özbek
- **Given Date:** 14-02-2026
- **Deadline:** 20-02-2026 24:00
- **Hours:** 4
- **Value:** 2
- **Week:** 7

---

## Deliverables

- Pull request merged containing:
  - `ios/ProductReview/App/Core/AppTheme.swift` — all token enums/structs
  - `ios/ProductReview/Presentation/Components/GlassCardModifier.swift` — reusable glass ViewModifier
  - `ios/ProductReview/Presentation/Components/GlowModifier.swift` — reusable glow shadow ViewModifier
  - Updated `Assets.xcassets/AccentColor.colorset` — set to emerald `#10B981` for both light and dark appearances
  - Xcode Preview canvas showing all tokens visually

---

## Scope Definition

**In Scope:**

**`AppTheme.swift` contents:**

- `AppColors` — all color tokens matching `theme.ts` exact hex values:
  - `background`: `#0B1120`, `foreground`: `#F1F5F9`, `card`: `rgba(15,23,42,0.55)`
  - `primary` (emerald): `#10B981`, `starFilled`: `#FBBF24`, `destructive`: `#F87171`
  - `aiPurple`: `#8B5CF6`, `premiumGold`: `#F59E0B`

- `AppGradients` — SwiftUI gradient definitions:
  - `brand`: `#10B981` → `#059669` (emerald, `LinearGradient`)
  - `ai`: `#8B5CF6` → `#6366F1` (purple, `LinearGradient`)
  - `premium`: `#F59E0B` → `#FBBF24` (gold, `LinearGradient`)
  - `hero`: subtle dark multi-stop `LinearGradient` (transparent → `#0B1120`)
  - `meshOrb(color:opacity:)`: factory for decorative `RadialGradient` orbs

- `AppGlass` — glass tier definitions (as `(background: Color, opacity: Double, blur: Material)` tuples):
  - `card`: `rgba(15,23,42,0.55)`, `.ultraThinMaterial`
  - `strong`: `rgba(15,23,42,0.70)`, `.thinMaterial`
  - `subtle`: `rgba(15,23,42,0.35)`, `.ultraThinMaterial`
  - `elevated`: `rgba(15,23,42,0.75)`, `.regularMaterial`

- `AppGlow` — shadow definitions (as `(color: Color, opacity: Double, radius: CGFloat)` tuples):
  - `primary`: emerald, 0.45, 20
  - `primarySoft`: emerald, 0.25, 14
  - `accent`: gold, 0.40, 18
  - `ai`: purple, 0.45, 20

- `AppSpacing` — `CGFloat` constants: `xs`(4), `sm`(8), `md`(12), `lg`(16), `xl`(24), `x2l`(32), `x3l`(40), `x4l`(48), `x5l`(64)

- `AppFontSize` — `CGFloat` constants: `xs`(12) through `x6l`(52)

- `AppRadius` — `CGFloat` constants: `sm`(6), `md`(8), `lg`(12), `xl`(16), `x2l`(24), `x3l`(32), `full`(9999)

**`GlassCardModifier`:**
- `.background(.ultraThinMaterial)` + color overlay at specified opacity
- 1pt border with `Color.white.opacity(0.08)` stroke (glass edge highlight)
- `cornerRadius` parameter (defaults to `AppRadius.xl`)

**`GlowModifier`:**
- Applies `shadow(color:radius:x:y:)` using `AppGlow` parameters
- Chainable

**AccentColor update:**
- Set `#10B981` for Dark appearance and a slightly adjusted value for Light appearance in `Assets.xcassets`

**Out of Scope:**
- Any screen-level changes (issues 02–07)
- Custom font loading
- Light mode color set (light mode tokens only where needed to avoid regressions)
- Animation utilities

---

## Acceptance Criteria

- [ ] `AppTheme.swift` compiles with no warnings; all token names match `theme.ts` semantically
- [ ] `Color("AccentColor")` resolves to `#10B981` in dark mode on device/simulator
- [ ] `.glassCard()` modifier applied to any `View` renders semi-transparent dark card with blur effect
- [ ] `.glow(.primary)` modifier applied to a button renders emerald colored shadow with 0.45 opacity
- [ ] `AppGradients.brand` creates a valid `LinearGradient` usable directly in SwiftUI views
- [ ] `AppGradients.meshOrb(color: .purple, opacity: 0.15)` returns a centered `RadialGradient`
- [ ] Preview canvas renders color swatches, gradient tiles, and glass/glow demo views without errors
- [ ] No hardcoded hex strings exist outside `AppTheme.swift` after this change
- [ ] Existing app still builds and runs without regressions (existing views unaffected)

---

## Domain-Specific Notes

- **Reference file:** `mobile/src/constants/theme.ts` — exact hex values, opacity levels, blur tier names
- **Existing code:** `ios/ProductReview/App/Core/ThemeManager.swift` — existing ThemeManager manages `colorScheme`; `AppTheme.swift` complements it with static token values, does NOT replace it
- **Constraint:** Glass effect must use `.ultraThinMaterial` or `.thinMaterial` (iOS 15+ system materials) — avoid custom blur as it is not supported in standard SwiftUI
- **Constraint:** `AppTheme` tokens are `static let` / `static func` values — no instances needed
- **Note:** The `meshOrb` gradient is a decorative `RadialGradient` with `startRadius: 0, endRadius: 200` and centered at `.center`; used as background layer in hero sections
- **Current branch:** `feat/fe-deployment`; the merged `ios-development` branch confirms iOS is an active workstream

---

## Validation and Review Requirements

- **Validation:** Build succeeds with 0 errors and 0 warnings on Xcode 16, iOS 17 simulator
- **Validation:** Visual review of Xcode Preview canvas — all token tiles render correctly
- **Validation:** Apply `.glassCard()` to one existing card view in ProductListView as a smoke test
- **Reviewer:** Tech Lead / iOS reviewer
- **Definition of Done:** PR merged, `AppTheme.swift` is the sole source for all color/gradient/glass/glow constants, AccentColor is emerald, existing screens unaffected
