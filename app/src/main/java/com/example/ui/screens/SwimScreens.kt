package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Appointment
import com.example.data.Workout
import com.example.data.WorkoutSessionLog
import com.example.ui.WorkoutViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Helper function to format seconds as MM:SS or HH:MM:SS
fun formatDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

// Helper to format pace as MM'SS"
fun formatPace(secondsPer100m: Int): String {
    val minutes = secondsPer100m / 60
    val seconds = secondsPer100m % 60
    return String.format("%d'%02d\"", minutes, seconds)
}

// Helper to format timestamp
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwimAppNavigationContainer(viewModel: WorkoutViewModel) {
    val activeWorkout by viewModel.activeWorkout.collectAsState()
    val workouts by viewModel.filteredWorkouts.collectAsState()
    val selectedWorkout by viewModel.selectedWorkout.collectAsState()
    
    // Bottom navigation index: 0 = Home, 1 = Workouts, 2 = Assessoria, 3 = Dashboard / Profile
    var currentTab by remember { mutableStateOf(0) }
    
    // Navigation stack inside tabs
    // For Home Tab (index 0): 0 = Home Main, 1 = Prepare Screen
    var homeNavigationState by remember { mutableStateOf(0) }

    // If a workout is active, force render the active workout screen on top
    if (activeWorkout != null) {
        ActiveWorkoutScreen(viewModel = viewModel)
    } else {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { 
                            currentTab = 0 
                            homeNavigationState = 0 // Reset sub-navigation when switching tabs
                        },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                        label = { Text("Início", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OceanPrimary,
                            selectedTextColor = OceanPrimary,
                            indicatorColor = OceanSecondaryContainer
                        ),
                        modifier = Modifier.testTag("tab_home")
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = { Icon(Icons.Default.List, contentDescription = "Treinos") },
                        label = { Text("Treinos", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OceanPrimary,
                            selectedTextColor = OceanPrimary,
                            indicatorColor = OceanSecondaryContainer
                        ),
                        modifier = Modifier.testTag("tab_workouts")
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Assessoria") },
                        label = { Text("Assessoria", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OceanPrimary,
                            selectedTextColor = OceanPrimary,
                            indicatorColor = OceanSecondaryContainer
                        ),
                        modifier = Modifier.testTag("tab_coaching")
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Painel") },
                        label = { Text("Painel", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OceanPrimary,
                            selectedTextColor = OceanPrimary,
                            indicatorColor = OceanSecondaryContainer
                        ),
                        modifier = Modifier.testTag("tab_dashboard")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> {
                        if (homeNavigationState == 1 && selectedWorkout != null) {
                            PrepareScreen(
                                workout = selectedWorkout!!,
                                onBack = { homeNavigationState = 0 },
                                onStartWorkout = { workout ->
                                    viewModel.startWorkout(workout)
                                }
                            )
                        } else {
                            HomeScreen(
                                viewModel = viewModel,
                                onWorkoutClick = { workout ->
                                    viewModel.selectWorkout(workout)
                                    homeNavigationState = 1
                                }
                            )
                        }
                    }
                    1 -> {
                        WorkoutsListTab(
                            viewModel = viewModel,
                            onWorkoutClick = { workout ->
                                viewModel.selectWorkout(workout)
                                currentTab = 0
                                homeNavigationState = 1
                            }
                        )
                    }
                    2 -> {
                        AssessoriaTab(viewModel = viewModel)
                    }
                    3 -> {
                        DashboardScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

// 1. HOME SCREEN (Main view with hero, horizontal calendar, info metrics)
@Composable
fun HomeScreen(
    viewModel: WorkoutViewModel,
    onWorkoutClick: (Workout) -> Unit
) {
    val level by viewModel.selectedLevel.collectAsState()
    val workouts by viewModel.filteredWorkouts.collectAsState()
    val defaultWorkout = workouts.firstOrNull() ?: workouts.getOrNull(0)
    val appointments by viewModel.allAppointments.collectAsState()
    val weeklyGoal by viewModel.weeklyDistanceGoal.collectAsState()
    val weeklyCompleted by viewModel.weeklyDistanceCompleted.collectAsState()
    var showEditGoalDialog by remember { mutableStateOf(false) }
    var goalInputString by remember { mutableStateOf("") }

    // Find the next appointment in the next 24 hours
    val upcomingAppointment = remember(appointments) {
        val now = System.currentTimeMillis()
        val twentyFourHoursFromNow = now + 24 * 60 * 60 * 1000L
        
        appointments
            .filter { it.status == "Agendado" }
            .mapNotNull { app ->
                try {
                    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val date = format.parse("${app.dateString} ${app.timeSlot}")
                    if (date != null) {
                        app to date.time
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            .filter { (_, time) -> time in now..twentyFourHoursFromNow }
            .minByOrNull { (_, time) -> time }
            ?.first
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Brand Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Water,
                    contentDescription = "Waves",
                    tint = OceanPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BT Acqua",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary,
                    letterSpacing = (-0.5).sp
                )
            }
            IconButton(
                onClick = { /* Notificações */ },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notificações",
                    tint = AquaticOnSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Upcoming Workout Alert System
        upcomingAppointment?.let { appointment ->
            val displayTime = remember(appointment) {
                try {
                    val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val appDate = sdfInput.parse(appointment.dateString)
                    
                    val today = Calendar.getInstance()
                    val todayStr = sdfInput.format(today.time)
                    
                    val tomorrow = Calendar.getInstance()
                    tomorrow.add(Calendar.DAY_OF_YEAR, 1)
                    val tomorrowStr = sdfInput.format(tomorrow.time)
                    
                    when (appointment.dateString) {
                        todayStr -> "Hoje às ${appointment.timeSlot}"
                        tomorrowStr -> "Amanhã às ${appointment.timeSlot}"
                        else -> {
                            val sdfOutput = SimpleDateFormat("dd/MM", Locale.getDefault())
                            if (appDate != null) {
                                "${sdfOutput.format(appDate)} às ${appointment.timeSlot}"
                            } else {
                                "${appointment.dateString} ${appointment.timeSlot}"
                            }
                        }
                    }
                } catch (e: Exception) {
                    "${appointment.dateString} ${appointment.timeSlot}"
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .testTag("upcoming_appointment_alert"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDF2FE)), // Light marine blue
                border = BorderStroke(1.5.dp, OceanPrimary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(OceanPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = OceanPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(OceanPrimary, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PRÓXIMO TREINO",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "EM BREVE",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = appointment.workoutTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = "Com Coach ${appointment.trainerName} (${appointment.trainerSpecialty})",
                            fontSize = 12.sp,
                            color = AquaticOnSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Data",
                                tint = OceanPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = displayTime,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                        }
                    }
                }
            }
        }

        // Weekly Distance Goal Card Component
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .testTag("weekly_goal_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header with Title and Edit button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(OceanPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Flag,
                                contentDescription = "Meta",
                                tint = OceanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Meta Semanal",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary
                            )
                            Text(
                                text = "Distância acumulada nesta semana",
                                fontSize = 11.sp,
                                color = AquaticOnSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            goalInputString = weeklyGoal.toString()
                            showEditGoalDialog = true
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("edit_weekly_goal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar meta",
                            tint = OceanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Progress Info Row
                val progressPercent = if (weeklyGoal > 0) {
                    (weeklyCompleted.toFloat() / weeklyGoal.toFloat() * 100).toInt()
                } else 0
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = String.format("%,d", weeklyCompleted) + "m",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                color = OceanPrimary
                            )
                            Text(
                                text = "de ${String.format("%,d", weeklyGoal)}m",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = AquaticOnSurfaceVariant,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }
                    }

                    Text(
                        text = "$progressPercent%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (progressPercent >= 100) Color(0xFF2E7D32) else OceanPrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Progress Bar with animated transition
                val progressFraction = if (weeklyGoal > 0) {
                    (weeklyCompleted.toFloat() / weeklyGoal.toFloat()).coerceIn(0.0f, 1.0f)
                } else 0.0f

                LinearProgressIndicator(
                    progress = progressFraction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .testTag("weekly_goal_progress_bar"),
                    color = if (progressFraction >= 1.0f) Color(0xFF2E7D32) else OceanPrimary,
                    trackColor = OceanPrimary.copy(alpha = 0.1f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Incentive Message / Footer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = if (progressFraction >= 1.0f) Icons.Default.EmojiEvents else Icons.Default.AutoAwesome,
                        contentDescription = "Status",
                        tint = if (progressFraction >= 1.0f) Color(0xFFFBC02D) else OceanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (progressFraction >= 1.0f) {
                            "Parabéns! Você alcançou sua meta semanal! 🎉🏊‍♂️"
                        } else {
                            val remaining = (weeklyGoal - weeklyCompleted).coerceAtLeast(0)
                            "Faltam apenas ${String.format("%,d", remaining)}m para atingir o seu objetivo!"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (progressFraction >= 1.0f) Color(0xFF2E7D32) else AquaticOnSurfaceVariant
                    )
                }
            }
        }

        // Edit Weekly Goal Dialog
        if (showEditGoalDialog) {
            AlertDialog(
                onDismissRequest = { showEditGoalDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = OceanPrimary
                        )
                        Text(
                            text = "Meta de Distância Semanal",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Defina sua meta de distância em metros para nadar durante esta semana. O progresso será calculado com base em seus treinos realizados.",
                            fontSize = 13.sp,
                            color = AquaticOnSurfaceVariant
                        )

                        OutlinedTextField(
                            value = goalInputString,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.all { it.isDigit() }) {
                                    goalInputString = input
                                }
                            },
                            label = { Text("Distância da Meta (metros)") },
                            singleLine = true,
                            suffix = { Text("m") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OceanPrimary,
                                focusedLabelColor = OceanPrimary,
                                cursorColor = OceanPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("goal_input_field")
                        )

                        // Quick Selectors
                        Text(
                            text = "Sugestões Rápidas:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaticOnSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(2000, 4000, 6000, 10000).forEach { quickMeters ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(
                                            width = 1.dp,
                                            color = if (goalInputString == quickMeters.toString()) OceanPrimary else Color(0xFFE0E0E0),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (goalInputString == quickMeters.toString()) OceanPrimary.copy(alpha = 0.1f) else Color.Transparent
                                        )
                                        .clickable {
                                            goalInputString = quickMeters.toString()
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${quickMeters / 1000}k",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (goalInputString == quickMeters.toString()) OceanPrimary else AquaticOnSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newGoal = goalInputString.toIntOrNull() ?: 5000
                            viewModel.updateWeeklyDistanceGoal(newGoal)
                            showEditGoalDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                        modifier = Modifier.testTag("confirm_goal_button")
                    ) {
                        Text("Salvar Meta")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showEditGoalDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = OceanPrimary)
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }

        // Horizontal Calendar Segment
        Text(
            text = "Junho 2026",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AquaticOnSurfaceVariant,
            modifier = Modifier.padding(start = 2.dp, bottom = 12.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val days = listOf(
                "SEG" to "12",
                "TER" to "13",
                "QUA" to "14",
                "QUI" to "15",
                "SEX" to "16",
                "SAB" to "17"
            )
            days.forEach { (weekday, dayNum) ->
                val isSelected = dayNum == "13" // Terça 13 active from HTML
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) OceanPrimary else MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else AquaticOutlineVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = weekday,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else AquaticOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = dayNum,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else AquaticOnSurface
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }
                }
            }
        }

        // Swimmer Hero Card (using the first HTML image link)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable { defaultWorkout?.let { onWorkoutClick(it) } },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background Image
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCOchFTYPVBT9O8o6GMqKUODxz06tFXbuQASeMgwdR8frsl5RT-0vYqjBBG0oDKCDCpXismLcagWDqJWnqfB3gI9T3r9zyQL3Ju63HlefpEgbgPHZsZ0_uuw4tlSVa91Kecx7gxP_N139AL5kny-uxLM-jsQH1B24CZlvpEIMLmoZry__wtSZmxzC7FlF89c4QbHppSZFzXIPf9qZf55uILKk1vHjIoNmFSSXIpNJV4B1-7XIatI9pXutS4QuB8hNntT0xZ3PXB0cb0",
                    contentDescription = "Swimmer mid-stroke",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                startY = 150f
                            )
                        )
                )
                // Text Overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = defaultWorkout?.title ?: "Treino Performance",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = defaultWorkout?.description ?: "Série técnica de resistência",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Info Section (Hoje Metrics)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Straighten,
                        contentDescription = "Hoje",
                        tint = OceanPrimary
                    )
                    Column {
                        Text(
                            text = "Hoje",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AquaticOnSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${defaultWorkout?.totalDistanceMeters ?: 2500}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaticOnSurface
                            )
                            Text(
                                text = "m",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaticOnSurface,
                                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Tempo Est.",
                        tint = OceanPrimary
                    )
                    Column {
                        Text(
                            text = "Tempo Est.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = AquaticOnSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${defaultWorkout?.estimatedDurationMinutes ?: 55}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaticOnSurface
                            )
                            Text(
                                text = "min",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AquaticOnSurface,
                                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Level Toggle pill selector
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = OceanSecondaryContainer.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val isInter = level == "Intermediário"
                Button(
                    onClick = { viewModel.selectLevel("Intermediário") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isInter) Color.White else Color.Transparent,
                        contentColor = if (isInter) OceanPrimary else OceanOnSecondaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("level_intermediario"),
                    elevation = if (isInter) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Intermediário", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = { viewModel.selectLevel("Avançado") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isInter) Color.White else Color.Transparent,
                        contentColor = if (!isInter) OceanPrimary else OceanOnSecondaryContainer
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("level_avancado"),
                    elevation = if (!isInter) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Avançado", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // CTA Play button (Iniciar Treino)
        Button(
            onClick = { defaultWorkout?.let { onWorkoutClick(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("start_workout_button"),
            colors = ButtonDefaults.buttonColors(containerColor = OceanPrimaryContainer),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Iniciar Treino",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// 4. PREPARE SCREEN (Prepare-se: distance metrics, horizontal equipments bento grid, workout structure list)
@Composable
fun PrepareScreen(
    workout: Workout,
    onBack: () -> Unit,
    onStartWorkout: (Workout) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Image Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDkcKTVjFrmW8KlHfRSMHe2ZKKZCLrhwkvXAndXlBCbVpxnnE-d0SQBQDTQiY03WkIMIv62efApFkFTPxeiFNH9MDmksimtxBtPEI-HJ-4Hrr76ZfTg5TkUM1j-pWuB7Sd0yJT_7YbDTQx2LkTv3m3RkhHVt_UDFgrdYNV3YyqTCXTieDnoKXX2VTqKOQFUWLTGvJ5qtcNK0CINdp2pby7CAcHXLDEWvGQvxtScQZ5WLir6IvAsPn5hW_IntZ0pScEXbGYFQW9pvHji",
                contentDescription = "Swimmer mid-stroke prepare background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.3f), AquaticBackground),
                            startY = 0f
                        )
                    )
            )
            // Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 16.dp, top = 16.dp)
                    .align(Alignment.TopStart)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Voltar",
                    tint = OceanPrimary
                )
            }
            // Title Header overlay
            Text(
                text = "Prepare-se",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = OceanPrimaryContainer,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 12.dp)
            )
        }

        // Metrics Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DISTÂNCIA TOTAL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AquaticOnSurfaceVariant,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${workout.totalDistanceMeters}",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary
                    )
                    Text(
                        text = "m",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary,
                        modifier = Modifier.padding(bottom = 6.dp, start = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(OceanSecondaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = OceanPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${workout.estimatedDurationMinutes} min",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OceanPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(OceanSecondaryContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                tint = OceanPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${workout.kcal} kcal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OceanPrimary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Required Equipments Bento Grid
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Equipamentos Necessários",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AquaticOnSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val equipmentList = workout.equipment.split(",")
                val defaultEquips = listOf("Palmar", "Pull Buoy", "Nadadeira")
                
                defaultEquips.forEach { equip ->
                    val hasIt = equipmentList.any { it.trim().equals(equip, ignoreCase = true) }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (hasIt) OceanSecondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (equip) {
                                    "Palmar" -> Icons.Default.SportsHandball
                                    "Pull Buoy" -> Icons.Default.WaterDrop
                                    else -> Icons.Default.Pool
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = equip,
                                    tint = if (hasIt) OceanPrimary else AquaticOutline,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = equip,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (hasIt) AquaticOnSurface else AquaticOutline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Workout structure outline list
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "Estrutura do Treino",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AquaticOnSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val stages = workout.phases.split("||")
                stages.forEach { stage ->
                    val parts = stage.split("|")
                    if (parts.size >= 2) {
                        val phaseName = parts[0]
                        val phaseDetails = parts[1]
                        
                        // Percentage simulation based on phaseName
                        val percentage = when {
                            phaseName.contains("Aquecimento", ignoreCase = true) -> "15%"
                            phaseName.contains("Principal", ignoreCase = true) -> "70%"
                            phaseName.contains("Preparatória", ignoreCase = true) -> "10%"
                            else -> "15%"
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(36.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(OceanPrimary)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = phaseName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AquaticOnSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = phaseDetails,
                                        fontSize = 13.sp,
                                        color = AquaticOnSurfaceVariant
                                    )
                                }
                                Text(
                                    text = percentage,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Start button CTA
        Button(
            onClick = { onStartWorkout(workout) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(60.dp)
                .testTag("iniciar_treino_real_button"),
            colors = ButtonDefaults.buttonColors(containerColor = OceanPrimaryContainer),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Iniciar Treino",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// 2. ACTIVE WORKOUT SCREEN (Timer, list of phases with Completed, Active, Upcoming styling)
@Composable
fun ActiveWorkoutScreen(viewModel: WorkoutViewModel) {
    val workout by viewModel.activeWorkout.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val currentPhaseIndex by viewModel.currentPhaseIndex.collectAsState()

    if (workout == null) return

    val phasesList = workout!!.phases.split("||")
    val totalPhases = phasesList.size

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Water,
                        contentDescription = "Waves",
                        tint = OceanPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "BT Acqua",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary,
                        letterSpacing = (-0.5).sp
                    )
                }
                IconButton(
                    onClick = { viewModel.cancelActiveWorkout() },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Fechar treino",
                        tint = AquaticOnSurface
                    )
                }
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { viewModel.pauseOrResumeTimer() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("pause_workout_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanSecondaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isTimerRunning) "Pausar" else "Retomar",
                            tint = OceanPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isTimerRunning) "Pausar" else "Retomar",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary
                        )
                    }
                }

                Button(
                    onClick = { viewModel.advancePhase() },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .testTag("next_set_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (currentPhaseIndex == totalPhases - 1) "Finalizar" else "Próximo Set",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowForward,
                            contentDescription = "Avançar",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Timer Segment
            Text(
                text = "TEMPO DE TREINO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AquaticOnSurfaceVariant,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatDuration(elapsedSeconds),
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = OceanPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Linear Progress Bar
            val overallProgress = (currentPhaseIndex.toFloat() / totalPhases.toFloat()).coerceAtLeast(0.05f)
            LinearProgressIndicator(
                progress = { overallProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = OceanPrimary,
                trackColor = OceanSecondaryContainer
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(workout!!.totalDistanceMeters * overallProgress).toInt()}m",
                    fontSize = 12.sp,
                    color = AquaticOnSurfaceVariant
                )
                Text(
                    text = "Progresso: ${(overallProgress * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Timeline execution
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                // Vertical Timeline Path Line
                Box(
                    modifier = Modifier
                        .padding(start = 23.dp, top = 20.dp, bottom = 20.dp)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(AquaticOutlineVariant.copy(alpha = 0.5f))
                        .align(Alignment.CenterStart)
                )

                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    phasesList.forEachIndexed { index, stage ->
                        val parts = stage.split("|")
                        if (parts.size >= 2) {
                            val phaseName = parts[0]
                            val phaseDetails = parts[1]

                            val isCompleted = index < currentPhaseIndex
                            val isActive = index == currentPhaseIndex
                            val isPending = index > currentPhaseIndex

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                // Timeline bullet icon
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isCompleted -> OceanSecondaryContainer
                                                isActive -> OceanPrimary
                                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            }
                                        )
                                        .border(
                                            width = if (isActive) 4.dp else 0.dp,
                                            color = if (isActive) OceanOnPrimaryContainer else Color.Transparent,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Completado",
                                            tint = OceanPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Icon(
                                            imageVector = when {
                                                phaseName.contains("Aquecimento", ignoreCase = true) -> Icons.Default.Waves
                                                phaseName.contains("Final", ignoreCase = true) -> Icons.Default.Flag
                                                else -> Icons.Default.Pool
                                            },
                                            contentDescription = null,
                                            tint = if (isActive) Color.White else AquaticOutline,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Content Card
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .animateContentSize(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) Color.White else Color.Transparent
                                    ),
                                    border = if (isActive) BorderStroke(1.dp, OceanSecondaryContainer) else null,
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(
                                            horizontal = if (isActive) 16.dp else 4.dp,
                                            vertical = if (isActive) 12.dp else 2.dp
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = phaseName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isActive) OceanPrimary else if (isCompleted) AquaticOnSurfaceVariant.copy(alpha = 0.6f) else AquaticOnSurfaceVariant,
                                                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                                            )
                                            if (isActive) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(OceanPrimaryContainer)
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = "Ativo",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = phaseDetails,
                                            fontSize = 13.sp,
                                            fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                                            color = if (isCompleted) AquaticOnSurfaceVariant.copy(alpha = 0.5f) else AquaticOnSurface,
                                            textDecoration = if (isCompleted) TextDecoration.LineThrough else null
                                        )

                                        // If active, show additional sub-details or parameters
                                        if (isActive) {
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Divider(color = AquaticOutlineVariant.copy(alpha = 0.5f))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Timer,
                                                        contentDescription = null,
                                                        tint = OceanSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Ritmo Ideal",
                                                        fontSize = 12.sp,
                                                        color = AquaticOnSurfaceVariant
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Speed,
                                                        contentDescription = null,
                                                        tint = OceanSecondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Zona Aeróbica Z3/Z4",
                                                        fontSize = 12.sp,
                                                        color = AquaticOnSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Atmospheric Card/Gradient at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(OceanPrimary.copy(alpha = 0.15f), OceanPrimaryContainer.copy(alpha = 0.3f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Pool,
                        contentDescription = null,
                        tint = OceanPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Mantenha a técnica perfeita e foco na respiração na fase principal.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = OceanPrimary,
                        maxLines = 2
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// 3. PERFORMANCE DASHBOARD (Completed circular ring, bento details grid, list of historical logs, weekly highlights card)
@Composable
fun DashboardScreen(viewModel: WorkoutViewModel) {
    val logs by viewModel.allSessionLogs.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Sync state for loading feedback
    var isSyncing by remember { mutableStateOf(false) }

    // Aggregate statistics
    val totalWorkouts = logs.size
    val totalDistance = logs.sumOf { it.distanceCompleted }
    val totalDurationSeconds = logs.sumOf { it.durationSeconds }
    val averageHeartRate = if (logs.isNotEmpty()) logs.map { it.avgHeartRate }.average().toInt() else 0
    val averagePace = if (logs.isNotEmpty()) logs.map { it.avgPaceSecondsPer100m }.average().toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Water,
                    contentDescription = "Waves",
                    tint = OceanPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BT Acqua",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary,
                    letterSpacing = (-0.5).sp
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { /* Notificações */ },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notificações",
                        tint = AquaticOnSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OceanPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calculate the last 7 calendar days
        val last7Days = remember(logs) {
            (0..6).map { i ->
                Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, -(6 - i))
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
            }
        }

        val dayLabels = remember(last7Days) {
            last7Days.map { dayCal ->
                val sdf = SimpleDateFormat("EEE", Locale("pt", "BR"))
                val label = sdf.format(dayCal.time).uppercase()
                if (label.endsWith(".")) label.dropLast(1) else label
            }
        }

        val dailyDistances = remember(logs, last7Days) {
            FloatArray(7) { index ->
                val dayCal = last7Days[index]
                val dayStart = dayCal.timeInMillis
                val dayEnd = dayStart + 24 * 60 * 60 * 1000L
                logs.filter { it.timestamp in dayStart until dayEnd }.sumOf { it.distanceCompleted }.toFloat()
            }
        }

        val dailyDurationsMinutes = remember(logs, last7Days) {
            FloatArray(7) { index ->
                val dayCal = last7Days[index]
                val dayStart = dayCal.timeInMillis
                val dayEnd = dayStart + 24 * 60 * 60 * 1000L
                logs.filter { it.timestamp in dayStart until dayEnd }.sumOf { it.durationSeconds } / 60f
            }
        }

        val weeklyDistanceGoal = 10000 // 10.000m
        val weeklyDurationGoalMinutes = 180 // 3 horas (180 min)

        val weeklyDistanceTotal = dailyDistances.sum().toInt()
        val weeklyDurationTotalMinutes = dailyDurationsMinutes.sum().toInt()

        val weeklyDistanceProgress = if (weeklyDistanceGoal > 0) (weeklyDistanceTotal.toFloat() / weeklyDistanceGoal).coerceIn(0f, 1f) else 0f
        val weeklyDurationProgress = if (weeklyDurationGoalMinutes > 0) (weeklyDurationTotalMinutes.toFloat() / weeklyDurationGoalMinutes).coerceIn(0f, 1f) else 0f

        var selectedChartTab by remember { mutableStateOf(0) } // 0 = Distância, 1 = Tempo
        var selectedBarIndex by remember { mutableStateOf(-1) }

        // Card de Resumo Semanal (Weekly Performance summary with daily interactive chart)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Resumo Semanal",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Consolidado dos últimos 7 dias de atividades",
                    fontSize = 12.sp,
                    color = AquaticOnSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Distance Progress
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Pool,
                                contentDescription = null,
                                tint = OceanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Distância Total",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AquaticOnSurface
                            )
                        }
                        Text(
                            text = String.format("%,d m / %,d m", weeklyDistanceTotal, weeklyDistanceGoal),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(OceanSecondaryContainer)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(weeklyDistanceProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(OceanSecondary, OceanPrimary)
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(weeklyDistanceProgress * 100).toInt()}% da meta de volume",
                        fontSize = 11.sp,
                        color = AquaticOnSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Duration Progress
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = OceanPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tempo de Treino",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AquaticOnSurface
                            )
                        }
                        Text(
                            text = String.format(
                                "%dh %02dmin / %dh 00min",
                                weeklyDurationTotalMinutes / 60,
                                weeklyDurationTotalMinutes % 60,
                                weeklyDurationGoalMinutes / 60
                            ),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(OceanSecondaryContainer)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(weeklyDurationProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(OceanPrimary, OceanPrimaryContainer)
                                    )
                                )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${(weeklyDurationProgress * 100).toInt()}% da meta de tempo",
                        fontSize = 11.sp,
                        color = AquaticOnSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Custom Selector/Toggles for Chart (Material 3 Segmented Control style)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("Distância (m)", "Tempo (min)")
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedChartTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) OceanPrimaryContainer else Color.Transparent)
                                .clickable {
                                    selectedChartTab = index
                                    selectedBarIndex = -1 // Reset selection
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else AquaticOnSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Interactive 7-Day Bar Chart
                val chartData = if (selectedChartTab == 0) dailyDistances else dailyDurationsMinutes
                val maxVal = chartData.maxOrNull() ?: 1f
                val chartMax = if (maxVal == 0f) 1f else maxVal

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 4.dp)
                ) {
                    // Draw soft background grid lines (horizontal)
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(4) {
                            HorizontalDivider(
                                color = AquaticOutlineVariant.copy(alpha = 0.2f),
                                thickness = 1.dp
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp)) // Spacer at bottom
                    }

                    // Columns (Bars)
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        chartData.forEachIndexed { index, value ->
                            val heightFraction = (value / chartMax).coerceIn(0f, 1f)
                            val isToday = index == 6
                            val isSelected = selectedBarIndex == index

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.Bottom,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Value indicator label at the top of the bar (if active or tapped)
                                Box(
                                    modifier = Modifier
                                        .height(24.dp)
                                        .animateContentSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (value > 0f && (isSelected || isToday)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(OceanPrimary)
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (selectedChartTab == 0) "${value.toInt()}m" else "${value.toInt()}min",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // The active bar Box
                                Box(
                                    modifier = Modifier
                                        .width(20.dp)
                                        .fillMaxHeight(heightFraction * 0.75f) // Limit max height to leave room for labels
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = when {
                                                    isSelected -> listOf(OceanPrimary, OceanPrimaryContainer)
                                                    isToday -> listOf(OceanSecondary, OceanPrimary)
                                                    else -> listOf(OceanPrimary.copy(alpha = 0.4f), OceanPrimary.copy(alpha = 0.7f))
                                                }
                                            )
                                        )
                                        .clickable {
                                            selectedBarIndex = if (selectedBarIndex == index) -1 else index
                                        }
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Day label (e.g. SEG, TER)
                                Text(
                                    text = dayLabels[index],
                                    fontSize = 10.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isToday) OceanPrimary else AquaticOnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Interactive Detail tooltip text under the chart
                AnimatedVisibility(
                    visible = selectedBarIndex != -1,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    if (selectedBarIndex in 0..6) {
                        val dayValue = chartData[selectedBarIndex]
                        val dayName = when (selectedBarIndex) {
                            0 -> "Segunda-feira"
                            1 -> "Terça-feira"
                            2 -> "Quarta-feira"
                            3 -> "Quinta-feira"
                            4 -> "Sexta-feira"
                            5 -> "Sábado"
                            else -> "Domingo"
                        }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = OceanSecondaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (selectedChartTab == 0) Icons.Default.Pool else Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = OceanPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = dayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OceanPrimary
                                    )
                                }
                                Text(
                                    text = if (selectedChartTab == 0) "${dayValue.toInt()} metros nadados" else "${dayValue.toInt()} minutos de treino",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AquaticOnSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bento Grid Details
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Col 1 Item 1: Total Duration
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = OceanPrimary
                    )
                    Column {
                        Text(
                            text = "Duração Total",
                            fontSize = 12.sp,
                            color = AquaticOnSurfaceVariant
                        )
                        Text(
                            text = if (logs.isNotEmpty()) formatDuration(totalDurationSeconds) else "00:00",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaticOnSurface
                        )
                    }
                }
            }

            // Col 1 Item 2: Avg Heart Rate
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = OceanPrimary
                    )
                    Column {
                        Text(
                            text = "FC Média",
                            fontSize = 12.sp,
                            color = AquaticOnSurfaceVariant
                        )
                        Text(
                            text = if (logs.isNotEmpty()) "$averageHeartRate bpm" else "-- bpm",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaticOnSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Col 2 Item 1: Avg Pace
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = OceanPrimary
                    )
                    Column {
                        Text(
                            text = "Ritmo Médio",
                            fontSize = 12.sp,
                            color = AquaticOnSurfaceVariant
                        )
                        Text(
                            text = if (logs.isNotEmpty()) "${formatPace(averagePace)}/100m" else "--/100m",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AquaticOnSurface
                        )
                    }
                }
            }

            // Col 2 Item 2: Sync Data / Baixar Treino button (Interactive simulation!)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
                    .clickable {
                        scope.launch {
                            isSyncing = true
                            kotlinx.coroutines.delay(1800)
                            isSyncing = false
                        }
                    },
                colors = CardDefaults.cardColors(containerColor = OceanPrimaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(
                        imageVector = if (isSyncing) Icons.Default.Autorenew else Icons.Default.CloudDownload,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = if (isSyncing) "Sincronizando..." else "Sincronizar",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = if (isSyncing) "Buscando..." else "Baixar Treino",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // History list details
        Text(
            text = "Histórico de Treinos",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AquaticOnSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (logs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Waves,
                        contentDescription = null,
                        tint = AquaticOutline,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sem treinos gravados ainda",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AquaticOnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Os seus treinos finalizados aparecerão aqui automaticamente.",
                        fontSize = 12.sp,
                        color = AquaticOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                logs.take(5).forEachIndexed { idx, log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(OceanSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${idx + 1}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = log.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AquaticOnSurface
                                    )
                                    Text(
                                        text = "${log.distanceCompleted}m",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OceanPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Ritmo: ${formatPace(log.avgPaceSecondsPer100m)} | ${formatDuration(log.durationSeconds)}",
                                        fontSize = 12.sp,
                                        color = AquaticOnSurfaceVariant
                                    )
                                    Text(
                                        text = formatDate(log.timestamp).split(" ")[0],
                                        fontSize = 11.sp,
                                        color = AquaticOutline
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Weekly Highlight Card (using the third image link)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = "https://lh3.googleusercontent.com/aida-public/AB6AXuCw2766OjTEOjacJF4N9xGpG46smafqWIEGVQZnjb-moDYnqUrgqHFXRApSkuisBb7AOG4cmoYPU3XQafucHAgTTpOxKhqVYMKmAP0tBxFHgDgY38_3JWlbhgIIe17XVcwWh-p2okm12Kj6TLDGI_fvHklvB5SrXCJp2Th9NGhFhi5z1EwCI73gusjnZriU_MHAGbIT7bXKM_w4UPf_ockcxwFfse8fEQVADAbUnxsLA-vsEMIioJUB235sKR9W1kXKgl7pbwmwbbW7",
                    contentDescription = "Swimmer close up",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, OceanPrimary.copy(alpha = 0.85f)),
                                startY = 100f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Destaque da Semana",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Você melhorou a sua velocidade de tiro nos 200m Crawl Livre em 4% nesta semana!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Helper class for dynamic workout creation / modification of phases
data class PhaseItem(val name: String, val details: String)

// Interactive lists screen for browsing predefined workouts
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsListTab(
    viewModel: WorkoutViewModel,
    onWorkoutClick: (Workout) -> Unit
) {
    val level by viewModel.selectedLevel.collectAsState()
    val workouts by viewModel.allWorkouts.collectAsState()

    // Form states
    var isFormOpen by remember { mutableStateOf(false) }
    var editingWorkoutId by remember { mutableStateOf<Int?>(null) } // null = Create, Int = Edit
    var titleInput by remember { mutableStateOf("") }
    var descriptionInput by remember { mutableStateOf("") }
    var totalDistanceInput by remember { mutableStateOf("1500") }
    var estimatedDurationInput by remember { mutableStateOf("45") }
    var kcalInput by remember { mutableStateOf("300") }
    var levelInput by remember { mutableStateOf("Intermediário") }
    var equipmentInput by remember { mutableStateOf("") }
    val phasesList = remember { mutableStateListOf<PhaseItem>() }

    // Deletion confirmation
    var deletingWorkoutIdState by remember { mutableStateOf<Int?>(null) }

    if (isFormOpen) {
        WorkoutFormScreen(
            editingWorkoutId = editingWorkoutId,
            initialTitle = titleInput,
            initialDescription = descriptionInput,
            initialDistance = totalDistanceInput,
            initialDuration = estimatedDurationInput,
            initialKcal = kcalInput,
            initialLevel = levelInput,
            initialEquipment = equipmentInput,
            initialPhases = phasesList,
            onDismiss = { isFormOpen = false },
            onSave = { workout ->
                viewModel.saveWorkout(workout)
                isFormOpen = false
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Top Brand Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Water,
                            contentDescription = "Waves",
                            tint = OceanPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Treinos",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(OceanPrimary)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = level,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (workouts.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AquaticOutline,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Nenhum treino cadastrado.",
                                fontSize = 15.sp,
                                color = AquaticOnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Clique no '+' para criar um novo treino.",
                                fontSize = 13.sp,
                                color = AquaticOutline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 88.dp)
                    ) {
                        items(workouts) { workout ->
                            val isMatchLevel = workout.level == level
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(
                                    width = if (isMatchLevel) 2.dp else 1.dp,
                                    color = if (isMatchLevel) OceanPrimaryContainer else AquaticOutlineVariant.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Clickable area for details
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onWorkoutClick(workout) },
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(OceanSecondaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Pool,
                                                contentDescription = null,
                                                tint = OceanPrimary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = workout.title,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AquaticOnSurface
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(
                                                            if (workout.level == "Avançado") Color(0xFFFFEAEA) else Color(0xFFEAFFE8)
                                                        )
                                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = workout.level,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (workout.level == "Avançado") Color.Red else Color(0xFF007A00)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = workout.description,
                                                fontSize = 13.sp,
                                                color = AquaticOnSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Straighten,
                                                        contentDescription = null,
                                                        tint = AquaticOutline,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${workout.totalDistanceMeters}m",
                                                        fontSize = 12.sp,
                                                        color = AquaticOnSurfaceVariant
                                                    )
                                                }
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Schedule,
                                                        contentDescription = null,
                                                        tint = AquaticOutline,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "${workout.estimatedDurationMinutes} min",
                                                        fontSize = 12.sp,
                                                        color = AquaticOnSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // CRUD Quick Actions (Edit and Delete)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                editingWorkoutId = workout.id
                                                titleInput = workout.title
                                                descriptionInput = workout.description
                                                totalDistanceInput = workout.totalDistanceMeters.toString()
                                                estimatedDurationInput = workout.estimatedDurationMinutes.toString()
                                                kcalInput = workout.kcal.toString()
                                                levelInput = workout.level
                                                equipmentInput = workout.equipment
                                                phasesList.clear()
                                                if (workout.phases.isNotBlank()) {
                                                    workout.phases.split("||").forEach { stage ->
                                                        val parts = stage.split("|")
                                                        if (parts.size >= 2) {
                                                            phasesList.add(PhaseItem(parts[0], parts[1]))
                                                        } else if (parts.isNotEmpty()) {
                                                            phasesList.add(PhaseItem(parts[0], ""))
                                                        }
                                                    }
                                                }
                                                isFormOpen = true
                                            },
                                            modifier = Modifier.size(32.dp).testTag("edit_workout_${workout.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar treino",
                                                tint = OceanPrimary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                deletingWorkoutIdState = workout.id
                                            },
                                            modifier = Modifier.size(32.dp).testTag("delete_workout_${workout.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Excluir treino",
                                                tint = Color.Red.copy(alpha = 0.8f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating Action Button to Register a New Workout
            FloatingActionButton(
                onClick = {
                    editingWorkoutId = null
                    titleInput = ""
                    descriptionInput = ""
                    totalDistanceInput = "1500"
                    estimatedDurationInput = "45"
                    kcalInput = "300"
                    levelInput = level // Pre-fill with current category level
                    equipmentInput = "Palmar, Pull Buoy"
                    phasesList.clear()
                    phasesList.add(PhaseItem("Aquecimento", "300m Crawl leve e solto"))
                    phasesList.add(PhaseItem("Principal", "10x100m Crawl alternando intensidade"))
                    phasesList.add(PhaseItem("Final", "100m Soltura de braços"))
                    isFormOpen = true
                },
                containerColor = OceanPrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_add_workout")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Treino")
            }
        }
    }

    // Deletion confirmation
    if (deletingWorkoutIdState != null) {
        AlertDialog(
            onDismissRequest = { deletingWorkoutIdState = null },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Tem certeza que deseja excluir este treino? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        deletingWorkoutIdState?.let { id ->
                            viewModel.deleteWorkout(id)
                        }
                        deletingWorkoutIdState = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Excluir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingWorkoutIdState = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutFormScreen(
    editingWorkoutId: Int?,
    initialTitle: String,
    initialDescription: String,
    initialDistance: String,
    initialDuration: String,
    initialKcal: String,
    initialLevel: String,
    initialEquipment: String,
    initialPhases: List<PhaseItem>,
    onDismiss: () -> Unit,
    onSave: (Workout) -> Unit
) {
    var titleInput by remember { mutableStateOf(initialTitle) }
    var descriptionInput by remember { mutableStateOf(initialDescription) }
    var totalDistanceInput by remember { mutableStateOf(initialDistance) }
    var estimatedDurationInput by remember { mutableStateOf(initialDuration) }
    var kcalInput by remember { mutableStateOf(initialKcal) }
    var levelInput by remember { mutableStateOf(initialLevel) }
    var equipmentInput by remember { mutableStateOf(initialEquipment) }
    val phasesList = remember { mutableStateListOf<PhaseItem>().apply { addAll(initialPhases) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingWorkoutId == null) "Cadastrar Treino" else "Editar Treino",
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_workout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Cancelar",
                            tint = OceanPrimary
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (titleInput.isNotBlank()) {
                                val serializedPhases = phasesList.joinToString("||") { "${it.name}|${it.details}" }
                                val workoutToSave = Workout(
                                    id = editingWorkoutId ?: 0,
                                    title = titleInput,
                                    description = descriptionInput,
                                    totalDistanceMeters = totalDistanceInput.toIntOrNull() ?: 0,
                                    estimatedDurationMinutes = estimatedDurationInput.toIntOrNull() ?: 0,
                                    kcal = kcalInput.toIntOrNull() ?: 0,
                                    level = levelInput,
                                    equipment = equipmentInput,
                                    phases = if (serializedPhases.isNotBlank()) serializedPhases else "Geral|Treino completo"
                                )
                                onSave(workoutToSave)
                            }
                        },
                        enabled = titleInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .testTag("save_workout_button")
                    ) {
                        Text("Salvar", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = AquaticBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card: Informações Gerais
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Informações Gerais",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = OceanPrimary
                    )
                    
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Título do Treino") },
                        modifier = Modifier.fillMaxWidth().testTag("workout_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OceanPrimary,
                            unfocusedBorderColor = AquaticOutlineVariant
                        )
                    )
                    
                    OutlinedTextField(
                        value = descriptionInput,
                        onValueChange = { descriptionInput = it },
                        label = { Text("Descrição do Treino") },
                        modifier = Modifier.fillMaxWidth().testTag("workout_desc_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OceanPrimary,
                            unfocusedBorderColor = AquaticOutlineVariant
                        )
                    )
                    
                    OutlinedTextField(
                        value = equipmentInput,
                        onValueChange = { equipmentInput = it },
                        label = { Text("Equipamentos necessários") },
                        placeholder = { Text("Ex: Palmar, Pull Buoy, Prancha") },
                        modifier = Modifier.fillMaxWidth().testTag("workout_equipment_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OceanPrimary,
                            unfocusedBorderColor = AquaticOutlineVariant
                        )
                    )
                }
            }

            // Card: Métricas e Dificuldade
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Métricas e Nível",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = OceanPrimary
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = totalDistanceInput,
                            onValueChange = { totalDistanceInput = it },
                            label = { Text("Distância (m)") },
                            modifier = Modifier.weight(1f).testTag("workout_distance_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OceanPrimary,
                                unfocusedBorderColor = AquaticOutlineVariant
                            )
                        )
                        OutlinedTextField(
                            value = estimatedDurationInput,
                            onValueChange = { estimatedDurationInput = it },
                            label = { Text("Tempo (min)") },
                            modifier = Modifier.weight(1f).testTag("workout_duration_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OceanPrimary,
                                unfocusedBorderColor = AquaticOutlineVariant
                            )
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = kcalInput,
                            onValueChange = { kcalInput = it },
                            label = { Text("Calorias (kcal)") },
                            modifier = Modifier.weight(1f).testTag("workout_kcal_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OceanPrimary,
                                unfocusedBorderColor = AquaticOutlineVariant
                            )
                        )
                        
                        Box(modifier = Modifier.weight(1f)) {
                            var expanded by remember { mutableStateOf(false) }
                            OutlinedTextField(
                                value = levelInput,
                                onValueChange = {},
                                label = { Text("Nível") },
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(onClick = { expanded = !expanded }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Nível")
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = !expanded }
                                    .testTag("workout_level_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OceanPrimary,
                                    unfocusedBorderColor = AquaticOutlineVariant
                                )
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Intermediário") },
                                    onClick = {
                                        levelInput = "Intermediário"
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Avançado") },
                                    onClick = {
                                        levelInput = "Avançado"
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Card: Fases do Treino
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fases do Treino",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = OceanPrimary
                        )
                        
                        TextButton(
                            onClick = {
                                phasesList.add(PhaseItem("Nova Fase", "Detalhes da fase..."))
                            },
                            modifier = Modifier.testTag("add_phase_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Fase", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adicionar Fase")
                        }
                    }
                    
                    if (phasesList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nenhuma fase cadastrada. Clique em 'Adicionar Fase'.",
                                fontSize = 13.sp,
                                color = AquaticOnSurfaceVariant
                            )
                        }
                    } else {
                        phasesList.forEachIndexed { index, phase ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = OceanSecondaryContainer.copy(alpha = 0.15f)),
                                border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.15f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Fase ${index + 1}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = OceanPrimary
                                        )
                                        IconButton(
                                            onClick = { phasesList.removeAt(index) },
                                            modifier = Modifier.size(24.dp).testTag("delete_phase_$index")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remover fase",
                                                tint = Color.Red.copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                    
                                    OutlinedTextField(
                                        value = phase.name,
                                        onValueChange = { newName ->
                                            phasesList[index] = phase.copy(name = newName)
                                        },
                                        label = { Text("Nome da Fase") },
                                        modifier = Modifier.fillMaxWidth().testTag("phase_name_input_$index"),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = OceanPrimary,
                                            unfocusedBorderColor = AquaticOutlineVariant
                                        )
                                    )
                                    
                                    OutlinedTextField(
                                        value = phase.details,
                                        onValueChange = { newDetails ->
                                            phasesList[index] = phase.copy(details = newDetails)
                                        },
                                        label = { Text("Detalhes da Fase") },
                                        modifier = Modifier.fillMaxWidth().testTag("phase_details_input_$index"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = OceanPrimary,
                                            unfocusedBorderColor = AquaticOutlineVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Personal Trainer & Scheduling System
data class PersonalTrainer(
    val name: String,
    val specialty: String,
    val rating: Double,
    val experienceYears: Int,
    val recommendedLevel: String, // "Intermediário", "Avançado", "Todos"
    val bio: String,
    val availableTimes: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessoriaTab(viewModel: WorkoutViewModel) {
    val level by viewModel.selectedLevel.collectAsState()
    val workouts by viewModel.allWorkouts.collectAsState()
    val appointments by viewModel.allAppointments.collectAsState()
    
    val trainers = remember {
        listOf(
            PersonalTrainer("Carlos Rocha", "Técnica de Nado & Triatlo", 4.9, 12, "Todos", "Especialista em biomecânica do nado e transição para águas abertas. Ideal para correção de braçada.", listOf("06:30", "08:00", "12:00", "18:30", "20:00")),
            PersonalTrainer("Amanda Lima", "Treino de Performance & Velocidade", 5.0, 8, "Avançado", "Preparadora de atletas de elite. Foco em séries de alta intensidade, limiar anaeróbico e potência.", listOf("07:00", "08:30", "13:00", "17:30", "19:00")),
            PersonalTrainer("Maurício Dias", "Resistência & Águas Abertas", 4.8, 15, "Intermediário", "Campeão de travessias de longa distância. Foco em ritmo sustentado, respiração bilateral e táticas de prova.", listOf("06:00", "07:30", "09:00", "16:00", "18:00")),
            PersonalTrainer("Sofia Neves", "Postura & Prevenção de Lesões", 4.9, 6, "Todos", "Fisioterapeuta e treinadora. Foco em fortalecimento, flexibilidade articular e nado preventivo saudável.", listOf("08:00", "09:30", "11:30", "15:00", "17:00"))
        )
    }
    
    var selectedTrainer by remember { mutableStateOf(trainers[0]) }
    
    val days = remember {
        val list = mutableListOf<Triple<String, String, String>>() // <DayName, DayNumber, DateStr>
        val cal = Calendar.getInstance()
        val dayFormatter = SimpleDateFormat("EEE", Locale("pt", "BR"))
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        for (i in 0..6) {
            val dayName = dayFormatter.format(cal.time).replace(".", "").uppercase()
            val dayNum = cal.get(Calendar.DAY_OF_MONTH).toString()
            val dateStr = dateFormatter.format(cal.time)
            list.add(Triple(dayName, dayNum, dateStr))
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        list
    }
    
    var selectedDateStr by remember { mutableStateOf(days[0].third) }
    var selectedTimeSlot by remember { mutableStateOf(selectedTrainer.availableTimes.firstOrNull() ?: "08:00") }
    var selectedWorkoutTitle by remember { mutableStateOf("Treino Técnico (Foco na Técnica)") }
    var workoutDropdownExpanded by remember { mutableStateOf(false) }
    
    var alertMessage by remember { mutableStateOf<String?>(null) }
    var showCancelDialogId by remember { mutableStateOf<Int?>(null) }

    // Reset default time slot if selected trainer changes
    LaunchedEffect(selectedTrainer) {
        selectedTimeSlot = selectedTrainer.availableTimes.firstOrNull() ?: "08:00"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Sports,
                    contentDescription = null,
                    tint = OceanPrimary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Assessoria",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary,
                    letterSpacing = (-0.5).sp
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(OceanPrimary)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = level,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        if (alertMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE6F4EA)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF34A853).copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF137333))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = alertMessage!!,
                        color = Color(0xFF137333),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { alertMessage = null },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color(0xFF137333), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Section 1: Trainer Selection
        Text(
            text = "Escolha seu Personal Trainer",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AquaticOnSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            trainers.forEach { trainer ->
                val isSelected = trainer.name == selectedTrainer.name
                val isRecommended = trainer.recommendedLevel == level || trainer.recommendedLevel == "Todos"
                
                Card(
                    modifier = Modifier
                        .width(280.dp)
                        .clickable { selectedTrainer = trainer },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) OceanPrimary.copy(alpha = 0.03f) else Color.White
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) OceanPrimary else AquaticOutlineVariant.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(OceanPrimaryContainer, OceanPrimary)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = trainer.name.split(" ").map { it.take(1) }.joinToString(""),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Coach " + trainer.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = AquaticOnSurface
                                )
                                Text(
                                    text = trainer.specialty,
                                    fontSize = 12.sp,
                                    color = AquaticOnSurfaceVariant
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = "Nota", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${trainer.rating}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AquaticOnSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${trainer.experienceYears} anos exp.",
                                    fontSize = 12.sp,
                                    color = AquaticOnSurfaceVariant
                                )
                            }

                            if (isRecommended) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFEAFFE8), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Indicado",
                                        color = Color(0xFF007A00),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = trainer.bio,
                            fontSize = 12.sp,
                            color = AquaticOnSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Selected Trainer Detail & Booking Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.3f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Agendar com Coach ${selectedTrainer.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary
                )

                // Calendar
                Text(
                    text = "Selecione o Dia",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AquaticOnSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    days.forEach { (dayName, dayNum, dateStr) ->
                        val isSelected = dateStr == selectedDateStr
                        Card(
                            modifier = Modifier
                                .width(60.dp)
                                .clickable { selectedDateStr = dateStr },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) OceanPrimary else Color.White
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) OceanPrimary else AquaticOutlineVariant.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = dayName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color.White else AquaticOnSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dayNum,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else AquaticOnSurface
                                )
                            }
                        }
                    }
                }

                // Time Slots
                Text(
                    text = "Horários Disponíveis",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AquaticOnSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    selectedTrainer.availableTimes.forEach { slot ->
                        val isSelected = slot == selectedTimeSlot
                        Card(
                            modifier = Modifier
                                .clickable { selectedTimeSlot = slot }
                                .clip(RoundedCornerShape(20.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) OceanSecondaryContainer else Color.White
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) OceanPrimary else AquaticOutlineVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = slot,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) OceanPrimary else AquaticOnSurface
                            )
                        }
                    }
                }

                // Workout Selection Dropdown
                Text(
                    text = "Selecione o Treino",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AquaticOnSurfaceVariant
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        onClick = { workoutDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, AquaticOutlineVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedWorkoutTitle,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AquaticOnSurface
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = OceanPrimary)
                        }
                    }
                    DropdownMenu(
                        expanded = workoutDropdownExpanded,
                        onDismissRequest = { workoutDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f).background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Treino Técnico (Escolha do Personal)", fontSize = 13.sp) },
                            onClick = {
                                selectedWorkoutTitle = "Treino Técnico (Escolha do Personal)"
                                workoutDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Treino de Regeneração Pulmonar", fontSize = 13.sp) },
                            onClick = {
                                selectedWorkoutTitle = "Treino de Regeneração Pulmonar"
                                workoutDropdownExpanded = false
                            }
                        )
                        workouts.forEach { workout ->
                            DropdownMenuItem(
                                text = { Text("${workout.title} (${workout.level})", fontSize = 13.sp) },
                                onClick = {
                                    selectedWorkoutTitle = workout.title
                                    workoutDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val newAppointment = com.example.data.Appointment(
                            trainerName = selectedTrainer.name,
                            trainerSpecialty = selectedTrainer.specialty,
                            dateString = selectedDateStr,
                            timeSlot = selectedTimeSlot,
                            workoutTitle = selectedWorkoutTitle,
                            status = "Agendado"
                        )
                        viewModel.saveAppointment(newAppointment)
                        alertMessage = "Sua sessão com Coach ${selectedTrainer.name} foi agendada!"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_booking_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanPrimary),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Confirmar Agendamento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }
        }

        // Section 2: Appointments List
        Text(
            text = "Meus Agendamentos",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AquaticOnSurface
        )

        if (appointments.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = AquaticOutlineVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nenhum agendamento ativo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AquaticOnSurface
                    )
                    Text(
                        text = "Agende uma sessão com nossos coaches acima.",
                        fontSize = 12.sp,
                        color = AquaticOnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                appointments.forEach { appointment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, AquaticOutlineVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(OceanSecondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = OceanPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Coach " + appointment.trainerName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AquaticOnSurface
                                )
                                Text(
                                    text = appointment.trainerSpecialty,
                                    fontSize = 11.sp,
                                    color = AquaticOnSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = null,
                                            tint = AquaticOutline,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        val displayDate = try {
                                            val parts = appointment.dateString.split("-")
                                            if (parts.size == 3) "${parts[2]}/${parts[1]}" else appointment.dateString
                                        } catch(e: Exception) {
                                            appointment.dateString
                                        }
                                        Text(
                                            text = displayDate,
                                            fontSize = 11.sp,
                                            color = AquaticOnSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = AquaticOutline,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = appointment.timeSlot,
                                            fontSize = 11.sp,
                                            color = AquaticOnSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "Treino: " + appointment.workoutTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OceanPrimary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = { showCancelDialogId = appointment.id },
                                modifier = Modifier.testTag("delete_appointment_btn_${appointment.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Cancelar sessão",
                                    tint = Color.Red.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }

    if (showCancelDialogId != null) {
        AlertDialog(
            onDismissRequest = { showCancelDialogId = null },
            title = { Text("Cancelar Treino", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Deseja realmente cancelar este agendamento com o personal?", fontSize = 14.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCancelDialogId?.let { viewModel.deleteAppointment(it) }
                        showCancelDialogId = null
                    }
                ) {
                    Text("Confirmar", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialogId = null }) {
                    Text("Voltar")
                }
            }
        )
    }
}

