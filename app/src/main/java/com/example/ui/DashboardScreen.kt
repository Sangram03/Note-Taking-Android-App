package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceHub
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Task
import com.example.ui.theme.AccentMint
import com.example.ui.theme.NeonLime
import com.example.ui.theme.SolidIndigo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val predictionState by viewModel.predictionState.collectAsState()
    val isKeyConfigured = viewModel.isKeyConfigured
    val selectedStrategy by viewModel.selectedStrategy.collectAsState()

    var showAddTaskSheet by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var selectedFilterTab by remember { mutableStateOf("FOCUS") } // FOCUS, ACTIVE, COMPLETED

    var activeFocusTask by remember { mutableStateOf<Task?>(null) }
    var activeFocusSecondsLeft by remember { mutableStateOf(0) }
    var activeFocusIsPlaying by remember { mutableStateOf(false) }
    var activeFocusTotalSeconds by remember { mutableStateOf(1) }

    LaunchedEffect(activeFocusIsPlaying) {
        if (activeFocusIsPlaying) {
            while (activeFocusSecondsLeft > 0) {
                kotlinx.coroutines.delay(1000L)
                activeFocusSecondsLeft -= 1
            }
            if (activeFocusSecondsLeft == 0) {
                activeFocusIsPlaying = false
            }
        }
    }

    val todayDateStr = remember {
        val formatter = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US)
        formatter.format(Date()).uppercase()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.statusBars,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .testTag("add_task_fab")
                    .navigationBarsPadding()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create New Task",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- HEADER ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "PRIORITY PREDICTOR",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = todayDateStr,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NeonLime,
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(SolidIndigo, Color.Transparent)))
                            .clickable { viewModel.runPriorityPrediction() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Trigger AI Refresh",
                            tint = NeonLime,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // --- PRODUCTIVITY METRICS KPI PANEL ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val totalTasks = tasks.size
                    val completedTasks = tasks.count { it.completed }
                    val completedPercent = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0
                    val activeTasks = tasks.filter { !it.completed }
                    val totalMinutes = activeTasks.sumOf { it.estimatedMinutes }
                    val hours = totalMinutes / 60
                    val mins = totalMinutes % 60
                    val focusTimeStr = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

                    // KPI 1: Completion
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111322)),
                        border = BorderStroke(1.dp, Color(0xFF1F243E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "COMPLETED STATUS", 
                                fontSize = 8.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = Color(0xFF8F9BB3), 
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("$completedTasks / $totalTasks Tasks", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))
                            // Tiny linear progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1A1C30))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(if (completedPercent > 0) completedPercent / 100f else 0.01f)
                                        .fillMaxHeight()
                                        .clip(CircleShape)
                                        .background(AccentMint)
                                )
                            }
                        }
                    }

                    // KPI 2: Total Focus load
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF111322)),
                        border = BorderStroke(1.dp, Color(0xFF1F243E)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "TOTAL FOCUS VOL", 
                                fontSize = 8.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = Color(0xFF8F9BB3), 
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(focusTimeStr, fontSize = 13.sp, fontWeight = FontWeight.Black, color = NeonLime)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (totalMinutes > 120) "🔴 Intense load" else "🟢 Managed pace",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (totalMinutes > 120) Color(0xFFEF4444) else AccentMint
                            )
                        }
                    }
                }

                // API Key missing card
                if (!isKeyConfigured) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_alert_card"),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0x1FEF4444)
                        ),
                        border = BorderStroke(1.dp, Color(0x66EF4444)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alert",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "AI connection inactive. Setup your GEMINI_API_KEY in the AI Studio Secrets panel to activate priority scheduling predictions.",
                                fontSize = 12.sp,
                                color = Color(0xFFFCA5A5),
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // --- AI INSIGHT SECTION ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                AIInsightCard(
                    state = predictionState,
                    isKeyConfigured = isKeyConfigured,
                    selectedStrategy = selectedStrategy,
                    onStrategySelected = { viewModel.selectStrategy(it) },
                    onPredictClick = { viewModel.runPriorityPrediction() },
                    onDismissError = { viewModel.clearPredictionError() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SEGMENTED FILTER CHIPS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterTabChip(
                    label = "AI Focus Plan",
                    icon = Icons.Default.AutoAwesome,
                    selected = selectedFilterTab == "FOCUS",
                    tag = "filter_tab_focus",
                    onClick = { selectedFilterTab = "FOCUS" }
                )
                FilterTabChip(
                    label = "Active (${tasks.count { !it.completed }})",
                    icon = Icons.Default.DeviceHub,
                    selected = selectedFilterTab == "ACTIVE",
                    tag = "filter_tab_active",
                    onClick = { selectedFilterTab = "ACTIVE" }
                )
                FilterTabChip(
                    label = "Completed",
                    icon = Icons.Default.CheckCircle,
                    selected = selectedFilterTab == "COMPLETED",
                    tag = "filter_tab_completed",
                    onClick = { selectedFilterTab = "COMPLETED" }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- TASK LISTS ---
            val filteredTasks = when (selectedFilterTab) {
                "FOCUS" -> tasks.filter { !it.completed && it.predictionPriority != null }
                    .sortedByDescending { it.predictionPriority }
                "ACTIVE" -> tasks.filter { !it.completed }
                "COMPLETED" -> tasks.filter { it.completed }
                else -> tasks
            }

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = when (selectedFilterTab) {
                                "FOCUS" -> Icons.Default.Lightbulb
                                "COMPLETED" -> Icons.Default.CheckCircle
                                else -> Icons.Default.DeviceHub
                            },
                            contentDescription = "Empty plan",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (selectedFilterTab) {
                                "FOCUS" -> if (tasks.none { !it.completed }) {
                                    "No active tasks to predict! Create some tasks using the '+' button below."
                                } else {
                                    "Your AI Focus Plan is empty. Tap the sparkles icon in the roadmap above to auto-generate predictions."
                                }
                                "COMPLETED" -> "No completed tasks yet. Finish a task to archive it!"
                                else -> "All done! What are you building next?"
                            },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("task_list"),
                    contentPadding = WindowInsets.navigationBars.asPaddingValues()
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        TaskItemCard(
                            task = task,
                            onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                            onDelete = { viewModel.deleteTask(task) },
                            onEdit = { taskToEdit = task },
                            onStartFocus = {
                                activeFocusTask = task
                                activeFocusSecondsLeft = task.estimatedMinutes * 60
                                activeFocusTotalSeconds = task.estimatedMinutes * 60
                                activeFocusIsPlaying = true
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(86.dp))
                    }
                }
            }
        }
    }

    // --- ADD/EDIT TASK SHEET ---
        if (showAddTaskSheet || taskToEdit != null) {
            AddTaskBottomSheet(
                taskToEdit = taskToEdit,
                onDismiss = { 
                    showAddTaskSheet = false
                    taskToEdit = null
                },
                onTaskAdd = { title, desc, cat, urg, imp, minutes ->
                    viewModel.addTask(title, desc, cat, urg, imp, minutes)
                    showAddTaskSheet = false
                },
                onTaskUpdate = { updatedTask ->
                    viewModel.updateTask(
                        id = updatedTask.id,
                        title = updatedTask.title,
                        description = updatedTask.description,
                        category = updatedTask.category,
                        urgency = updatedTask.urgency,
                        importance = updatedTask.importance,
                        estimatedMinutes = updatedTask.estimatedMinutes,
                        completed = updatedTask.completed,
                        predictionPriority = updatedTask.predictionPriority,
                        predictionReason = updatedTask.predictionReason
                    )
                    taskToEdit = null
                }
            )
        }

        // --- FOCUS TIMER FLOATING CONTROLS PANEL ---
        AnimatedVisibility(
            visible = activeFocusTask != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            val task = activeFocusTask
            if (task != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("focus_timer_card"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13152A)),
                    border = BorderStroke(1.5.dp, SolidIndigo),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val percent = if (activeFocusTotalSeconds > 0) activeFocusSecondsLeft.toFloat() / activeFocusTotalSeconds else 0f
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(44.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { percent },
                                color = NeonLime,
                                trackColor = Color(0xFF1E213E),
                                strokeWidth = 3.5.dp,
                                modifier = Modifier.fillMaxSize()
                            )
                            val min = activeFocusSecondsLeft / 60
                            val sec = activeFocusSecondsLeft % 60
                            Text(
                                text = String.format("%02d:%02d", min, sec),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE FOCUS SESSION",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8F9BB3),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = task.title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = { activeFocusIsPlaying = !activeFocusIsPlaying },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF1C1E3C), CircleShape)
                            ) {
                                Icon(
                                    imageVector = if (activeFocusIsPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Playback toggle",
                                    tint = NeonLime,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.toggleTaskCompletion(task)
                                    activeFocusTask = null
                                    activeFocusIsPlaying = false
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(AccentMint.copy(alpha = 0.15f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Complete session task",
                                    tint = AccentMint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = {
                                    activeFocusTask = null
                                    activeFocusIsPlaying = false
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Dismiss session",
                                    tint = Color(0xFF8F9BB3),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
fun FilterTabChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else Color(0xFF161929),
        animationSpec = tween(150),
        label = "tab_color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) NeonLime else Color(0xFF8F9BB3),
        animationSpec = tween(150),
        label = "content_color"
    )
    val borderStroke = if (selected) {
        BorderStroke(1.dp, NeonLime.copy(alpha = 0.5f))
    } else {
        BorderStroke(1.dp, Color(0xFF1E2135))
    }

    Surface(
        modifier = Modifier
            .testTag(tag)
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(20.dp)),
        color = containerColor,
        contentColor = contentColor,
        border = borderStroke
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AIInsightCard(
    state: PredictionState,
    isKeyConfigured: Boolean,
    selectedStrategy: String = "BALANCED",
    onStrategySelected: (String) -> Unit = {},
    onPredictClick: () -> Unit,
    onDismissError: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ai_insight_card"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF111322)
        ),
        border = BorderStroke(1.dp, Color(0xFF1F243E)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Gemini AI",
                        tint = NeonLime,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI FOCUS TIMELINE",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 1.5.sp,
                        color = Color.White
                    )
                }

                if (state is PredictionState.Success) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(SolidIndigo)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PREDICTED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (state) {
                is PredictionState.Idle -> {
                    Text(
                        text = "Auto-assign intelligent priority weights (1-100%) and design a customizable productivity roadmap using AI analysis.",
                        fontSize = 13.sp,
                        color = Color(0xFF9EACC0),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "PRIORITIZATION LENS (CHOOSE AI FOCUS STRATEGY)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val strategies = listOf(
                            "BALANCED" to "🏆 Balanced",
                            "EAT_THE_FROG" to "🐸 Frog Focus",
                            "QUICK_WINS" to "⚡ Quick Wins"
                        )
                        strategies.forEach { (strKey, title) ->
                            val isSelected = selectedStrategy == strKey
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) SolidIndigo else Color(0xFF1B1C30))
                                    .clickable { onStrategySelected(strKey) }
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonLime.copy(alpha = 0.6f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF8F9BB3),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onPredictClick,
                        enabled = isKeyConfigured,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonLime,
                            contentColor = Color(0xFF090A10),
                            disabledContainerColor = Color(0xFF1E2135),
                            disabledContentColor = Color(0xFF5A6375)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("predict_priorities_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Calculate AI Priorities",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                is PredictionState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = NeonLime,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Analyzing task matrix with Gemini...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = NeonLime
                        )
                    }
                }

                is PredictionState.Success -> {
                    Text(
                        text = state.reasoning,
                        fontSize = 13.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Normal
                    )

                    if (state.tips.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            state.tips.forEach { tip ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = "Tip",
                                        tint = NeonLime,
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tip,
                                        fontSize = 12.sp,
                                        color = Color(0xFFACC0D8),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = onPredictClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E2138),
                            contentColor = NeonLime
                        ),
                        border = BorderStroke(1.dp, NeonLime.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .testTag("predict_priorities_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Refresh AI Priority Models",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                is PredictionState.Error -> {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analysis Suspended",
                                color = Color(0xFFFCA5A5),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = onDismissError,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = Color(0xFF9CA3AF),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.message,
                            fontSize = 12.sp,
                            color = Color(0xFF9EACC0),
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onPredictClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E1A1A),
                                contentColor = Color(0xFFFCA5A5)
                            ),
                            border = BorderStroke(1.dp, Color(0xFF4E2020)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Text(
                                text = "Retry Calculation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItemCard(
    task: Task,
    onToggleCompletion: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onStartFocus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .testTag("task_item_card_${task.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (task.completed) Color(0xFF0F111E) else Color(0xFF131526)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when {
                task.completed -> Color(0xFF171A2E)
                task.predictionPriority != null && task.predictionPriority >= 85 -> NeonLime.copy(alpha = 0.35f)
                else -> Color(0xFF20233D)
            }
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onToggleCompletion),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (task.completed) {
                            Icons.Filled.CheckCircle
                        } else {
                            Icons.Filled.CheckCircle // Outline simulation
                        },
                        contentDescription = "Toggle Complete",
                        tint = if (task.completed) AccentMint else Color(0xFF4B5573),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = task.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (task.completed) Color(0xFF4B5573) else Color.White,
                            textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        if (task.predictionPriority != null && !task.completed) {
                            val badgeColor = if (task.predictionPriority >= 85) NeonLime else Color(0xFF4F46E5)
                            val textColor = if (task.predictionPriority >= 85) Color(0xFF090A10) else Color.White

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(badgeColor)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = textColor,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${task.predictionPriority}% PRIORITY",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }

                    if (task.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = task.description,
                            fontSize = 13.sp,
                            color = if (task.completed) Color(0xFF374151) else Color(0xFF8F9BB3),
                            lineHeight = 17.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1E213A))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = task.category.uppercase(),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9EACC0)
                                )
                            }

                            if (!task.completed) {
                                val urgColor = when (task.urgency) {
                                    "HIGH" -> Color(0xFFEF4444)
                                    "MEDIUM" -> Color(0xFFF59E0B)
                                    else -> Color(0xFF10B981)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(urgColor.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = task.urgency,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = urgColor
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = "Duration",
                                    tint = Color(0xFF4B5573),
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${task.estimatedMinutes}m",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4B5573)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!task.completed) {
                                IconButton(
                                    onClick = onStartFocus,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag("start_focus_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Start Focus",
                                        tint = NeonLime,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = onEdit,
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("edit_task_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Task",
                                    tint = Color(0xFF6366F1),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(24.dp)
                                    .testTag("delete_task_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Task",
                                    tint = Color(0x80EF4444),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    if (task.predictionReason != null && !task.completed) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF16192F))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = "Insight",
                                    tint = NeonLime,
                                    modifier = Modifier
                                        .padding(top = 1.dp)
                                        .size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = task.predictionReason,
                                    fontSize = 11.sp,
                                    color = Color(0xFFABC0D8),
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTaskBottomSheet(
    taskToEdit: Task? = null,
    onDismiss: () -> Unit,
    onTaskAdd: (String, String, String, String, String, Int) -> Unit,
    onTaskUpdate: (Task) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var description by remember { mutableStateOf(taskToEdit?.description ?: "") }
    var selectedCategory by remember { mutableStateOf(taskToEdit?.category ?: "Work") }
    var selectedUrgency by remember { mutableStateOf(taskToEdit?.urgency ?: "MEDIUM") }
    var selectedImportance by remember { mutableStateOf(taskToEdit?.importance ?: "MEDIUM") }
    var minutesStr by remember { mutableStateOf(taskToEdit?.estimatedMinutes?.toString() ?: "30") }

    val categories = listOf("Work", "Personal", "Health", "Finance", "Learn", "Other")
    val priorities = listOf("LOW", "MEDIUM", "HIGH")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF101222),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F243E))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (taskToEdit != null) "EDIT PRODUCTIVITY TASK" else "NEW PRODUCTIVITY TASK",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close panel",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                placeholder = { Text("e.g. Finish fiscal budget reports") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_input_title"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonLime,
                    unfocusedBorderColor = Color(0xFF20233D),
                    focusedLabelColor = NeonLime,
                    unfocusedLabelColor = Color(0xFF4B5573),
                    focusedContainerColor = Color(0xFF0C0D17),
                    unfocusedContainerColor = Color(0xFF0C0D17),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Task Notes (Optional)") },
                placeholder = { Text("Add critical links, deliverables, context...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_input_desc"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonLime,
                    unfocusedBorderColor = Color(0xFF20233D),
                    focusedLabelColor = NeonLime,
                    unfocusedLabelColor = Color(0xFF4B5573),
                    focusedContainerColor = Color(0xFF0C0D17),
                    unfocusedContainerColor = Color(0xFF0C0D17),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "WORK CATEGORY",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = NeonLime,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    InputChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = Color(0xFF1A1C30),
                            labelColor = Color(0xFF8F9BB3),
                            selectedContainerColor = SolidIndigo,
                            selectedLabelColor = Color.White
                        ),
                        border = BorderStroke(1.dp, if (isSelected) NeonLime.copy(alpha = 0.5f) else Color(0xFF20233D))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "URGENCY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        priorities.forEach { urg ->
                            val isSelected = selectedUrgency == urg
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) SolidIndigo else Color(0xFF1A1C30))
                                    .clickable { selectedUrgency = urg }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = urg,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF8F9BB3)
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "IMPORTANCE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        priorities.forEach { imp ->
                            val isSelected = selectedImportance == imp
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) SolidIndigo else Color(0xFF1A1C30))
                                    .clickable { selectedImportance = imp }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = imp,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color(0xFF8F9BB3)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ESTIMATED DURATION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonLime,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Optimizes AI calendar priority mapping",
                        fontSize = 11.sp,
                        color = Color(0xFF4B5573)
                    )
                }

                OutlinedTextField(
                    value = minutesStr,
                    onValueChange = { minutesStr = it.filter { char -> char.isDigit() } },
                    modifier = Modifier
                        .width(90.dp)
                        .testTag("task_input_duration"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = Color(0xFF20233D),
                        focusedContainerColor = Color(0xFF0C0D17),
                        unfocusedContainerColor = Color(0xFF0C0D17),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true,
                    suffix = { Text("m", color = Color(0xFF4B5573)) }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.trim().isNotEmpty()) {
                        val minutes = minutesStr.toIntOrNull() ?: 30
                        if (taskToEdit != null) {
                            onTaskUpdate(
                                taskToEdit.copy(
                                    title = title.trim(),
                                    description = description.trim(),
                                    category = selectedCategory,
                                    urgency = selectedUrgency,
                                    importance = selectedImportance,
                                    estimatedMinutes = minutes
                                )
                            )
                        } else {
                            onTaskAdd(
                                title.trim(),
                                description.trim(),
                                selectedCategory,
                                selectedUrgency,
                                selectedImportance,
                                minutes
                            )
                        }
                    }
                },
                enabled = title.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonLime,
                    contentColor = Color(0xFF090A10),
                    disabledContainerColor = Color(0xFF1E2135),
                    disabledContentColor = Color(0xFF4B5573)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_task_button")
            ) {
                Text(
                    text = if (taskToEdit != null) "Update Task Details" else "Save Productivity Task",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
