# DebtCollect — Page Features

Legend: 🟢 MVP (Free) · 👑 Pro (Paid) · 🔵 Later (Phase 7)

---

## 1. Splash
- 🟢 Show app logo while checking the session (cached `ImageBitmap` + GPU-accelerated `graphicsLayer` transition)
- 🟢 **Background Prefetching**: Hydrates local disk caches (`LocalDiskCache`) and prefetches Home data (`ProfileRepository`, `DebtRepository`, `PaymentRepository`, notes) if a session is active, or prefetches PSGC provinces (`PsgcRepository`) if logged out
- 🟢 Session found → **Home**, no session → **Login**
- 🟢 No internet → show error with **Retry**

## 2. Login
**Fields:** Email, Password (show/hide toggle)
- 🟢 Validation: valid email format, password not empty
- 🟢 **Login** button with loading state; disabled while loading
- 🟢 Error messages: wrong credentials, email not confirmed, no internet
- 🟢 Links: **Forgot password?** · **Create an account**
- 🟢 Stay logged in (session persisted)
- 🔵 Login with Google
- 🔵 Biometric / PIN unlock

## 3. Register
**Fields:** First name, Middle name (optional), Last name, Age, Address (PSGC Province, City/Municipality, Barangay, Street), Email, Phone (optional), Password, Confirm password
- 🟢 Validation: required name/age/address; valid email; PH phone format (`09XXXXXXXXX`); password at least 8 characters; passwords match
- 🟢 Password strength hint
- 🟢 Checkbox: agree to Terms & Privacy
- 🟢 **Register** → creates account + profile → **Email OTP Verification Dialog** (6-digit code, resend OTP with loading spinner & cooldown) → Home
- 🟢 Error: email already used
- 🟢 Link: **Already have an account? Login**

## 4. Forgot Password
- 🟢 Email field → **Send reset link**
- 🟢 Success message: "Check your email"
- 🟢 Resend button with a 60-second cooldown
- 🟢 Back to Login

---

## 5. Home (two tabs: **Money I Lent** | **Money I Owe**)
**Common**
- 🟢 Top bar: greeting ("Hi, Juan"), profile icon → Account Settings
- 🟢 Dynamic announcements banner (from Supabase `announcements` table, dismissible)
- 🟢 Pull to refresh & automatic debounced refresh when returning to Home
- 🟢 **Instant Cold-Start + Skeleton Loaders**: Hydrates immediately from RAM + local JSON disk cache (`LocalDiskCache`) for 0ms startup; shows low-contrast pulsing `HomeSkeletonLoader` placeholders (instead of a blank screen + spinner) only when no cached data exists yet, and empty state with an illustration and a call to action
- 🟢 Remembers the last tab you opened

### 5a. Money I Lent tab
- 🟢 **Summary cards:** Total Lent · Collected · Outstanding · Overdue count
- 🟢 **Banner:** "You have N payments waiting for confirmation" → opens the pending list
- 🟢 **Debt list** (card per debt): borrower name (or "Awaiting borrower"), title, balance, progress bar, due date, status chip
- 🟢 Filter chips: All · Awaiting · Active · Overdue · Paid
- 🟢 **Debts tab banner:** `"N debt(s) awaiting a borrower"` quick-filter banner to find unjoined debts and share their invite code
- 🟢 Search by title or borrower name
- 🟢 Sort: due date / balance / newest
- 🟢 **FAB "+ New Debt"** → Create Debt

### 5b. Money I Owe tab
- 🟢 **Summary cards:** Total Owed · Total Paid · Next Due (earliest unpaid monthly installment date + amount, advancing automatically when installments are paid early)
- 🟢 **Debt list:** lender name, title, balance, progress, due date, status chip
- 🟢 Badge on a debt if a payment is pending or was rejected
- 🟢 Filter chips: All · Active · Overdue · Paid
- 🟢 **Button "Join Debt"** → Join Debt screen

---

## 6. Create Debt (lender)
**Fields:** Title, Principal amount, Interest type (**Flat Rate ₱** or **Percentage %**), Interest value (optional), Duration in months (with quick presets `3m`, `6m`, `12m`, `24m`), Payment day of month, Date given (default today), Due date (auto-computed from duration + payment day, editable), Notes
- 🟢 Live preview: **Total to collect** and **Monthly Payment** (`total / months` on chosen day of month)
- 🟢 Validation: title required, principal > 0
- 🟢 **Create & Generate Code** → goes to Invite Code
- 👑 **Unlimited active debts** (Free tier capped at 5 active debts; prompts OWE Pro upgrade dialog)
- 🔵 Discard-changes confirmation on back

