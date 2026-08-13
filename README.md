# AI Audio Book Client App - Setup & Credentials Guide

This repository contains the read-only Android client app for the AI Audio Book platform. It uses Firebase Authentication (supporting Email/Password and Google Sign-In) and Firestore.

---

## 1. Firebase Configuration (`google-services.json`)
The application requires a `google-services.json` file. It has been moved to the `app/` directory:
* Path: `app/google-services.json`

If you create a new Firebase project, download `google-services.json` from the Firebase Console and place it at `app/google-services.json`. Make sure the package name matches `com.rodrigos01.aiaudiobook`.

---

## 2. Google Sign-In Credentials & SHA-1 Fingerprint
To use Google Sign-In, Firebase requires your machine's SHA-1 certificate fingerprint to be added to your Firebase project:

### Step 2.1: Generate your SHA-1 Fingerprint
Run the following command from the root directory of this Android project:
```powershell
./gradlew signingReport
```
Look for the `debug` variant in the output. It will show details like:
```text
Variant: debug
Config: debug
Store: C:\Users\rodri\.android\debug.keystore
Alias: AndroidDebugKey
MD5: XX:XX:XX...
SHA-1: 3B:DA:A0:5B:4F:14:32:1A:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
SHA-256: ...
```

### Step 2.2: Add SHA-1 to Firebase Console
1. Open the [Firebase Console](https://console.firebase.google.com/).
2. Select your project: `ai-audio-book`.
3. Click the gear icon next to **Project Overview** and select **Project Settings**.
4. Scroll down to **Your apps**, choose the Android app (`com.rodrigos01.aiaudiobook`).
5. Click **Add fingerprint** under **SHA certificate fingerprints**.
6. Paste the SHA-1 fingerprint generated in Step 2.1.
7. Click **Save**.

---

## 3. Enable Authentication Providers in Firebase
Before trying to log in or register, ensure the following are enabled in the Firebase Console (**Build > Authentication > Sign-in method**):
1. **Email/Password**: Enable both **Email/Password** and **Email link (passwordless sign-in)** (optional, but Email/Password is required).
2. **Google**: Enable the Google sign-in provider. Under configuration:
   * Verify the **Web SDK configuration** fields are filled (they are populated automatically from your project details).
   * Note the **Web client ID** (this matches the ID configured in the app for signing in).

---

## 4. Firestore Database Setup
Make sure the Cloud Firestore database is created in the same Firebase project.
The Security Rules must match the specs in `docs/firestore_schema.md` to allow read operations for authenticated users (`user:<firebase-uid>`).

---

## 5. CI/CD: Build & Distribute on Merge to `master`

The workflow at `.github/workflows/release-prod.yml` builds the `prod` release flavor and
uploads it to Firebase App Distribution every time a commit is pushed/merged to `master`
(it can also be run manually via **Actions > Build and Distribute (Prod) > Run workflow**).

None of the required credential files are committed to the repo, so the workflow writes them
from GitHub Actions secrets at build time. Add the following **repository secrets**
(**Settings > Secrets and variables > Actions > New repository secret**):

| Secret name | Contents |
| --- | --- |
| `GOOGLE_SERVICES_JSON` | Raw contents of `app/google-services.json` (plain text, paste as-is) |
| `FIREBASE_APP_DISTRIBUTION_CREDENTIALS` | Raw contents of the Firebase service account JSON used by the App Distribution Gradle plugin (`app/ai-audio-book-2c2ff064ff10.json`, plain text, paste as-is) |
| `RELEASE_KEYSTORE_BASE64` | Base64 of the release signing keystore (`app/ai-audio-book-keystore`) — this one is binary, so it must stay base64-encoded |
| `RELEASE_STORE_PASSWORD` | Keystore password |
| `RELEASE_KEY_ALIAS` | Signing key alias |
| `RELEASE_KEY_PASSWORD` | Signing key password |

`GOOGLE_SERVICES_JSON` and `FIREBASE_APP_DISTRIBUTION_CREDENTIALS` are plain JSON text, so paste
the file contents directly into the secret value box — no base64 needed. Only the keystore is
binary and needs encoding:
```bash
base64 -w0 path/to/keystore   # Linux
base64 -i path/to/keystore | tr -d '\n'   # macOS
```

The `FIREBASE_APP_DISTRIBUTION_CREDENTIALS` service account needs the **Firebase App
Distribution Admin** role (or equivalent) on the `ai-audio-book` Firebase project, since the
`app` module's `release` build type is configured to upload to the `devs` tester group via
`firebaseAppDistribution { serviceCredentialsFile = "app/ai-audio-book-2c2ff064ff10.json" }`.
