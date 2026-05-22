package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String = "",
    val category: String = "Other", // Work, Personal, Health, Finance, Learn, Other
    val urgency: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val importance: String = "MEDIUM", // LOW, MEDIUM, HIGH
    val completed: Boolean = false,
    val dueDate: Long = System.currentTimeMillis() + 86400000, // Default to tomorrow
    val estimatedMinutes: Int = 30,
    val predictionPriority: Int? = null, // Priority determined by AI (1-100)
    val predictionReason: String? = null, // Why AI thinks this is high priority
    val lastPredictedAt: Long? = null
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY completed ASC, predictionPriority DESC, id DESC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE completed = 0")
    suspend fun getActiveTasksDirect(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "priority_predictor_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

    suspend fun getActiveTasksDirect(): List<Task> = taskDao.getActiveTasksDirect()

    suspend fun insert(task: Task): Long = taskDao.insertTask(task)

    suspend fun update(task: Task) = taskDao.updateTask(task)

    suspend fun delete(task: Task) = taskDao.deleteTask(task)
}
