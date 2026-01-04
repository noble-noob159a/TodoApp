package app

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

data class TodoItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val deadline: Long,
    val group: TodoGroup,
    val priority: Priority,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

enum class ViewType {
    LIST, GROUP, CALENDAR, HISTORY
}

enum class TodoGroup(val displayName: String, val color: Color) {
    SCHOOL("School", Color(0xFF50C878)),
    WORK("Work", Color(0xFF00416A)),
    HOUSEWORK("Housework", Color(0xFF00BCD4)),
    MEETING("Meeting", Color(0xFF9C27B0)),
    OTHER("Other", Color(0xFF696969))
}

enum class Priority(val level: Int, val displayName: String, val color: Color) {
    LOW(1, "Low", Color(0xFF8BC34A)),
    MEDIUM(2, "Medium", Color(0xFFFFC107)),
    HIGH(3, "High", Color(0xFFFF5722))
}


enum class CalendarCategory(val title: String, val color: Color) {
    OVERDUE("Overdue Tasks", Color(0xFFD32F2F)),
    UPCOMING("Next 20 Days", Color(0xFF1976D2)),
    FUTURE("20+ Days Away", Color(0xFF388E3C))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    currentView: ViewType,
    selectedDayTodos: List<TodoItem>?,
    selectedDayTitle: String,
    onViewChange: (ViewType) -> Unit,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectedDayTodos != null) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
                Text(
                    when {
                        selectedDayTodos != null -> selectedDayTitle
                        currentView == ViewType.HISTORY -> "📚 History"
                        else -> "📌 Todo App <3"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (currentView == ViewType.HISTORY) Color(0xFF8E24AA) else Color(
                0xFF6200EA
            )
        ),
        actions = {
            if (selectedDayTodos == null) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("List View")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { onViewChange(ViewType.LIST) }) {
                        Icon(
                            Icons.Default.Checklist,
                            contentDescription = "List View",
                            tint = Color.White
                        )
                    }
                }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("Group View")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { onViewChange(ViewType.GROUP) }) {
                        Icon(
                            Icons.Default.Dashboard,
                            contentDescription = "Group View",
                            tint = Color.White
                        )
                    }
                }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("Calendar View")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { onViewChange(ViewType.CALENDAR) }) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "Calendar View",
                            tint = Color.White
                        )
                    }
                }
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("History View")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    IconButton(onClick = { onViewChange(ViewType.HISTORY) }) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "History View",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun TodoCard(
    todo: TodoItem,
    onToggleDone: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit,
    onEdit: ((TodoItem) -> Unit)? = null,
    showRestore: Boolean = false,
    onRestore: ((TodoItem) -> Unit)? = null
) {
    val urgencyColor = getUrgencyColor(todo.deadline)

    val scale by animateFloatAsState(
        targetValue = if (todo.isDone) 0.92f else 1f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 400,
            easing = androidx.compose.animation.core.EaseInOutCubic
        ),
        label = "card_scale"
    )

    val cardColor by animateColorAsState(
        targetValue = if (todo.isDone) Color(0xFFF0F0F0) else Color.White,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 300,
            easing = androidx.compose.animation.core.EaseInOut
        ),
        label = "card_color"
    )

    val borderColor by animateColorAsState(
        targetValue = if (showRestore) Color(0xFF8E24AA) else urgencyColor,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 200,
            easing = androidx.compose.animation.core.EaseInOut
        ),
        label = "border_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .animateContentSize(
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 250,
                    easing = androidx.compose.animation.core.EaseInOutCubic
                )
            ),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(if (todo.isDone) 2.dp else 6.dp),
        border = BorderStroke(width = 2.dp, color = borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!showRestore) {
                Checkbox(
                    checked = todo.isDone,
                    onCheckedChange = { onToggleDone(todo) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF4CAF50),
                        uncheckedColor = urgencyColor
                    )
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                AnimatedContent(
                    targetState = todo.isDone,
                    transitionSpec = {
                        fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(200)
                        ) togetherWith fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(200)
                        )
                    },
                    label = "text_animation"
                ) { isDone ->
                    Text(
                        text = todo.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (isDone) TextDecoration.LineThrough else null,
                        color = if (isDone) Color.Gray else Color.Black
                    )
                }

                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Chip(
                        text = todo.group.displayName,
                        backgroundColor = todo.group.color.copy(alpha = 0.2f),
                        textColor = todo.group.color
                    )
                    Chip(
                        text = todo.priority.displayName,
                        backgroundColor = todo.priority.color.copy(alpha = 0.2f),
                        textColor = todo.priority.color
                    )
                }
                Text(
                    text = if (showRestore && todo.completedAt != null) {
                        "Completed: ${formatDate(todo.completedAt)}"
                    } else {
                        "Due: ${formatDate(todo.deadline)}"
                    },
                    fontSize = 12.sp,
                    color = if (showRestore) Color(0xFF8E24AA) else urgencyColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row {
                if (onEdit != null && !showRestore) {
                    var isEditHovered by remember { mutableStateOf(false) }
                    val editScale by animateFloatAsState(
                        targetValue = if (isEditHovered) 1.1f else 1f,
                        animationSpec = androidx.compose.animation.core.tween(100),
                        label = "edit_scale"
                    )

                    IconButton(
                        onClick = { onEdit(todo) },
                        modifier = Modifier.scale(editScale)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF2196F3)
                        )
                    }
                }

                if (showRestore && onRestore != null) {
                    var isRestoreHovered by remember { mutableStateOf(false) }
                    val restoreScale by animateFloatAsState(
                        targetValue = if (isRestoreHovered) 1.1f else 1f,
                        animationSpec = androidx.compose.animation.core.tween(100),
                        label = "restore_scale"
                    )

                    IconButton(
                        onClick = { onRestore(todo) },
                        modifier = Modifier.scale(restoreScale)
                    ) {
                        Icon(
                            Icons.Default.Restore,
                            contentDescription = "Restore",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                } else {
                    var isDeleteHovered by remember { mutableStateOf(false) }
                    val deleteScale by animateFloatAsState(
                        targetValue = if (isDeleteHovered) 1.15f else 1f,
                        animationSpec = androidx.compose.animation.core.spring(
                            dampingRatio = 0.6f,
                            stiffness = androidx.compose.animation.core.Spring.StiffnessMedium
                        ),
                        label = "delete_scale"
                    )

                    val deleteColor by animateColorAsState(
                        targetValue = if (isDeleteHovered) Color(0xFFD32F2F) else Color(0xFFE91E63),
                        animationSpec = androidx.compose.animation.core.tween(150),
                        label = "delete_color"
                    )

                    IconButton(
                        onClick = { onDelete(todo) },
                        modifier = Modifier.scale(deleteScale)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = deleteColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactTodoCard(
    todo: TodoItem,
    onToggleDone: (TodoItem) -> Unit
) {
    val urgencyColor = getUrgencyColor(todo.deadline)


    val scale by animateFloatAsState(
        targetValue = if (todo.isDone) 0.95f else 1f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 200,
            easing = androidx.compose.animation.core.EaseInOut
        ),
        label = "compact_item_scale"
    )


    val backgroundColor by animateColorAsState(
        targetValue = if (todo.isDone) {
            urgencyColor.copy(alpha = 0.05f)
        } else {
            urgencyColor.copy(alpha = 0.1f)
        },
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 300,
            easing = androidx.compose.animation.core.EaseInOut
        ),
        label = "compact_background_color"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(8.dp)
            .animateContentSize(
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 300,
                    easing = androidx.compose.animation.core.EaseInOut
                )
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        AnimatedContent(
            targetState = todo.isDone,
            transitionSpec = {
                fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(200)
                ) togetherWith fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(200)
                )
            },
            label = "checkbox_animation"
        ) { isDone ->
            Checkbox(
                checked = isDone,
                onCheckedChange = { onToggleDone(todo) },
                modifier = Modifier.scale(0.8f),
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF4CAF50),
                    uncheckedColor = todo.priority.color
                )
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {

            AnimatedContent(
                targetState = todo.isDone,
                transitionSpec = {
                    fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(200)
                    ) togetherWith fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(200)
                    )
                },
                label = "text_animation"
            ) { isDone ->
                Text(
                    text = todo.name,
                    fontSize = 14.sp,
                    color = if (isDone) Color.Gray else Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (isDone) TextDecoration.LineThrough else null
                )
            }
        }


        val priorityAlpha by animateFloatAsState(
            targetValue = if (todo.isDone) 0.5f else 1f,
            animationSpec = androidx.compose.animation.core.tween(300),
            label = "priority_alpha"
        )

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    todo.priority.color.copy(alpha = priorityAlpha),
                    RoundedCornerShape(50)
                )
        )
    }
}

@Composable
fun Chip(
    text: String,
    backgroundColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}