## 7. Invite Code (lender)
- 🟢 Large code display (`DC-7K4P9Q`)
- 🟢 **Copy** button (toast "Copied")
- 🟢 **Share** → Android share sheet with a pre-written message: *"Juan added a debt of ₱5,000 in DebtCollect. Join using code DC-7K4P9Q"*
- 🟢 Expiry countdown ("Expires in 6 days")
- 🟢 **Regenerate code** (makes the old one invalid)
- 🟢 **Done** → Debt Details
- 🔵 QR code to scan
- 🔵 Deep link (tap the link to open the app straight to Join)

## 8. Join Debt (borrower)
- 🟢 Code input (auto uppercase, auto-format `DC-`)
- 🟢 Paste from clipboard button
- 🟢 **Find** → preview card: lender name, title, total amount, due date, notes
- 🟢 **Accept** / **Cancel**
- 🟢 Errors: invalid code, expired, already used, "This is your own debt"
- 🟢 On accept → Debt Details
- 🔵 Scan QR

---

## 9. Debt Details (both; actions depend on role)
**Header**
- 🟢 Title, status chip, other party's name and phone (tap to call or text)
- 🟢 Amounts: Total · Paid · **Balance** · progress bar
- 🟢 Date given, next unpaid installment due date (computed from monthly schedule & `total_paid`), final due date, and installment progress (`X of Y installments paid`)
- 🟢 Notes

**Payment timeline**
- 🟢 List of all payments: amount, date, method, reference no., status chip (Pending / Confirmed / Rejected), who recorded it
- 🟢 Rejected payments show the reason
- 🟢 Tap a payment → payment detail sheet

**Lender actions**
- 🟢 **Record Payment** (cash received, etc.)
- 🟢 **Review** pending payments (Confirm / Reject)
- 🟢 **Share Invite Code** (shows real `invite_code` or generates one while `awaiting_borrower`)
- 🟢 **Edit debt** (only before any confirmed payment)
- 🟢 **Cancel debt** (with confirmation)
- 🟢 **Mark as Paid** manually (forgive the remaining balance, with confirmation)

**Borrower actions**
- 🟢 **Submit Payment**
- 🟢 **Cancel** your own pending payment
- 🟢 **Resubmit** a rejected payment (prefilled)

- 👑 Export / share statement as PDF
- 🔵 Realtime updates when the other party acts

## 10. Submit Payment (borrower)
**Fields:** Amount (prefilled with the monthly installment from the debt's schedule when available, or remaining balance), Payment date (locked to today's date `YYYY-MM-DD`, non-editable), Method (Cash / GCash / Maya / Bank Transfer / Other), Reference no., Note
- 🟢 Reference no. **required when method is not Cash**; hidden or optional for Cash
- 🟢 "Other" method → extra text field to specify
- 🟢 Shows real lender name, debt title, and current balance; warning if the amount is more than the balance
- 🟢 Quick buttons: **Pay full balance**
- 🟢 Confirmation dialog: "Submit ₱1,000 via GCash (Ref 1234…)?"
- 🟢 After submitting → status **Pending**, message "Waiting for lender confirmation"
- 🟢 Duplicate guard: warn if the same reference no. was already used on this debt
- 👑 Attach a proof screenshot (camera / gallery)

## 11. Record Payment (lender)
- 🟢 Same form as Submit Payment
- 🟢 Saved as **Confirmed** right away
- 🟢 Balance updates; debt becomes **Paid** automatically when the balance reaches 0

## 12. Review Payment (bottom sheet, lender)
- 🟢 Opens from **Review Pending Payment** or by tapping a pending payment card
- 🟢 Shows real amount, method, reference no., payment date, submitter name, and optional note
- 🟢 **Optimistic UI Updates**: **Confirm Payment** and **Reject** immediately update the payment status chip, adjust the debt balance/paid progress, and dismiss the bottom sheet in 0ms while syncing the `review_payment` RPC in the background (with automatic state rollback if the network request fails)
- 🔵 Copy reference no. & view the proof image

---

