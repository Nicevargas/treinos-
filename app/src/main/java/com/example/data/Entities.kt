package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val totalDistanceMeters: Int,
    val estimatedDurationMinutes: Int,
    val kcal: Int,
    val level: String, // "Intermediário" or "Avançado"
    val equipment: String, // Comma-separated: "Palmar,Pull Buoy,Nadadeira"
    val phases: String // Serialized descriptions of phases
)

@Entity(tableName = "workout_session_logs")
data class WorkoutSessionLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val workoutId: Int,
    val title: String,
    val distanceCompleted: Int,
    val durationSeconds: Int,
    val avgHeartRate: Int,
    val avgPaceSecondsPer100m: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointments")
data class Appointment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val trainerName: String,
    val trainerSpecialty: String,
    val dateString: String,      // e.g. "2026-07-02"
    val timeSlot: String,        // e.g. "08:30"
    val workoutTitle: String,    // e.g. "Treino Performance"
    val status: String = "Agendado", // "Agendado", "Concluído", "Cancelado"
    val timestamp: Long = System.currentTimeMillis()
)

