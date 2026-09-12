package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Appointment
import com.example.data.Workout
import com.example.data.WorkoutRepository
import com.example.data.WorkoutSessionLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WorkoutRepository
    
    val allWorkouts: StateFlow<List<Workout>>
    val allSessionLogs: StateFlow<List<WorkoutSessionLog>>
    val allAppointments: StateFlow<List<Appointment>>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = WorkoutRepository(database.workoutDao())
        allWorkouts = repository.allWorkouts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        allSessionLogs = repository.allSessionLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        allAppointments = repository.allAppointments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private val sharedPrefs = application.getSharedPreferences("bt_acqua_prefs", android.content.Context.MODE_PRIVATE)

    private val _weeklyDistanceGoal = MutableStateFlow(sharedPrefs.getInt("weekly_distance_goal", 5000))
    val weeklyDistanceGoal: StateFlow<Int> = _weeklyDistanceGoal.asStateFlow()

    fun updateWeeklyDistanceGoal(meters: Int) {
        _weeklyDistanceGoal.value = meters
        sharedPrefs.edit().putInt("weekly_distance_goal", meters).apply()
    }

    private fun getStartOfWeekTimestamp(): Long {
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        val today = java.util.Calendar.getInstance()
        if (today.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY) {
            cal.add(java.util.Calendar.WEEK_OF_YEAR, -1)
        }
        return cal.timeInMillis
    }

    val weeklyDistanceCompleted: StateFlow<Int> = allSessionLogs.map { logs ->
        val startOfWeek = getStartOfWeekTimestamp()
        logs.filter { it.timestamp >= startOfWeek }.sumOf { it.distanceCompleted }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Level toggle: "Intermediário" vs "Avançado"
    private val _selectedLevel = MutableStateFlow("Intermediário")
    val selectedLevel: StateFlow<String> = _selectedLevel.asStateFlow()

    fun selectLevel(level: String) {
        _selectedLevel.value = level
        // Automatically switch active/viewed workout to match level if viewing home
        val currentHome = filteredWorkouts.value.firstOrNull { it.level == level }
        if (currentHome != null) {
            _selectedWorkout.value = currentHome
        }
    }

    // Filtered workouts based on level
    val filteredWorkouts = combine(allWorkouts, selectedLevel) { workouts, level ->
        workouts.filter { it.level == level }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current selected workout (for detail / preparation screen)
    private val _selectedWorkout = MutableStateFlow<Workout?>(null)
    val selectedWorkout: StateFlow<Workout?> = _selectedWorkout.asStateFlow()

    fun selectWorkout(workout: Workout) {
        _selectedWorkout.value = workout
    }

    // Workout execution state
    private val _activeWorkout = MutableStateFlow<Workout?>(null)
    val activeWorkout: StateFlow<Workout?> = _activeWorkout.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _currentPhaseIndex = MutableStateFlow(0)
    val currentPhaseIndex: StateFlow<Int> = _currentPhaseIndex.asStateFlow()

    private var timerJob: Job? = null

    fun startWorkout(workout: Workout) {
        _activeWorkout.value = workout
        _currentPhaseIndex.value = 0
        _elapsedSeconds.value = 0
        _isTimerRunning.value = true
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_isTimerRunning.value) {
                    _elapsedSeconds.value += 1
                }
            }
        }
    }

    fun pauseOrResumeTimer() {
        _isTimerRunning.value = !_isTimerRunning.value
    }

    fun advancePhase() {
        val workout = _activeWorkout.value ?: return
        val phasesList = workout.phases.split("||")
        if (_currentPhaseIndex.value < phasesList.size - 1) {
            _currentPhaseIndex.value += 1
        } else {
            // Completed! Save session log and clear active
            completeWorkout()
        }
    }

    private fun completeWorkout() {
        val workout = _activeWorkout.value ?: return
        val duration = _elapsedSeconds.value
        
        viewModelScope.launch {
            // Save to database
            val log = WorkoutSessionLog(
                workoutId = workout.id,
                title = workout.title,
                distanceCompleted = workout.totalDistanceMeters,
                durationSeconds = duration,
                avgHeartRate = (138..152).random(), // Simulate realistic aerobic heart rate range
                avgPaceSecondsPer100m = if (workout.totalDistanceMeters > 0) {
                    (duration * 100) / workout.totalDistanceMeters
                } else 105
            )
            repository.insertSessionLog(log)
            
            // Clear active workout state
            _activeWorkout.value = null
            _isTimerRunning.value = false
            timerJob?.cancel()
        }
    }

    fun cancelActiveWorkout() {
        _activeWorkout.value = null
        _isTimerRunning.value = false
        timerJob?.cancel()
        _elapsedSeconds.value = 0
        _currentPhaseIndex.value = 0
    }

    fun deleteLog(id: Int) {
        viewModelScope.launch {
            repository.deleteSessionLog(id)
        }
    }

    fun saveWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.insertWorkout(workout)
        }
    }

    fun deleteWorkout(id: Int) {
        viewModelScope.launch {
            // If the deleted workout was selected or active, clear it
            if (_selectedWorkout.value?.id == id) {
                _selectedWorkout.value = null
            }
            if (_activeWorkout.value?.id == id) {
                cancelActiveWorkout()
            }
            repository.deleteWorkout(id)
        }
    }

    fun saveAppointment(appointment: com.example.data.Appointment) {
        viewModelScope.launch {
            repository.insertAppointment(appointment)
        }
    }

    fun deleteAppointment(id: Int) {
        viewModelScope.launch {
            repository.deleteAppointment(id)
        }
    }
}
