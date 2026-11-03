# ZExpense

**ZExpense** is an Android expense and receipt tracker for people who need to record work-related spending and prepare reimbursement or claim reports. Add expenses manually or scan a paper receipt, review the extracted vendor, amount, date, category, and VAT, then organize, analyze, and export the records as CSV or a monthly PDF claim with receipt images.

The app is designed around local-first storage: expense records live in a Room database on the device, and receipt photos are saved in the app's private files directory. Firebase Authentication and Gemini-powered receipt extraction are integrated as optional services; the app also includes local OCR as a receipt-scanning fallback.

## At a glance

- Native Android application built with Kotlin and Jetpack Compose.
- Record expenses in Saudi riyals (SAR), including vendor, category, date, VAT, notes, and receipt image.
- Scan with the camera or choose an image from the gallery; review and edit OCR results before saving.
- Search and filter expenses by date scope, category, vendor, notes, or amount.
- Review monthly totals, category breakdowns, daily spending, and calendar activity.
- Generate and share monthly PDF expense-claim reports with receipt evidence, export CSV, or share a text summary.
- Keep expense records and receipt images on the device; no Firestore expense synchronization is implemented in the current code.

## Features

### Expense records

Each expense stores an amount and currency, date, vendor, category, VAT amount, notes, optional local receipt-image path, and a sync-status field. Records can be added manually, reviewed individually, edited, deleted, and shared. The current UI and report tools use **SAR** as the default currency and display it throughout the app.

The starter categories are **Petrol**, **Parking**, **Other**, **Maintenance**, **Food**, and **Supplies**. The Add Expense screen lets the user choose from its built-in categories and offers vendor shortcuts such as Aramco, Petromin, SASCO, Shell, and ADNOC.

### Receipt capture and OCR

- Capture a receipt using the Android camera, or select an existing image from the gallery.
- The scanner supports flash control and provides a review form for the extracted fields before the expense is saved.
- `ExpenseViewModel.processReceiptOcr()` first tries `GeminiReceiptOcrEngine` for multimodal receipt parsing. If that is unavailable or fails, it calls `LocalReceiptOcrEngine`, which uses Google ML Kit text recognition and local parsing rules.
- The local parser normalizes Arabic-Indic numerals, recognizes common English and Arabic merchant/category keywords, and looks for totals, VAT, and dates.
- The receipt photo is copied to app-private storage under a year/month directory and linked to its expense record.
- A receipt image can be opened in a larger view, shared with the expense details, and included in a PDF report.

**Always review OCR output before saving.** Receipt layouts vary, and automatic extraction can misread a total, date, merchant, or VAT value. If local text recognition cannot read an image, the current ViewModel has a hard-coded sample-receipt fallback; verify the populated values rather than assuming they came from the scanned bill.

### Dashboard, lists, and calendar

- **Home:** monthly spending total, bill count, number of categories, category summaries, and recent expenses.
- **Expenses:** browse this month's or all recorded expenses, search text, and filter by category. The list shows a receipt thumbnail when an image is attached.
- **Expense detail:** review the bill image and stored fields, edit the amount/vendor/notes, share the record, or delete the expense and its associated local receipt file.
- **Calendar:** select a day in the chosen month to see that date's expenses and total.
- **Categories:** see category totals, bill counts, and each category's share of the selected month's spending.

### Reports and exports

- **Insights:** view a monthly category spending breakdown and average daily spend.
- **Reports:** see monthly expense totals, bill and category counts, and average-per-day figures.
- **PDF claim:** generate a claim document with employee name, claim period, expense table, total claim, VAT, bill count, and attached receipt images. The PDF is created in the app cache and can be opened or shared through Android's share sheet.
- **CSV:** export expense rows with ID, date, vendor, category, amount, VAT, sync-status field, notes, and whether a receipt image is attached.
- **Text summary:** share a monthly summary with total spend and category breakdown.

### Account and settings

- Sign in or create an account with email and password through Firebase Authentication when Firebase is configured.
- Continue as guest from the authentication screen.
- Edit the local profile name, email, and job title.
- Review counts of saved bills and receipt photos, manage categories, export all records to CSV, and sign out.

## How to use it

1. Install and open the Android app. Sign in, create an account, or choose **Continue as Guest / Skip**.
2. From Home, choose **Add Expense / Bill**, or use the center **Scan** action to capture a receipt.
3. For a scan, take a photo or choose one from the gallery. Check the extracted fields and correct anything needed before saving.
4. Open **Expenses** to search and filter records, then tap a record to review or edit it.
5. Choose **Reports** to inspect monthly totals and create a PDF, CSV, or shareable summary.

## Data, privacy, and service notes

- **Local expense database:** `AppDatabase` creates the Room database named `zexpense_database`. Expense records are stored in that database on the device.
- **Local receipt files:** `ReceiptStorageManager` saves receipt images inside the app's private `filesDir/Receipts/<year>/<month>/` directory. Deleting an expense also attempts to delete its local image.
- **Local-first is not the same as encrypted:** the project uses Room and app-private file storage, but the code does not configure database encryption. Do not rely on UI labels that say “Encrypted” as proof that records are cryptographically encrypted.
- **No expense cloud sync:** the entity and UI include `SyncStatus` values (`PENDING`, `SYNCED`, `FAILED`) and a sync indicator, but the current repository does not implement a remote expense-sync worker or Firestore persistence. Firebase Authentication is separate from expense storage; signing in does not, by itself, upload or restore the local ledger.
- **Sample data:** `ExpenseRepository.seedInitialDataIfEmpty()` inserts example expenses and generated sample receipt images whenever the database is empty. A fresh install therefore starts with demonstration records, not an empty ledger.
- **Authentication fallback:** `AuthRepository` uses Firebase Auth when available, but its current fallback paths can mark sign-in, registration, or guest access successful when Firebase is unavailable or a Firebase request fails. Do not treat this fallback as production authentication or use it as an authorization boundary.
- **Gemini receipt parsing:** Gemini processing sends a compressed receipt image to the Google Generative Language API when a key is configured. The Gradle secrets plugin exposes `GEMINI_API_KEY` through generated `BuildConfig`; a key placed in an Android app can be extracted from the package. Do not ship a sensitive, unrestricted API key in a production APK. Use a protected backend or appropriately restricted credentials for production.
- **VAT assumptions:** the local OCR parser uses a 15% VAT calculation when it does not detect VAT on the receipt. Confirm the actual receipt values and applicable tax rules.

