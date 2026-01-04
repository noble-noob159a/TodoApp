package app

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.collections.plus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp() {
    val context = LocalContext.current
    var todos by remember { mutableStateOf(loadTodos(context)) }
    var historyTodos by remember { mutableStateOf(loadHistoryTodos(context)) }
    var currentView by remember { mutableStateOf(ViewType.LIST) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf<TodoItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedDayTodos by remember { mutableStateOf<List<TodoItem>?>(null) }
    var selectedDayTitle by remember { mutableStateOf("") }

    LaunchedEffect(todos, historyTodos) {
        saveTodos(context, todos)
        saveHistoryTodos(context, historyTodos)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                currentView = currentView,
                selectedDayTodos = selectedDayTodos,
                selectedDayTitle = selectedDayTitle,
                onViewChange = { newView -> currentView = newView },
                onBackClick = { selectedDayTodos = null }
            )
        },
        floatingActionButton = {
            if (currentView != ViewType.HISTORY) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Color(0xFFFF4081),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Todo")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = if (currentView == ViewType.HISTORY) {
                            listOf(Color(0xFFE1BEE7), Color(0xFFF3E5F5))
                        } else {
                            listOf(Color(0xFFF5F5F5), Color(0xFFE8EAF6))
                        }
                    )
                )
        ) {
            if (selectedDayTodos != null) {
                ListView(
                    todos = selectedDayTodos!!,
                    onToggleDone = { todo ->
                        val updatedTodo = todo.copy(
                            isDone = !todo.isDone,
                            completedAt = if (!todo.isDone) System.currentTimeMillis() else null
                        )
                        todos = todos.map {
                            if (it.id == todo.id) updatedTodo else it
                        }

                        if (updatedTodo.isDone) {

                            historyTodos = (listOf(updatedTodo) + historyTodos).take(100)
                            todos = todos.filter { it.id != todo.id }
                        }

                        selectedDayTodos = selectedDayTodos!!.map {
                            if (it.id == todo.id) updatedTodo else it
                        }.filter { !it.isDone }
                    },
                    onDelete = { todo ->
                        val deletedTodo = todo.copy(isDone = true, completedAt = System.currentTimeMillis())
                        historyTodos = (listOf(deletedTodo) + historyTodos).take(100)
                        todos = todos.filter { it.id != todo.id }
                        selectedDayTodos = selectedDayTodos!!.filter { it.id != todo.id }
                    },
                    onEdit = { todo -> showEditDialog = todo }
                )
            } else {
                if (currentView != ViewType.HISTORY) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search todos...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6200EA),
                            unfocusedBorderColor = Color(0xFFBDBDBD)
                        )
                    )
                }

                val filteredTodos = todos.filter {
                    it.name.contains(searchQuery, ignoreCase = true) && !it.isDone
                }
                val filteredHistoryTodos = historyTodos.filter {
                    it.name.contains(searchQuery, ignoreCase = true)
                }

                when (currentView) {
                    ViewType.LIST -> ListView(
                        todos = filteredTodos,
                        onToggleDone = { todo ->
                            val updatedTodo = todo.copy(
                                isDone = true,
                                completedAt = System.currentTimeMillis()
                            )
                            historyTodos = (listOf(updatedTodo) + historyTodos).take(100)
                            todos = todos.filter { it.id != todo.id }
                        },
                        onDelete = { todo ->
                            val deletedTodo = todo.copy(isDone = true, completedAt = System.currentTimeMillis())
                            historyTodos = (listOf(deletedTodo) + historyTodos).take(100)
                            todos = todos.filter { it.id != todo.id }
                        },
                        onEdit = { todo -> showEditDialog = todo }
                    )
                    ViewType.GROUP -> GroupView(
                        todos = filteredTodos,
                        onToggleDone = { todo ->
                            val updatedTodo = todo.copy(
                                isDone = true,
                                completedAt = System.currentTimeMillis()
                            )
                            historyTodos = (listOf(updatedTodo) + historyTodos).take(100)
                            todos = todos.filter { it.id != todo.id }
                        }
                    )
                    ViewType.CALENDAR -> CalendarView(
                        todos = filteredTodos,
                        onToggleDone = { todo ->
                            val updatedTodo = todo.copy(
                                isDone = true,
                                completedAt = System.currentTimeMillis()
                            )
                            historyTodos = (listOf(updatedTodo) + historyTodos).take(100)
                            todos = todos.filter { it.id != todo.id }
                        },
                        onDayClick = { dayTodos, title ->
                            selectedDayTodos = dayTodos.filter { !it.isDone }
                            selectedDayTitle = title
                        }
                    )
                    ViewType.HISTORY -> HistoryView(
                        historyTodos = if (searchQuery.isNotEmpty()) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                label = { Text("Search history...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF8E24AA),
                                    unfocusedBorderColor = Color(0xFFBDBDBD)
                                )
                            )
                            filteredHistoryTodos
                        } else {
                            filteredHistoryTodos
                        },
                        onRestore = { todo ->
                            val restoredTodo = todo.copy(isDone = false, completedAt = null)
                            todos = todos + restoredTodo
                            historyTodos = historyTodos.filter { it.id != todo.id }
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTodoDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { todo ->
                todos = todos + todo
                showAddDialog = false
            }
        )
    }

    showEditDialog?.let { todo ->
        EditTodoDialog(
            todo = todo,
            onDismiss = { showEditDialog = null },
            onSave = { updatedTodo ->
                todos = todos.map {
                    if (it.id == todo.id) updatedTodo else it
                }
                selectedDayTodos = selectedDayTodos?.map {
                    if (it.id == todo.id) updatedTodo else it
                }
                showEditDialog = null
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoDialog(
    onDismiss: () -> Unit,
    onAdd: (TodoItem) -> Unit
) {
    TodoFormDialog(
        todo = null,
        onDismiss = onDismiss,
        onSave = onAdd
    )
}

@Composable
fun EditTodoDialog(
    todo: TodoItem,
    onDismiss: () -> Unit,
    onSave: (TodoItem) -> Unit
) {
    TodoFormDialog(
        todo = todo,
        onDismiss = onDismiss,
        onSave = onSave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoFormDialog(
    todo: TodoItem? = null,
    onDismiss: () -> Unit,
    onSave: (TodoItem) -> Unit
) {
    val isEditing = todo != null

    var name by remember { mutableStateOf(todo?.name ?: "") }
    var selectedGroup by remember { mutableStateOf(todo?.group ?: TodoGroup.OTHER) }
    var selectedPriority by remember { mutableStateOf(todo?.priority ?: Priority.MEDIUM) }
    var selectedDate by remember {
        mutableStateOf(
            Calendar.getInstance().apply {
                timeInMillis = todo?.deadline ?: System.currentTimeMillis()
            }
        )
    }

    val context = LocalContext.current
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.timeInMillis
    )
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val dateMillis = datePickerState.selectedDateMillis
                    if (dateMillis != null) {
                        val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }

                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                calendar.set(Calendar.HOUR_OF_DAY, hour)
                                calendar.set(Calendar.MINUTE, minute)
                                selectedDate = calendar
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true
                        ).show()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val dialogColor = if (isEditing) Color(0xFF2196F3) else Color(0xFF6200EA)
    val dialogTitle = if (isEditing) "✏️ Edit Todo" else "✨ Add New Todo"
    val buttonText = if (isEditing) "Save Changes" else "Add Todo"
    val buttonColor = if (isEditing) Color(0xFF2196F3) else Color(0xFFFF4081)
    val textFieldIcon = if (isEditing) Icons.Default.Edit else Icons.Default.Create

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                dialogTitle,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = dialogColor
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Task Name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = dialogColor
                    ),
                    trailingIcon = { Icon(textFieldIcon, contentDescription = null) }
                )


                Text("Group", fontWeight = FontWeight.Medium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TodoGroup.values().toList()) { group ->
                        FilterChip(
                            selected = selectedGroup == group,
                            onClick = { selectedGroup = group },
                            label = { Text(group.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = group.color.copy(alpha = 0.3f)
                            )
                        )
                    }
                }


                Text("Priority", fontWeight = FontWeight.Medium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Priority.values().toList()) { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = priority.color.copy(alpha = 0.3f)
                            )
                        )
                    }
                }


                Text("Deadline", fontWeight = FontWeight.Medium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDate(selectedDate.timeInMillis),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Pick deadline"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val todoItem = if (isEditing) {
                            todo!!.copy(
                                name = name,
                                deadline = selectedDate.timeInMillis,
                                group = selectedGroup,
                                priority = selectedPriority
                            )
                        } else {
                            TodoItem(
                                name = name,
                                deadline = selectedDate.timeInMillis,
                                group = selectedGroup,
                                priority = selectedPriority
                            )
                        }
                        onSave(todoItem)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor
                )
            ) {
                Text(buttonText, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
@SuppressLint("SimpleDateFormat")
fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm")
    return sdf.format(Date(millis))
}

fun getUrgencyColor(deadline: Long): Color {
    val now = System.currentTimeMillis()
    val daysUntilDeadline = ((deadline - now) / (24 * 60 * 60 * 1000)).toInt()

    return when {
        daysUntilDeadline < 0 -> Color(0xFFD32F2F) // Past due - red
        daysUntilDeadline == 0 -> Color(0xFFE91E63) // Due today - pink red
        daysUntilDeadline == 1 -> Color(0xFFFF5722) // Tomorrow - orange red
        daysUntilDeadline <= 3 -> Color(0xFFFF9800) // 2-3 days - orange
        daysUntilDeadline <= 7 -> Color(0xFFFFD700) // Week - yellow
        else -> Color(0xFF4CAF50) // More than a week - green
    }
}

fun saveHistoryTodos(context: Context, historyTodos: List<TodoItem>) {
    try {
        val file = File(context.filesDir, "history_todos.txt")
        val content = historyTodos.joinToString("\n") { todo ->
            "${todo.id}|${todo.name}|${todo.deadline}|${todo.group.name}|${todo.priority.name}|${todo.isDone}|${todo.createdAt}|${todo.completedAt ?: ""}"
        }
        file.writeText(content)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadHistoryTodos(context: Context): List<TodoItem> {
    return try {
        val file = File(context.filesDir, "history_todos.txt")
        if (file.exists()) {
            file.readLines().mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size >= 7) {
                    TodoItem(
                        id = parts[0],
                        name = parts[1],
                        deadline = parts[2].toLongOrNull() ?: System.currentTimeMillis(),
                        group = TodoGroup.valueOf(parts[3]),
                        priority = Priority.valueOf(parts[4]),
                        isDone = parts[5].toBoolean(),
                        createdAt = parts[6].toLongOrNull() ?: System.currentTimeMillis(),
                        completedAt = if (parts.size > 7 && parts[7].isNotBlank()) {
                            parts[7].toLongOrNull()
                        } else null
                    )
                } else null
            }
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

fun saveTodos(context: Context, todos: List<TodoItem>) {
    try {
        val file = File(context.filesDir, "todos.txt")
        val content = todos.joinToString("\n") { todo ->
            "${todo.id}|${todo.name}|${todo.deadline}|${todo.group.name}|${todo.priority.name}|${todo.isDone}|${todo.createdAt}"
        }
        file.writeText(content)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadTodos(context: Context): List<TodoItem> {
    return try {
        val file = File(context.filesDir, "todos.txt")
        if (file.exists()) {
            file.readLines().mapNotNull { line ->
                val parts = line.split("|")
                if (parts.size == 7) {
                    TodoItem(
                        id = parts[0],
                        name = parts[1],
                        deadline = parts[2].toLongOrNull() ?: System.currentTimeMillis(),
                        group = TodoGroup.valueOf(parts[3]),
                        priority = Priority.valueOf(parts[4]),
                        isDone = parts[5].toBoolean(),
                        createdAt = parts[6].toLongOrNull() ?: System.currentTimeMillis()
                    )
                } else null
            }
        } else {
            // Generate sample data for demonstration
            generateSampleTodos()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        generateSampleTodos()
    }
}

fun generateSampleTodos(): List<TodoItem> {
    val now = System.currentTimeMillis()
    val day = 24 * 60 * 60 * 1000L

    return listOf(
        TodoItem("1", "Complete math homework", now + day, TodoGroup.SCHOOL, Priority.HIGH, false),
        TodoItem("2", "Team meeting preparation", now + 2 * day, TodoGroup.MEETING, Priority.LOW, false),
        TodoItem("3", "Buy groceries", now + 3 * day, TodoGroup.HOUSEWORK, Priority.MEDIUM, false),
        TodoItem("4", "Project deadline", now + 5 * day, TodoGroup.WORK, Priority.LOW, false),
        TodoItem("5", "Clean the house", now + 7 * day, TodoGroup.HOUSEWORK, Priority.LOW, true),
        TodoItem("6", "Study for exam", now + 4 * day, TodoGroup.SCHOOL, Priority.HIGH, false),
        TodoItem("7", "Client presentation", now + 6 * day, TodoGroup.WORK, Priority.LOW, false),
        TodoItem("8", "Doctor appointment", now + 2 * day, TodoGroup.OTHER, Priority.HIGH, false),
        TodoItem("9", "Pay bills", now + day, TodoGroup.HOUSEWORK, Priority.HIGH, false)
    )
}