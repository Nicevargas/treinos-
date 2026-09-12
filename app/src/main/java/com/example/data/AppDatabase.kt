package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Workout::class, WorkoutSessionLog::class, Appointment::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bt_acqua_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.workoutDao())
                }
            }
        }

        suspend fun populateDatabase(dao: WorkoutDao) {
            // Pre-populate with default workouts
            dao.insertWorkout(
                Workout(
                    id = 1,
                    title = "Treino Performance",
                    description = "Série técnica de resistência",
                    totalDistanceMeters = 2500,
                    estimatedDurationMinutes = 55,
                    kcal = 480,
                    level = "Intermediário",
                    equipment = "Palmar,Pull Buoy,Nadadeira",
                    phases = "Aquecimento|400m Crawl Relaxado||Preparatória|200m Educativos Medley||Principal|8x100 Crawl com Palmar (Sair para 1'45\" - Z3 75%)||Principal|Intervalo: 30s||Principal|1x600 Crawl completo com Nadadeira||Final|200m Soltura / Alongamento"
                )
            )
            dao.insertWorkout(
                Workout(
                    id = 2,
                    title = "Treino Performance Pro",
                    description = "Série técnica de resistência extrema",
                    totalDistanceMeters = 3500,
                    estimatedDurationMinutes = 75,
                    kcal = 680,
                    level = "Avançado",
                    equipment = "Palmar,Pull Buoy,Nadadeira",
                    phases = "Aquecimento|600m Crawl c/ Snorkel||Preparatória|400m Educativos Medley Técnico||Principal|10x100 Crawl com Palmar (Sair para 1'30\" - Z4 85%)||Principal|Intervalo: 45s||Principal|1x1000 Crawl completo com Nadadeira||Final|300m Soltura / Alongamento"
                )
            )
            dao.insertWorkout(
                Workout(
                    id = 3,
                    title = "Foco em Velocidade",
                    description = "Treino de velocidade e ritmo de prova",
                    totalDistanceMeters = 1800,
                    estimatedDurationMinutes = 45,
                    kcal = 390,
                    level = "Intermediário",
                    equipment = "Palmar,Nadadeira",
                    phases = "Aquecimento|300m Crawl solto||Preparatória|200m Pernas de Crawl com Prancha||Principal|12x50m Crawl Intensidade Máxima (Sair para 1'00\" - Z5 95%)||Principal|Intervalo: 20s||Principal|4x100m Crawl Técnico Desacelerado||Final|100m Soltura"
                )
            )
            dao.insertWorkout(
                Workout(
                    id = 4,
                    title = "Foco em Velocidade Pro",
                    description = "Treino avançado de velocidade pura",
                    totalDistanceMeters = 2600,
                    estimatedDurationMinutes = 60,
                    kcal = 550,
                    level = "Avançado",
                    equipment = "Palmar,Nadadeira",
                    phases = "Aquecimento|400m Crawl solto||Preparatória|300m Pernas de Crawl com Prancha||Principal|16x50m Crawl Intensidade Máxima (Sair para 50\" - Z5 95%)||Principal|Intervalo: 20s||Principal|6x100m Crawl Técnico Desacelerado||Final|200m Soltura"
                )
            )

            // Pre-populate some historical workout session logs for the weekly dashboard view
            val nowMillis = System.currentTimeMillis()
            val dayInMillis = 24 * 60 * 60 * 1000L

            dao.insertSessionLog(
                WorkoutSessionLog(
                    id = 1,
                    workoutId = 1,
                    title = "Treino Performance",
                    distanceCompleted = 2500,
                    durationSeconds = 3300, // 55 mins
                    avgHeartRate = 142,
                    avgPaceSecondsPer100m = 132,
                    timestamp = nowMillis - (1 * dayInMillis) // Ontem (Segunda)
                )
            )
            dao.insertSessionLog(
                WorkoutSessionLog(
                    id = 2,
                    workoutId = 3,
                    title = "Foco em Velocidade",
                    distanceCompleted = 1800,
                    durationSeconds = 2700, // 45 mins
                    avgHeartRate = 145,
                    avgPaceSecondsPer100m = 150,
                    timestamp = nowMillis - (3 * dayInMillis) // Sábado passado
                )
            )
            dao.insertSessionLog(
                WorkoutSessionLog(
                    id = 3,
                    workoutId = 2,
                    title = "Treino Performance Pro",
                    distanceCompleted = 3500,
                    durationSeconds = 4500, // 75 mins
                    avgHeartRate = 138,
                    avgPaceSecondsPer100m = 128,
                    timestamp = nowMillis - (5 * dayInMillis) // Quinta passada
                )
            )
        }
    }
}
