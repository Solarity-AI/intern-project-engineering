# feat: redesign NotificationsView with type accent lines and glow effects

## Description

`NotificationsView.swift` currently renders notifications as a basic `List` with blue unread indicator dots, plain `Color("CardBackground")` row backgrounds, and a simple toolbar menu for mark-all-read and delete-all. The web app (`mobile/src/screens/NotificationsScreen.tsx` and `NotificationDetailScreen.tsx`) uses type-specific visual language: a 3pt left accent line per row (emerald for review, blue for order, purple for system), a circular colored icon, colored glow shadow on unread items, and a mini gradient hero in the detail view. This issue updates both the list and detail views to match.

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

- Pull request merged with the following file modified:
  - `ios/ProductReview/Presentation/Views/Notification/NotificationsView.swift`

---

## Scope Definition

**In Scope:**

**Notification type color helper (new):**
Add a `typeColor(for:)` helper (extension or private func) mapping notification type strings to colors:
- `"review"` → `AppColors.primary` (#10B981, emerald)
- `"order"` → `Color(hex: "#3B82F6")` (blue)
- `"system"` → `AppColors.aiPurple` (#8B5CF6, purple)
- Default → `AppColors.primary`

**Notification Row (update `NotificationRow` component):**
- Remove blue unread dot indicator
- Add 3pt `Rectangle` (fully opaque) on the leading edge with `typeColor(for: notification.type)` fill — use `overlay(alignment: .leading)` or `HStack` with a 3pt wide `Rectangle`
- Row background: `.glassCard()` modifier (replace flat `Color("CardBackground")`)
- Circular type icon (new): 32pt circle with `typeColor(...).opacity(0.15)` background + SF Symbol icon in `typeColor(...)`:
  - review → `star.fill`
  - order → `shippingbox.fill`
  - system → `bell.fill`
- Unread state: apply `AppGlow` shadow matching type color (opacity 0.35, radius 12) — add `.shadow(color: typeColor(...).opacity(0.35), radius: 12)` modifier
- Read state: no shadow, opacity 0.7 on the entire row
- Title: `AppColors.foreground`, 15pt semibold; body: `AppColors.foreground.opacity(0.6)`, 2-line limit
- Timestamp: relative format (e.g. "2 min ago") using `RelativeDateTimeFormatter`

**Notifications Screen Header (update):**
- "Notifications" title: `AppColors.foreground`, 28pt bold
- Unread count badge: small emerald pill with white number (if unread > 0)
- "Mark all read" toolbar button: `AppColors.primary` (emerald) tint (replace default blue tint)

**Filter Chips (new — add above list):**
- Horizontal scrollable `HStack` of chips: All | Reviews | Orders | System
- Active chip: `typeColor(for: selectedType)` gradient/fill background, white text
- Inactive chip: `.glassCard(tier: .subtle)` background, `AppColors.foreground.opacity(0.7)` text
- Filter logic: filter `NotificationsViewModel.notifications` array client-side by type

**Notification Detail View (update inline detail or `NotificationDetailView`):**
- Mini gradient hero (~120pt height):
  - Background: `LinearGradient` using `typeColor(for: notification.type)` from opacity 0.6 to 0.0 (top → bottom)
  - Large centered type icon (48pt, white)
- Type badge pill below hero: `typeColor(...)` fill, white text uppercase (e.g. "REVIEW")
- Full timestamp below badge: `AppColors.foreground.opacity(0.6)`, formatted as "Feb 14, 2026 at 14:30"
- Gradient divider below timestamp (use `AppGradients.dividerGlow` or simple 1pt emerald-fading line)
- Body text: `AppColors.foreground`, no line limit
- Product info card (if `notification.productId != nil`): `.glassCard()` with product name, emerald "View Product →" label
- "View Product" button: `AppGradients.brand` background (only if productId exists)
- "Delete" button: outlined destructive style (red border, red text)

**Out of Scope:**
- `NotificationsViewModel` logic changes
- Swipe-to-delete behavior (keep existing)
- Push notification registration

---

## Acceptance Criteria

- [ ] Each notification row has a 3pt left accent line in the correct type color (emerald/blue/purple)
- [ ] Circular type icon with colored background visible on each row
- [ ] Unread notifications have a colored glow shadow; read ones have 0.7 opacity and no shadow
- [ ] Filter chips (All / Reviews / Orders / System) filter the list correctly
- [ ] Active filter chip uses the type's color; inactive uses glass style
- [ ] "Mark all read" button tint is emerald
- [ ] Tapping a notification marks it as read (glow removed) and navigates to detail
- [ ] Detail screen mini hero shows type-specific gradient and large centered icon
- [ ] "View Product" button only appears when `notification.productId` is non-null; tap navigates to product
- [ ] "Delete" button removes notification and pops the screen
- [ ] No regressions: swipe-to-delete, mark-all-read, delete-all toolbar actions work as before
- [ ] No hardcoded `Color.blue` used for notification type colors — all via `typeColor(for:)` helper

---

## Domain-Specific Notes

- **Reference:** `mobile/src/screens/NotificationsScreen.tsx` and `NotificationDetailScreen.tsx`
- **File to modify:** `ios/ProductReview/Presentation/Views/Notification/NotificationsView.swift` (contains both list and detail views inline, or via `NotificationDetailView` inline component)
- **Type field:** `AppNotification.type` (or equivalent field on the model) — check `ios/ProductReview/Domain/Model/Notification.swift` for the exact property name; if not present, derive from `title` prefix or add a `type: String` field to the model (acceptable minimal model change)
- **Relative timestamps:** Use `RelativeDateTimeFormatter` with `unitsStyle = .full` for "2 minutes ago" format
- **Constraint:** Client-side filtering only — do NOT add a filter query parameter to the notifications API call
- **Constraint:** Do NOT modify `NotificationsViewModel` — presentation changes only
- **Dependency:** Issue 01 (`AppTheme.swift`) must be merged first

---

## Validation and Review Requirements

- **Validation:** Open notifications — each row has visible left accent line in correct color
- **Validation:** Unread row has glow; tap to open detail → navigate back → glow is gone, opacity reduced
- **Validation:** Filter by "Reviews" — only review-type notifications shown
- **Validation:** Detail screen hero gradient matches notification type
- **Reviewer:** Tech Lead / iOS reviewer
- **Definition of Done:** PR merged, type-specific accent lines and glow effects visible, filter chips functional, detail mini-hero correct, no regressions
