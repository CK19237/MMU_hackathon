# MedTrack

MedTrack is an Android medication tracking app built with Kotlin and Jetpack Compose.
It allows patients to log medications and symptoms, while giving clinicians an
aggregated dashboard to monitor trends across their patient population.

## Features
- Patient login, registration, and account claiming
- Medication logging, tracking, and searching
- Symptom logging with severity scoring and tracking
- Clinician dashboard with aggregate statistics and a symptom severity trend chart
- AI-powered MedCoach tips and clinician pattern insights

## AI Disclosure
This app uses the **Google Gemini API** to power two AI features:

- **MedCoach** — generates personalized medication tips and advice based on the
  patient's current medications and symptoms
- **Clinician Insights** — analyses aggregate patient data to identify potential
  clinical patterns and trends

The Gemini API key is used solely to make requests to Google's Gemini model.
No patient data is stored or retained by the API beyond the scope of each request.

## Tech Stack
- Kotlin & Jetpack Compose
- Room Database
- Google Gemini API

## How to Run
1. Clone the repository
2. Open in Android Studio
3. Add your Gemini API key to `local.properties`
    and name it as GEMINI_API_KEY (eg. GEMINI_API_KEY = geminiapikey)
4. Run on an emulator or physical device

## Disclaimer
This app is for tracking purposes only and does not replace professional medical advice.

## Author
Colleen Ker — Student ID: 36349879