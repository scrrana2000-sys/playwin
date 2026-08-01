# Google Sign-In Implementation Fix Plan

This implementation plan resolves the "Authentication Error: No credentials available" error during Google Sign-In by updating `GoogleAuthManager` to properly leverage Android Credential Manager with Google Identity Services. It ensures smooth account selection without throwing premature error dialogs when no saved/filtered credentials exist initially.

## User Review Required

> [!NOTE]
> No breaking changes or UI modifications are required. The user interface remains completely unchanged, but the background credential flow will seamlessly fall back to the interactive Google Account picker.

## Proposed Changes

### Auth Component

#### [MODIFY] [GoogleAuthManager.kt](file:///C:/Users/Rajputhana/StudioProjects/playwin/app/src/main/java/com/myplaywin/app/data/auth/GoogleAuthManager.kt)

- **Google Play Services Availability Verification**:
  Add `GoogleApiAvailability` check before attempting credential operations to ensure Google Play Services is available on the device.
- **Two-Stage Credential Request Strategy**:
  1. **First Attempt (Silent / Filtered)**: Request Google ID token with `.setFilterByAuthorizedAccounts(true)` and `.setAutoSelectEnabled(true)`.
  2. **Fallback Attempt (Interactive Picker)**: If a `NoCredentialException` or empty credential occurs (which happens when no prior authorized session is saved for silent sign-in), immediately retry with `.setFilterByAuthorizedAccounts(false)` and `.setAutoSelectEnabled(false)`. This directly launches the Google account chooser prompt.
- **Exception & Cancellation Handling**:
  - Catch `GetCredentialCancellationException` and return `GoogleSignInResult.Cancelled` (so no error popup is displayed when user closes/cancels the picker).
  - Explicitly handle `NoCredentialException` and other subclass instances of `GetCredentialException`.
  - Validate and parse `GoogleIdTokenCredential`.
- **Context Handling**:
  Use proper Activity context resolution or fallback for CredentialManager requests requiring UI focus when prompting the user.

---

### App Dependencies / Config Verification

#### [VERIFY] [build.gradle.kts](file:///C:/Users/Rajputhana/StudioProjects/playwin/app/build.gradle.kts)
#### [VERIFY] [google-services.json](file:///C:/Users/Rajputhana/StudioProjects/playwin/app/google-services.json)
#### [VERIFY] [strings.xml](file:///C:/Users/Rajputhana/StudioProjects/playwin/app/src/main/res/values/strings.xml)

- Confirm `androidx.credentials`, `androidx.credentials.play.services.auth`, and `googleid` dependencies are present (already verified).
- Confirm Web Client ID `228349425977-e2mjc70g7lp8qj4r8rkvm0cu46odohf8.apps.googleusercontent.com` in `strings.xml` matches client ID (type 3) in `google-services.json` (already verified).
- Confirm package name `com.myplaywin.app` matches `applicationId` (already verified).

## Verification Plan

### Automated Tests
- Run Gradle assemble to verify clean compilation:
  `./gradlew app:assembleDebug`

### Manual Verification
- Deploy and tap "Continue with Google" on device/emulator.
- Observe that if no previous authorized credentials are cached, the account picker dialog appears immediately without showing "No credentials available" error dialog.
