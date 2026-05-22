package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.GeminiPredictEngine
import com.example.data.Task
import com.example.data.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class PredictionState {
    object Idle : PredictionState()
    object Loading : PredictionState()
    data class Success(val reasoning: String, val tips: List<String>) : PredictionState()
    data class Error(val message: String) : PredictionState()
}

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _predictionState = MutableStateFlow<PredictionState>(PredictionState.Idle)
    val predictionState: StateFlow<PredictionState> = _predictionState.asStateFlow()

    private val _selectedStrategy = MutableStateFlow<String>("BALANCED")
    val selectedStrategy: StateFlow<String> = _selectedStrategy.asStateFlow()

    fun selectStrategy(strategy: String) {
        _selectedStrategy.value = strategy
    }

    val isKeyConfigured: Boolean
        get() {
            val key = BuildConfig.GEMINI_API_KEY
            return key.isNotEmpty() && key != "MY_GEMINI_API_KEY"
        }

    fun addTask(
        title: String,
        description: String,
        category: String,
        urgency: String,
        importance: String,
        estimatedMinutes: Int
    ) {
        viewModelScope.launch {
            val newTask = Task(
                title = title,
                description = description,
                category = category,
                urgency = urgency,
                importance = importance,
                estimatedMinutes = estimatedMinutes
            )
            repository.insert(newTask)
        }
    }

    fun updateTask(
        id: Int,
        title: String,
        description: String,
        category: String,
        urgency: String,
        importance: String,
        estimatedMinutes: Int,
        completed: Boolean,
        predictionPriority: Int?,
        predictionReason: String?
    ) {
        viewModelScope.launch {
            val updated = Task(
                id = id,
                title = title,
                description = description,
                category = category,
                urgency = urgency,
                importance = importance,
                estimatedMinutes = estimatedMinutes,
                completed = completed,
                predictionPriority = predictionPriority,
                predictionReason = predictionReason
            )
            repository.update(updated)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(completed = !task.completed)
            repository.update(updated)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }

    fun runPriorityPrediction() {
        if (!isKeyConfigured) {
            _predictionState.value = PredictionState.Error("API Key is not configured. Please add your GEMINI_API_KEY to the Secrets panel.")
            return
        }

        viewModelScope.launch {
            _predictionState.value = PredictionState.Loading

            // Get current incomplete tasks
            val activeTasks = tasks.value.filter { !it.completed }
            if (activeTasks.isEmpty()) {
                _predictionState.value = PredictionState.Error("Please add some active tasks first before predicting priorities.")
                return@launch
            }

            val result = GeminiPredictEngine.predictPriorities(activeTasks, selectedStrategy.value)
            if (result != null) {
                // Update tasks in Room with predicted values
                val now = System.currentTimeMillis()
                for (item in result.items) {
                    val matchingTask = activeTasks.find { it.id == item.id }
                    if (matchingTask != null) {
                        val updatedTask = matchingTask.copy(
                            predictionPriority = item.priorityScore,
                            predictionReason = item.reasoning,
                            lastPredictedAt = now
                        )
                        repository.update(updatedTask)
                    }
                }
                _predictionState.value = PredictionState.Success(
                    reasoning = result.prioritizationReasoning,
                    tips = result.productivityAdvice
                )
            } else {
                _predictionState.value = PredictionState.Error("Failed to predict priorities. Please check your network connection or API Key limits.")
            }
        }
    }

    fun clearPredictionError() {
        if (_predictionState.value is PredictionState.Error) {
            _predictionState.value = PredictionState.Idle
        }
    }
}

class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
