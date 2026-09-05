package com.colleen.s36349879.medtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.colleen.s36349879.medtrack.data.clinician.ClinicianPasswordViewModel
import com.colleen.s36349879.medtrack.data.genAI.GenAIViewModel
import com.colleen.s36349879.medtrack.data.medication.MedicationViewModel
import com.colleen.s36349879.medtrack.data.patient.PatientViewModel
import com.colleen.s36349879.medtrack.data.symptom.SymptomViewModel
import com.colleen.s36349879.medtrack.ui.theme.MedtrackTheme
import com.colleen.s36349879.medtrack.data.isDatabaseSeeded
import com.colleen.s36349879.medtrack.data.setDatabaseSeeded
import com.colleen.s36349879.medtrack.data.tip.TipViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // ViewModels hold and manage the app's data  "by viewModels" is a Kotlin shortcut
    // that creates the ViewModel and keeps it alive when the screen rotates
    private val patientViewModel: PatientViewModel by viewModels {
        PatientViewModel.PatientViewModelFactory(
            this // this refers to the current Activity
        )
    }
    private val medicationViewModel: MedicationViewModel by viewModels {
        MedicationViewModel.MedicationViewModelFactory(
            this
        )
    }
    private val symptomViewModel: SymptomViewModel by viewModels {
        SymptomViewModel.SymptomViewModelFactory(
            this
        )
    }

    private val genAiViewModel: GenAIViewModel by viewModels {
        GenAIViewModel.GenAiViewModelFactory(
            this
        )
    }

    private val tipViewModel: TipViewModel by viewModels {
       TipViewModel.TipViewModelFactory(
            this
        )
    }

    private val clinicianPasswordViewModel: ClinicianPasswordViewModel by viewModels {
        ClinicianPasswordViewModel.ClinicianPasswordViewModelFactory(
            this
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check if a user was already logged in from a previous session and restore it
        patientViewModel.loadSession(applicationContext)


        // Only seed the database if it hasn't been done before
        // isDatabaseSeeded checks a saved flag so we don't add duplicate data every time the app opens
        if (!isDatabaseSeeded(this)) {
            lifecycleScope.launch {

                patientViewModel.seedPatient(this@MainActivity)
                medicationViewModel.seedMedication(this@MainActivity)
                symptomViewModel.seedSymptom(this@MainActivity)

                // Mark as done so it never runs again
                setDatabaseSeeded(this@MainActivity)
            }
        }

        setContent {
            MedtrackTheme() {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MyNavHost(
                        patientViewModel = patientViewModel,
                        medicationViewModel = medicationViewModel,
                        symptomViewModel = symptomViewModel,
                        genAiViewModel = genAiViewModel,
                        tipViewModel = tipViewModel,
                        clinicianPasswordViewModel = clinicianPasswordViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

// Navigation Host
@Composable
fun MyNavHost(
    // We pass in all ViewModels so any screen can access the data it needs
    patientViewModel: PatientViewModel,
    medicationViewModel: MedicationViewModel,
    symptomViewModel: SymptomViewModel,
    genAiViewModel: GenAIViewModel,
    tipViewModel: TipViewModel,
    clinicianPasswordViewModel: ClinicianPasswordViewModel,
    modifier: Modifier = Modifier
){
    val navController = rememberNavController()

    // If someone is logged in, go to home, if not start at welcome
    val startDestination = if (patientViewModel.isUserLoggedIn()) "home" else "welcome"

    // Define all navigation destinations
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ){
        composable("welcome")  { WelcomeScreen(navController) }
        composable("login")    { LoginScreen(navController, patientViewModel) }
        composable("claim_account") { ClaimAccountScreen(navController, patientViewModel) }
        composable("signup")   { SignUpScreen(navController, patientViewModel) }
        composable("home")     { HomeScreen(navController, patientViewModel, medicationViewModel) }
        composable("symptoms") { SymptomsScreen(navController, symptomViewModel, patientViewModel) }
        composable("add_medication"){ AddMedicationScreen(navController, medicationViewModel, patientViewModel) }
        composable("settings") {SettingsScreen(navController, patientViewModel)}
        composable("med_coach") {MedCoachScreen(
            navController, medicationViewModel, genAiViewModel, tipViewModel, symptomViewModel, patientViewModel)
        }
        composable("clinician_login") {ClinicianLoginScreen(navController, clinicianPasswordViewModel)}
        composable("clinician_dashboard") {ClinicianDashboardScreen(
            navController,patientViewModel, medicationViewModel, symptomViewModel, clinicianPasswordViewModel, genAiViewModel)
        }
    }
}

//Welcome screen
@Composable
fun WelcomeScreen(navController: NavHostController) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        // App logo
        Image(painter = painterResource(id = R.drawable.logo),
            contentDescription = "MedTrack Logo",
            modifier = Modifier.size(200.dp)
        )

        // App name
        Text(
            text = "MedTrack",
            style = TextStyle(
                fontSize = 50.sp,
                color = colorResource(R.color.LightBlue),
                fontWeight = FontWeight.Bold
            )
        )

        Spacer (modifier = Modifier.height(150.dp))

        //Login button
        Button(onClick = {
            navController.navigate("login")
        },
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.LightBlue),
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ){ Text(text = "Login",
            fontSize = 20.sp) }

        //Claim Account Button (CSV users)
        OutlinedButton(
            onClick = { navController.navigate("claim_account") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
        ) { Text("Existing Patient? Claim Account", fontSize = 16.sp) }

        // Go to signup
        TextButton(onClick = {
            navController.navigate("signup")
        }) {
            Text("Don't have an account? Sign Up")
        }

        // Hyperlink to website
        HyperlinkText()
    }

    // Footer for warning, student name, and student ID
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(50.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(
            text = ("*This app is for tracking purposes only and does not replace professional medical advice*"),
            color = Color.Red,
            fontStyle = FontStyle.Italic
        )
        Text("Colleen Ker (36349879)")
    }

}

// Displays a clickable link that opens a website
@Composable
fun HyperlinkText(){
    Text(
        buildAnnotatedString {
            withLink(
                LinkAnnotation.Url(
                    "https://www.monash.edu/health/medical-centre",
                    // Styled to look like a normal link
                    TextLinkStyles(
                        style = SpanStyle(
                            color = Color.Blue,
                            textDecoration = TextDecoration.Underline))
                )
            ){
                append("Visit Monash Health Clinic Here")
            }
        }
    )
}