## 13. Account Settings
- 🟢 Profile header: avatar (initials), name, email, verified badge
- 👑 **OWE Pro banner**: shows current tier (Free vs Pro Active) → opens **Pro Upgrade / Paywall**
- 🟢 **Edit Profile**: name, phone
- 🟢 **Change Password**: current, new, confirm
- 🟢 **Change Email** (with verification)
- 🟢 Notification preferences (placeholder until reminders exist)
- 🟢 Theme: Light / Dark / System
- 👑 **Export Statements** (PDF summary; gated behind OWE Pro)
- 🟢 About: app version, Terms, Privacy Policy
- 🟢 **Logout** (with confirmation)
- 🟢 **Delete Account** (blocked while you have active debts; requires typing "DELETE")
- 🔵 Upload a profile photo
- 🔵 Default currency

## 14. Notifications
- 🟢 In-app notifications list (`NotificationsScreen`) backed by Supabase `notifications` table + Realtime updates
- 🟢 Live unread badge on Home bell icon
- 🟢 Tap notification to mark as read and jump to Debt Details, plus **Mark all read**
- 👑 Automated due-date reminders (3 days before / due today / overdue)

## 15. Pro Upgrade / Paywall (👑 Pro)
- 👑 Plan comparison: Free (up to 5 active debts, AdMob banner ads on Home/Debts/Activity + cooldown-capped interstitials after debt creation or payment submission) vs **Monthly** (₱49/month) or **One-Time Pro** (₱199 once), dynamically configurable via Supabase `app_config`
- 👑 Benefits: Unlimited active debts, PDF statement export, payment proof uploads, automated due-date reminders, ad-free experience
- 👑 Upgrade activation + Restore Purchase (local device state persisted; ready for Google Play Billing integration)

---

## Global / Cross-cutting & Performance Architecture
- 🟢 Peso formatting everywhere (`₱1,234.50`) using cached thread-local `NumberFormat` instances
- 🟢 Dates formatted as `Nov 12, 2026`
- 🟢 **Skeleton Loaders (`HomeSkeletonLoader`, `DebtDetailsSkeletonLoader`)**: Low-contrast pulsing placeholder cards that mimic the incoming layout on uncached initial loads instead of blank screens or heavy spinning wheels, plus empty and error states with Retry
- 🟢 **Two-Tier Memory & Local Disk Caching (`LocalDiskCache` + `ConcurrentHashMap`)**: Stores `profiles`, `debts`, `debt_notes`, `payments`, `notifications`, and PSGC locations in both RAM and internal JSON disk storage (`filesDir/owe_data_cache` & `psgc_cache`, cleared on logout) so screens render in 0ms on cold launch before checking the network (Stale-While-Revalidate)
- 🟢 **Optimistic UI Updates**: Immediate interface updates for payment reviews (Confirm/Reject), payment submissions, and notification read states (`markAsRead` / `markAllAsRead`) with background Supabase sync and rollback on failure
- 🟢 **Background Prefetching**: Splash screen prefetches user profile, debts, payments, and notes (or PSGC provinces when logged out); `HomeViewModel` prefetches all debt payments and notes in parallel (`async` on `Dispatchers.Default`) so **Debt Details** and **Submit Payment** open instantly
- 🟢 **Sparse Fieldsets & Lazy Loading**: Supabase queries request only needed columns via `Columns.list(...)` with query `.limit(...)` caps, and all lists use `LazyColumn` with stable `key`s, `contentType`s, and `@Immutable` UI state models
- 🟢 **Budget-Device Optimization (Galaxy A12 Ready)**: R8 minification & resource shrinking enabled on release builds, `MobileAds` + Realtime WebSockets offloaded to `Dispatchers.IO`, cached `AdView` and `ImageBitmap` instances, and lightweight hardware-accelerated transitions
- 🟢 **100% Kotlin Coroutines**: Uses modern structured concurrency (`viewModelScope`, `lifecycleScope`, `StateFlow`, `Dispatchers.IO` / `Dispatchers.Default`) — zero deprecated `AsyncTask`
- 🟢 Global **No Internet Connection** dialog (with Retry & Dismiss) when offline
- 🟢 Success & error floating pill snackbar (`AppFeedbackHost`) across actions
- 🟢 Dark mode support
- 🟢 Back navigation and keeping your input when the screen rotates
- 🔵 Filipino language option

> [!NOTE]
> Questions from the plan are still open: Supabase project (new or existing), flat interest, email confirmation, package name. If you don't answer, I'll use the defaults: new project, flat interest, confirmation off during development, keep the package name.
