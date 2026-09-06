# MedTrack

> **MedTrack is a personal health companion for medication tracking,
> health-claim fact-checking, symptom logging, medication information,
> and personalized health guidance.**

MedTrack is an Android application built with **Kotlin + Jetpack
Compose**. It combines local patient health records with AI-assisted
features and external health-information sources to help users make more
informed decisions about everyday health information.

The app is designed around one principle:

**Give users useful health information while clearly separating
AI-generated guidance from verified evidence and professional medical
advice.**

------------------------------------------------------------------------

## Table of Contents

-   [Core Features](#core-features)
-   [How to Use the App](#how-to-use-the-app)
-   [Patient User Flow](#patient-user-flow)
-   [Feature Guide](#feature-guide)
-   [Fact Checker](#fact-checker)
-   [Medication Safety and Warnings](#medication-safety-and-warnings)
-   [Personalized AI Tips](#personalized-ai-tips)
-   [Health Assistant](#health-assistant)
-   [Clinician Features](#clinician-features)
-   [Languages and Accessibility](#languages-and-accessibility)
-   [Data and Architecture](#data-and-architecture)
-   [External Services](#external-services)
-   [Project Structure](#project-structure)
-   [Setup](#setup)
-   [Running the App](#running-the-app)
-   [Safety and Limitations](#safety-and-limitations)
-   [Testing](#testing)
-   [Future Improvements](#future-improvements)

------------------------------------------------------------------------

## Core Features

### 🧾 Fact Checker

The Fact Checker is the **default screen after patient login**.

Users can enter a health or medicine-related claim and receive a
structured result such as:

-   **True / Supported**
-   **False / Unsupported**
-   **Misleading**
-   **Unverified / Insufficient evidence**

The Fact Checker can use:

-   Curated Malaysian government health evidence
-   Previously verified claims stored in the local cache
-   Trusted health sources
-   Google Search grounding through Gemini when curated evidence is
    unavailable

The app also displays the evidence sources used for a result where
available.

### 💊 Medicine Intake Log

Users can create and maintain medication records including:

-   Medication name
-   Dosage
-   Frequency
-   Medication time
-   Medication type
-   Notes
-   Taken/not-taken status
-   Last taken date

Medication records are stored per patient in the local Room database.

### ⚠️ Medicine Allergy Tracking

Users can record medicines they are allergic to.

Allergy information becomes shared patient context and can be used by:

-   Fact Checker
-   Personalized AI Tips
-   Medication safety warnings

If a recorded allergy matches a medicine being checked or a medication
appearing in the patient's intake records, the app can display a
prominent allergy warning.

### 🩺 Symptoms Logging

Users can record:

-   Symptom category
-   Severity
-   Notes
-   Date
-   Time
-   Medicine allergies

Symptoms are stored as patient-specific records and can be used as
context for personalized AI guidance.

### 🤖 Personalized AI Tips / MedCoach

The MedCoach feature provides personalized informational tips using
available patient context, including:

-   Recent symptoms
-   Recorded medicines
-   Recorded medicine allergies
-   Drug information retrieved from openFDA where available

The goal is to provide context-aware health information rather than
generic advice.

### 🧑‍⚕️ Health Assistant

The Health Assistant provides general educational guidance based on:

-   Reported symptoms
-   Reported vital information
-   Current medication context

The feature is intentionally constrained to avoid providing specific
medication names or dosages as treatment recommendations.

### 📚 History

Users can review previous Fact Checker activity, including:

-   Original claim/input
-   AI response
-   Evidence/source information

This makes it easier to revisit previously checked health information.

### ⚙️ Settings

Users can manage:

-   Preferred language
-   Font size
-   Basic account/profile information
-   Age
-   Clinician access

Language and font-size preferences are persisted and applied across
relevant screens.

### 👨‍⚕️ Clinician Dashboard

A clinician-only workflow provides aggregate information such as:

-   Total patients
-   Average medications per patient
-   Most common symptom
-   Average symptom severity
-   Recent symptom severity trend

Clinicians can also request AI-assisted pattern finding from aggregate
statistics.

### 🔎 Doctor Review Queue

Patients can flag Fact Checker or Health Assistant outputs for clinician
review.

Clinicians can:

-   View flagged items
-   Read the original patient input
-   Review the AI output
-   Add an optional clinician note
-   Mark an item as reviewed
-   Mark an item as corrected

------------------------------------------------------------------------

## How to Use the App

### 1. Create an account

From the welcome screen:

1.  Select **Sign Up**.
2.  Enter your full name.
3.  Enter your phone number.
4.  Create a password.
5.  Confirm the password.
6.  Select your preferred language.
7.  Create the account.

The application validates required fields and password/phone-number
requirements.

### 2. Log in

Use your:

-   Patient ID
-   Password

After successful login, MedTrack opens directly on the **Fact Checker**.

### 3. Set up your health context

For the best personalized experience:

1.  Add your medications.
2.  Record your medication allergies.
3.  Log relevant symptoms.
4.  Keep your medication intake status up to date.
5.  Configure language and font-size preferences.

This information becomes shared patient context for relevant features.

### 4. Check a health claim

From **Fact-Check**:

1.  Enter a health or medicine-related claim.
2.  Select the response language if available.
3.  Submit the claim.
4.  Review the verdict.
5.  Read the explanation.
6.  Check the evidence/source information.
7.  If something appears incorrect or concerning, use the flag/review
    functionality where available.

### 5. Track medication intake

From the medication/home workflow:

1.  Add a medication.
2.  Enter its dosage, frequency, time, type, and notes.
3.  Mark medication intake as taken when appropriate.
4.  The medication record is saved to the patient's local database.

### 6. Record symptoms

From **Symptoms**:

1.  Select the symptom category.
2.  Enter severity.
3.  Add notes if needed.
4.  Select the date and time.
5.  Record medicine allergies if necessary.
6.  Save the entry.

The stored symptom history can contribute to personalized AI guidance.

### 7. Get personalized medication information

From **MedCoach**:

1.  Search for a medicine.
2.  Review available drug information.
3.  Review purpose, indications, warnings, and administration
    information where provided.
4.  Request a personalized tip using the patient's available context.

### 8. Ask the Health Assistant

From **Health Assistant**:

1.  Enter symptoms.
2.  Optionally provide vital information.
3.  Submit the request.
4.  Read the general educational guidance.
5.  Seek professional medical advice for diagnosis, treatment, severe
    symptoms, or worsening conditions.

------------------------------------------------------------------------

## Patient User Flow

``` text
                         ┌──────────────┐
                         │    Welcome   │
                         └──────┬───────┘
                                │
                    ┌───────────┴───────────┐
                    │                       │
                 Sign Up                  Login
                    │                       │
                    └───────────┬───────────┘
                                ▼
                       ┌────────────────┐
                       │   Fact Checker │
                       │  Default Home  │
                       └───────┬────────┘
                               │
          ┌────────────────────┼────────────────────┐
          │                    │                    │
          ▼                    ▼                    ▼
   Medicine Intake         Symptoms            Allergies
          │                    │                    │
          └────────────────────┼────────────────────┘
                               │
                  ┌────────────┴────────────┐
                  ▼                         ▼
          Personalized AI Tips        Fact Checker
                  │                         │
                  ▼                         ▼
          Context-aware guidance      Evidence + warnings
```

------------------------------------------------------------------------

# Feature Guide

## 1. Home

The Home screen provides a medication-oriented overview while the **Fact
Checker is the default post-login screen**.

The medication workflow remains available and is not removed when Fact
Checker becomes the default.

## 2. Fact Checker

The Fact Checker is intended for checking public health claims and
medicine-related information.

### Result workflow

``` text
User Claim
    │
    ▼
Check Local Verified Claim Cache
    │
    ├── Match found ──► Return cached verified result
    │
    └── No match
           │
           ▼
    Retrieve Curated Evidence
           │
      ┌────┴────┐
      │         │
 Evidence     No curated
 available    evidence
      │         │
      ▼         ▼
   Gemini    Gemini + Google
   using     Search grounding
   supplied  fallback
   evidence
      │         │
      └────┬────┘
           ▼
     Fact Check Result
           │
           ▼
   Save verified result
       to cache
```

### Curated Malaysian evidence

The project includes curated evidence for Malaysian health sources
because several relevant organisations provide web pages and documents
rather than a suitable public fact-checking API.

Examples include:

-   Ministry of Health Malaysia / KKM
-   National Pharmaceutical Regulatory Agency / NPRA
-   Malaysian Health Technology Assessment / MaHTAS
-   National Health and Morbidity Survey / NHMS

The app also contains curated source information from trusted
international health organisations such as WHO, CDC, FDA and other
health-information providers.

------------------------------------------------------------------------

# Medication Safety and Warnings

MedTrack separates different warning types so that potentially important
patient-specific information is not hidden by the normal fact-check
result.

### 🔴 Allergy Warning

Displayed when the checked/intake medicine matches a medicine recorded
as an allergy.

Example:

``` text
⚠️ Allergy Warning

This medicine matches a medicine you have recorded as an allergy.
```

### 🟠 Medication Conflict Warning

The intended architecture supports a separate medication-conflict
warning when a reliable interaction source confirms that a checked
medicine may conflict with a medication in the user's intake record.

**The current project does not invent drug-drug interaction rules.**

Because no verified pairwise medication-interaction source is configured
in the current implementation, the Fact Checker explicitly avoids making
unsupported interaction claims.

Instead, when the patient has recorded medications, the UI can
communicate that medication interaction checking is not enabled rather
than pretending that no interaction exists.

### Why this matters

Health and medication information is safety-sensitive. MedTrack
therefore follows these principles:

-   Do not invent interaction rules.
-   Do not hardcode unsupported medical claims.
-   Do not treat AI output as a diagnosis.
-   Clearly distinguish warnings from confirmed medical diagnoses.
-   Prefer verified evidence where available.

------------------------------------------------------------------------

# Personalized AI Tips

MedCoach uses a combined patient context rather than symptoms alone.

``` text
Patient Profile
      │
      ├── Symptoms
      ├── Medicine Intake
      ├── Medicine Allergies
      └── Other available patient context
                │
                ▼
        Personalized AI Tips
                │
                ▼
       Context-aware guidance
```

The AI prompt explicitly tells the model to consider recorded allergies
and avoid recommending something that conflicts with a recorded allergy.

------------------------------------------------------------------------

# Health Assistant

The Health Assistant is designed for general health education.

It can consider:

-   Symptoms
-   Vital information
-   Current medications as context

The assistant is instructed not to provide:

-   A definitive diagnosis
-   A treatment plan
-   Specific medication recommendations
-   Medication dosages

A second response-sanitization layer checks generated output for dosage
or drug-recommendation patterns and redacts them when detected.

------------------------------------------------------------------------

# Clinician Features

## Clinician Login

Clinician functionality is accessed through the clinician authentication
flow from Settings.

## Clinician Dashboard

The dashboard provides aggregate statistics from the local patient
database.

Current dashboard metrics include:

  -----------------------------------------------------------------------
  Metric                              Description
  ----------------------------------- -----------------------------------
  Total Patients                      Number of patients recorded

  Avg Medications / Patient           Average medication records per
                                      patient

  Most Common Symptom                 Most frequently recorded symptom
                                      category

  Avg Symptom Severity                Average recorded severity

  Recent Severity Trend               Recent severity values used for
                                      trend visualization
  -----------------------------------------------------------------------

The dashboard also includes an AI-assisted **Find Patterns** feature for
interpreting aggregate statistics.

## Doctor Review Queue

Flagged Fact Checker and Health Assistant outputs enter a clinician
review queue.

Review statuses include:

-   `PENDING`
-   `REVIEWED`
-   `CORRECTED`

A clinician can add a note when reviewing or correcting a flagged item.

------------------------------------------------------------------------

# Languages and Accessibility

## Supported Languages

The application UI supports:

-   🇬🇧 English
-   🇨🇳 Chinese
-   🇲🇾 Bahasa Malaysia

The patient's preferred language is stored with the patient profile.

Changing the language inside the app causes the UI to update without
requiring a second language/localization system.

The Fact Checker also supports selecting the language for the generated
fact-check response.

## Font Size

Users can select different font-size preferences from Settings.

The preference is persisted with the application's existing settings
architecture and applied across relevant screens.

------------------------------------------------------------------------

# Data and Architecture

MedTrack uses a local-first architecture for patient and application
data.

### Main technologies

-   **Kotlin**
-   **Jetpack Compose**
-   **Material 3**
-   **Android Navigation Compose**
-   **Room**
-   **Kotlin Coroutines / Flow**
-   **Retrofit**
-   **OkHttp**
-   **Gson**
-   **Coil**
-   **Google Gemini**

### Local database

Room stores patient and feature data including:

``` text
Patient
 ├── Preferred Language
 ├── Font Size
 ├── Medicine Allergies
 └── Profile information

Medication
 ├── Medication Name
 ├── Dosage
 ├── Frequency
 ├── Time
 ├── Type
 ├── Notes
 └── Taken Status

Symptoms
 ├── Category
 ├── Severity
 ├── Notes
 └── Date / Time

Fact Check History
 ├── Claim
 ├── Result
 ├── Response
 └── Sources

Verified Claim Cache
 └── Previously verified claims

Doctor Review Queue
 ├── Flagged input
 ├── AI output
 ├── Review status
 └── Clinician note
```

The patient is the shared source of truth connecting symptoms,
medication intake, allergies, preferences, and AI-assisted features.

------------------------------------------------------------------------

# External Services

## Google Gemini

Gemini is the primary AI service used by MedTrack.

### Fact Checker

The Fact Checker directly calls the Google Generative Language REST API:

``` text
https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent
```

The API key is read from the Android project's `local.properties` and
exposed to the app through `BuildConfig.GEMINI_API_KEY`.

When curated official evidence is available, the Fact Checker provides
that evidence to Gemini and instructs the model to base the verdict only
on the supplied evidence.

When curated evidence is unavailable, the Fact Checker can use Gemini's
Google Search grounding capability to retrieve web-grounded information.

### Other AI features

The Health Assistant and personalized AI features reuse the
application's Gemini-based AI repository with their own safety
constraints.

------------------------------------------------------------------------

## openFDA

MedCoach uses the public **openFDA drug label API** for medicine
information.

Base URL:

``` text
https://api.fda.gov/
```

The application searches drug-label information using the medicine's
brand name and uses the returned information for the
medication-information workflow.

**Important:** openFDA is used for drug-label information in MedCoach.
It is not the primary engine of the Fact Checker.

------------------------------------------------------------------------

## data.gov.my

The project contains a Retrofit client for the Malaysian government's
Data Catalogue API:

``` text
https://api.data.gov.my/
```

The client is designed to access verified datasets when a specific
dataset ID and schema have been confirmed.

No unverified nutrition/NCD dataset ID is hardcoded into the current
implementation.

------------------------------------------------------------------------

# Project Structure

``` text
MMU_hackathon/
│
├── app/
│   ├── src/main/
│   │   ├── java/com/colleen/s36349879/medtrack/
│   │   │
│   │   ├── Welcome.kt
│   │   ├── Login.kt
│   │   ├── SignUp.kt
│   │   ├── ClaimAccount.kt
│   │   │
│   │   ├── Home.kt
│   │   ├── FactCheck.kt
│   │   ├── HealthAssistant.kt
│   │   ├── Symptoms.kt
│   │   ├── MedCoachScreen.kt
│   │   ├── HistoryScreen.kt
│   │   ├── Settings.kt
│   │   ├── AddMedication.kt
│   │   │
│   │   ├── ClinicianLogin.kt
│   │   ├── ClinicianDashboard.kt
│   │   ├── DoctorReview.kt
│   │   │
│   │   ├── MedTrackBottomBar.kt
│   │   │
│   │   ├── data/
│   │   │   ├── MedTrackDatabase.kt
│   │   │   ├── AuthManager.kt
│   │   │   ├── patient/
│   │   │   ├── medication/
│   │   │   ├── symptom/
│   │   │   ├── factcheck/
│   │   │   ├── genAI/
│   │   │   ├── healthassistant/
│   │   │   ├── tip/
│   │   │   ├── doctorreview/
│   │   │   ├── clinician/
│   │   │   └── network/
│   │   │
│   │   └── ui/
│   │       ├── localization/
│   │       └── theme/
│   │
│   └── build.gradle.kts
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

------------------------------------------------------------------------

# Setup

## Requirements

Recommended development environment:

-   Android Studio
-   JDK 11
-   Android SDK with API 36
-   Android device or emulator
-   Internet connection for AI and external API features
-   A valid Google Gemini API key

The application currently targets:

``` text
minSdk = 26
targetSdk = 36
compileSdk = 36
Java = 11
```

## Configure the Gemini API key

Create or edit:

``` text
local.properties
```

and add:

``` properties
GEMINI_API_KEY=YOUR_GEMINI_API_KEY
```

Do **not** commit your real API key to Git.

The Gradle configuration reads the key and exposes it to the app as:

``` text
BuildConfig.GEMINI_API_KEY
```

------------------------------------------------------------------------

# Running the App

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  Allow Gradle to synchronize.
4.  Add your Gemini API key to `local.properties`.
5.  Connect an Android device or start an emulator.
6.  Run the `app` configuration.

From the command line, the usual Gradle commands are:

``` bash
./gradlew assembleDebug
```

Windows:

``` powershell
.\gradlew.bat assembleDebug
```

To run unit tests:

``` bash
./gradlew test
```

To run Android instrumented tests:

``` bash
./gradlew connectedAndroidTest
```

------------------------------------------------------------------------

# Safety and Limitations

MedTrack is a health-information and tracking application. It is **not a
replacement for a doctor, pharmacist, emergency service, or other
qualified healthcare professional**.

The application should not be used to:

-   Diagnose a medical condition
-   Replace professional medical advice
-   Decide whether to start, stop, or change prescribed medication
-   Determine medication dosage without professional guidance
-   Treat AI-generated information as a confirmed diagnosis

### Fact Checker limitation

A fact-check result is dependent on the evidence available to the
application and the AI service.

If evidence is insufficient, the application should prefer an
**Unverified / Insufficient Evidence** result rather than inventing an
answer.

### Medication interaction limitation

The current implementation intentionally does not invent drug-drug
interaction rules.

A future implementation should connect a verified medication-interaction
data source before presenting confirmed interaction warnings.

### AI limitation

AI-generated responses may be incorrect or incomplete. The clinician
review workflow exists so that flagged outputs can be reviewed
separately by a clinician.

------------------------------------------------------------------------

# Testing

Important functional scenarios for this project include:

### Authentication

-   Create a patient account.
-   Log in with valid credentials.
-   Reject invalid credentials.
-   Restore an existing patient session.

### Preferences

-   Select English, Chinese, and Bahasa Malaysia.
-   Log out and log back in.
-   Confirm the saved language is restored.
-   Change language while inside the application.
-   Confirm the UI updates immediately.
-   Change font size.
-   Confirm the preference persists.

### Fact Checker

-   Submit a normal health claim.
-   Submit an empty claim.
-   Check a claim with curated Malaysian evidence.
-   Check a claim without curated evidence.
-   Confirm evidence/source information is shown where available.
-   Confirm previously verified claims can use the local cache.
-   Confirm the Fact Checker is the default post-login screen.
-   Confirm the Fact Checker screen can scroll through long results.

### Medication and allergy context

-   Add a medication.
-   Mark a medication as taken.
-   Record a medicine allergy.
-   Check a medicine matching the recorded allergy.
-   Confirm the allergy warning is displayed.
-   Confirm personalized AI context includes symptoms, medicines, and
    allergies.
-   Confirm unsupported medication interaction claims are not invented.

### Health Assistant

-   Submit symptoms.
-   Submit symptoms with vitals.
-   Confirm general educational guidance is returned.
-   Confirm the assistant does not intentionally provide specific
    medication/dosage recommendations.

### Clinician workflow

-   Open clinician login.
-   View aggregate dashboard statistics.
-   Generate aggregate AI pattern insights.
-   Flag a Fact Checker result.
-   Flag a Health Assistant response.
-   Review a flagged item.
-   Mark an item as reviewed or corrected.
-   Add a clinician note.

------------------------------------------------------------------------

# Future Improvements

Potential future enhancements include:

-   Integration with a **verified medication-interaction database/API**
-   More comprehensive Malaysian medication data
-   Improved evidence ranking and citation presentation
-   Stronger automated testing coverage
-   Offline-friendly fact-check history and medication workflows
-   More detailed medication adherence analytics
-   Additional accessibility options
-   More clinician review and audit capabilities
-   Stronger privacy/security controls for production deployment

Any medication-interaction feature should use a verified medical source
rather than hardcoded assumptions.

------------------------------------------------------------------------

# Privacy and Security Notes

This project stores patient-related information locally using Room.

For a production deployment, additional security measures should be
considered, including:

-   Secure credential storage
-   Encryption for sensitive local health data
-   Secure API-key handling
-   Backend-mediated AI/API access rather than embedding long-lived API
    credentials in a client app
-   Authentication hardening
-   Access control between patient and clinician workflows
-   Audit logging for clinician actions
-   Appropriate consent and privacy policies

------------------------------------------------------------------------

# Hackathon Pitch

## The Problem

People increasingly encounter health and medicine information online,
but it can be difficult to determine what is trustworthy while also
keeping track of their own medication and symptoms.

## The Solution

**MedTrack connects personal health tracking with evidence-aware AI
assistance.**

Instead of treating every user as a generic question, MedTrack can bring
together:

``` text
Symptoms
   +
Medication Intake
   +
Medicine Allergies
   +
Health Claims
   ↓
Personalized + Evidence-Aware Health Information
```

## Why MedTrack?

-   **Fact-check health information**
-   **Track medication intake**
-   **Record symptoms**
-   **Remember medicine allergies**
-   **Provide personalized AI tips**
-   **Use evidence from trusted health sources**
-   **Support multilingual users**
-   **Provide clinician review of flagged AI outputs**

### One-line pitch

> **MedTrack helps people track what they take, understand what they
> read, and connect their health information through a safer,
> evidence-aware AI experience.**
