# Liana Accounting

A deliberately small native Android app that reads SMS messages locally on the device.

## Why Kotlin

Kotlin is Android's first-class native language. This project avoids Flutter, React Native, Compose, databases, analytics SDKs, and third-party UI libraries. The result is a simpler app with fewer dependencies and less runtime overhead.

## Current features

- Requests `READ_SMS` only when the user taps the permission button
- Reads the newest 250 messages from the SMS inbox
- Displays sender, date, time, and message body
- Refreshes on demand
- Performs no network requests
- Disables Android backup to reduce accidental SMS-derived data exposure
- Enables release code and resource shrinking

## Open in Android Studio

1. Clone the repository.
2. Open the repository root in Android Studio.
3. Let Android Studio sync Gradle.
4. Run the `app` configuration on a physical Android phone.
5. Grant SMS permission when prompted.

A physical phone with SMS messages is recommended; most emulators have an empty inbox.

## Build requirements

- Android Studio with JDK 17
- Android SDK 35
- Minimum Android version: Android 6.0 (API 23)

## Privacy

The current app reads SMS through Android's local content provider. It does not contain internet permission or upload messages anywhere.

## Google Play warning

Google Play treats SMS permissions as restricted. SMS-based money-management apps may qualify for an exception, but publication requires a Permissions Declaration Form, a clear privacy policy, prominent disclosure and consent, and Google Play approval. Installing the APK privately does not remove Android's runtime permission requirement.

## Next accounting milestone

The next useful version should detect bank and payment messages, extract amount and merchant data on-device, and store only normalized transactions rather than copying the full SMS archive.
