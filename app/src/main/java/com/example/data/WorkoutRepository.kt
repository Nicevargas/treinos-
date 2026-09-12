package com.example.data

import kotlinx.coroutines.flow.Flow

class WorkoutRepository(private val workoutDao: WorkoutDao) {
    val allWorkouts: Flow<List<Workout>> = workoutDao.getAllWorkouts()
    val allSessionLogs: Flow<List<WorkoutSessionLog>> = workoutDao.getAllSessionLogs()
    val allAppointments: Flow<List<Appointment>> = workoutDao.getAllAppointments()

    suspend fun getWorkoutById(id: Int): Workout? {
        return workoutDao.getWorkoutById(id)
    }

    suspend fun insertWorkout(workout: Workout): Long {
        return workoutDao.insertWorkout(workout)
    }

    suspend fun deleteWorkout(id: Int) {
        workoutDao.deleteWorkoutById(id)
    }

    suspend fun insertSessionLog(log: WorkoutSessionLog): Long {
        return workoutDao.insertSessionLog(log)
    }

    suspend fun deleteSessionLog(id: Int) {
        workoutDao.deleteSessionLogById(id)
    }

    suspend fun insertAppointment(appointment: Appointment): Long {
        return workoutDao.insertAppointment(appointment)
    }

    suspend fun deleteAppointment(id: Int) {
        workoutDao.deleteAppointmentById(id)
    }
}
