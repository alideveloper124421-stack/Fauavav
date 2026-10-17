# Gas Cylinder Delivery & Udhaar (Khata) Management

A comprehensive, offline-first delivery and ledger management app built for gas cylinder distribution agents. It records customer details, coordinates deliveries with direct Google Maps launch, logs cylinder inventory metrics, and provides automated or manual Udhaar (pending credit balance) ledger calculations.

## User Review & Critical Decisions

> [!IMPORTANT]
> - **Udhaar Calculation Mode**: The app implements dynamic auto-calculation (`Real Rate - Amount Received = Pending Balance`) while providing an immediate manual override toggle/field as requested, allowing direct entry of any arbitrary pending amount.
> - **Fast Action Buttons**: Customer cards on the dashboard include a direct 1-tap Google Maps navigation button (opening `geo:` or web map URL with fallback), as well as direct phone dialer and WhatsApp chat triggers.
> - **Local Offline-First Persistence**: Powered by Room Database with reactive Kotlin StateFlows, ensuring instantaneous record creation, filtering, and offline resilience during field deliveries.

---

## 1. Overview & Core Concept

- **What It Does**: Enables delivery agents to manage cylinder drop-offs, record payments, calculate pending balances (Udhaar), track cylinder numbers (e.g. 20, 21, 22), and open customer drop-off locations directly in Google Maps.
- **Target Audience / Persona**: Gas cylinder delivery personnel, depot drivers, and local distribution agencies maintaining daily customer credit and delivery ledgers.
- **Key Value**: Eliminates physical paper Khata books, prevents calculation mistakes when collecting cash on delivery, and provides one-tap navigation to customer addresses.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Dashboard (Main Screen)**:
   - Header with summary stats: Total Active Deliveries, Total Amount Collected, and Total Outstanding Udhaar.
   - Search & Filter bar: Search by customer name, phone number, company, or filter by rating / pending balance.
   - Delivery Card List: Clear cards showing Customer Name, Phone, Company, Cylinder Number, Rating Badge, Real Rate, Amount Received, and Udhaar status badge (e.g., "Paid" in emerald green vs "Pending ₹X" in amber/red).
   - Direct Action Bar on each card: Prominent "Open in Maps" button, "Call Customer", and "WhatsApp" quick actions.
   - Floating Action Button (`+` Add Delivery Record) with smooth transition to the creation sheet.
2. **Add New Record Screen / Sheet**:
   - **Customer & Location Section**: Customer Name, Phone Number, Google Maps Link (with validation and paste helper), and Rating selector (Dropdown with *Excellent*, *Very Good*, *Good*, *Normal*, *Bad*, *Very Bad* accompanied by visual star/color cues).
   - **Cylinder Information Section**: Company Name (free text input with quick suggestions like Indane, HP, Bharat Gas), Cylinder Number (interactive selection chips for quick pick e.g. 20, 21, 22 + custom entry).
   - **Payment & Udhaar (Khata) Section**:
     - Real Rate (Total Price) numeric input.
     - Amount Received numeric input.
     - Live Auto-Calculated Remaining Balance card (`Rate - Received`).
     - Manual Udhaar Entry field with explicit override option if custom balance is typed.
   - **Save & Complete Action**: Primary CTA button validating fields and saving to Room DB with immediate toast notification and return to dashboard.

### Visual Identity & Theme
- **Aesthetic Direction**: Modern industrial utility with high-contrast, clean elevation cards and legible typography for sunlight visibility.
- **Color Palette**:
  - Primary: Deep Cyan / Industrial Teal (`#006874` / `#4DD8E6`)
  - Accent / Udhaar Alert: Warm Amber / Crimson (`#BA1A1A` for debt / `#006C4C` for settled)
  - Surface: Crisp light card surfaces (`#F8FAFA`) with tonal borders (`#CFE4E6`)
- **Typography**: Clean Android system typography using Material 3 scale with bold tabular figures for financial amounts.

---

## 3. Key Product Decisions & Trade-Offs

- **Local Room Database with KSP**:
  - *Chosen Approach*: Fully integrated Room DB with `@Dao`, `@Entity`, and reactive `Flow` emissions.
  - *Why*: Instant speed, zero internet dependency in remote delivery locations, and no authentication barrier.
- **Intent-Safe Google Maps Navigation**:
  - *Chosen Approach*: Handles standard Google Maps URLs (`https://maps.google.com/?q=...` or `https://goo.gl/maps/...`) and coordinates by resolving via explicit Android `Intent(Intent.ACTION_VIEW)` with fallback handling.
- **Balance Calculation Architecture**:
  - *Chosen Approach*: Auto-updates balance reactively whenever Real Rate or Amount Received changes, unless the user enters a custom manual override value in the dedicated Manual Udhaar field.

---

## 4. Technical Architecture & Data Strategy

### System & Component Diagram

```
┌────────────────────────────────────────────────────────┐
│                      MainActivity                      │
│                           │                            │
│           GasDeliveryApp (Nav / State Host)            │
│          ┌────────────────┴────────────────┐           │
│          ▼                                 ▼           │
│   DashboardScreen                  AddRecordScreen     │
│   ├─ StatsHeaderSummary            ├─ CustomerSection  │
│   ├─ SearchFilterBar               ├─ CylinderSection  │
│   └─ LazyColumn(DeliveryCards)     ├─ PaymentKhataCalc │
│         │                          └─ SaveButton       │
│         ├─ Maps Intent Launcher                        │
│         └─ Dialer Intent Launcher                      │
└───────────────────────────┬────────────────────────────┘
                            │
              DeliveryViewModel (StateFlow)
                            │
               DeliveryRecordRepository
                            │
                     DeliveryRecordDao
                            │
              SQLite / Room Database (Local)
```

### Data Model (`DeliveryRecordEntity`)
- `id`: Long (Primary Key, auto-generated)
- `customerName`: String
- `phoneNumber`: String
- `mapsLink`: String
- `customerRating`: Enum (`EXCELLENT`, `VERY_GOOD`, `GOOD`, `NORMAL`, `BAD`, `VERY_BAD`)
- `companyName`: String
- `cylinderNumber`: String (e.g., "20", "21", "22", or custom)
- `realRate`: Double
- `amountReceived`: Double
- `pendingUdhaar`: Double
- `isManualUdhaar`: Boolean
- `createdAt`: Long (timestamp)