## Technology

- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3, Navigation Compose
- **Architecture:** Activity + `ExpenseViewModel` + `ExpenseRepository` + Room DAO/database; coroutine flows expose database and filter state to Compose screens
- **Local persistence:** AndroidX Room
- **Camera and OCR:** CameraX and Google ML Kit Text Recognition
- **AI receipt extraction:** Gemini API via OkHttp when `GEMINI_API_KEY` is configured
- **Authentication:** Firebase Authentication (with the fallback behavior described above)
- **Reports:** Android `PdfDocument`, Android `FileProvider`, and CSV/text share intents
- **Image loading:** Coil Compose

## Project structure

```text
.
├── app/
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml       App permissions, launcher activity, FileProvider
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt        Database/repository/view-model setup
│       │   │   ├── data/
│       │   │   │   ├── auth/              Firebase auth wrapper and demo fallback
│       │   │   │   ├── export/            Expense CSV and text-summary export
│       │   │   │   ├── local/             Room database, DAO, expense entity
│       │   │   │   ├── ocr/               Gemini and on-device receipt parsers
│       │   │   │   ├── pdf/               Monthly claim PDF generator
│       │   │   │   ├── repository/        Expense persistence and sample seeding
│       │   │   │   └── storage/           Private receipt-image file management
│       │   │   └── ui/
│       │   │       ├── components/        Expense cards, category badges, sync status
│       │   │       ├── screens/           Auth, home, expense, calendar, reports, scanner
│       │   │       ├── theme/             Compose colors, typography, and theme
│       │   │       └── viewmodel/          Expense state, filters, OCR, and actions
│       │   └── res/                       Android launcher icons, themes, and file paths
│       ├── test/                          JVM, Robolectric, Compose screenshot tests
│       └── androidTest/                   Android instrumentation test scaffold
├── gradle/
│   ├── libs.versions.toml                 Version catalog for plugins and libraries
│   └── wrapper/                            Gradle wrapper configuration properties
├── .env.example                            Placeholder Gemini API key configuration
├── app/build.gradle.kts                   Android app plugins, SDK, dependencies, signing
├── build.gradle.kts                       Root Gradle plugin declarations
└── settings.gradle.kts                    Gradle repositories and app module setup
```

### How the app fits together

`MainActivity` in `app/src/main/java/com/example/MainActivity.kt` initializes `AppDatabase`, `ReceiptStorageManager`, `ExpenseRepository`, and `ExpenseViewModel`, then renders `ZExpenseApp`. `ZExpenseApp.kt` defines the navigation graph and bottom navigation for Home, Expenses, Reports, More, and the receipt scanner. Screens observe state from `ExpenseViewModel`; the view model calls the repository for Room-backed CRUD and invokes OCR/export/report helpers. `ExpenseDao` exposes Room `Flow`s so saved expense changes update the UI.

## Requirements

- Android Studio with a compatible Android Gradle Plugin / Gradle JDK.
- Android SDK matching the project's compile SDK configuration (API 36, minor API level 1).
- A device or emulator running Android 7.0 (API 24) or later (`minSdk = 24`).
- Optional: Firebase project configuration for real Firebase Authentication; Gemini API key for the cloud OCR attempt.

## Build and run

Open the cloned repository's root folder in Android Studio, allow Gradle sync to finish, and run the `app` configuration on a device or emulator.

The repository contains Gradle wrapper configuration under `gradle/wrapper/`, but does **not** include the `gradlew` / `gradlew.bat` launcher scripts. If you have a compatible Gradle installation on your PATH, you can build and run the JVM unit tests with:

```bash
gradle :app:assembleDebug
gradle :app:testDebugUnitTest
```

To install a debug build on a connected device, with Android SDK platform tools available:

```bash
gradle :app:installDebug
```

The APK is normally generated at `app/build/outputs/apk/debug/app-debug.apk`.

### Optional Firebase and Gemini configuration

- For Firebase Authentication, configure a Firebase Android app and provide its `google-services.json` in the app module as required by the Google Services Gradle plugin. The build is configured to warn if the Firebase services configuration is missing; the app's fallback authentication behavior is for demo/development only.
- For Gemini OCR, provide `GEMINI_API_KEY` using the Secrets Gradle Plugin's `.env` configuration. `.env.example` documents the placeholder. Do not commit real credentials. Since the key is generated into the app's `BuildConfig`, avoid embedding a production secret in a distributed APK.

## Tests

The repository contains basic JVM and Android test scaffolding, including `ExampleUnitTest`, an instrumentation example, and a Robolectric/Roborazzi screenshot example. The current `ExampleUnitTest` is only a placeholder arithmetic test; it does not validate expense calculations or OCR accuracy.

## License

No license file is currently included. Contact the repository owner before redistributing or reusing this project.
