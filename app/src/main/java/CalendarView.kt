package app

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun CalendarView(
    todos: List<TodoItem>,
    onToggleDone: (TodoItem) -> Unit,
    onDayClick: (List<TodoItem>, String) -> Unit
) {
    val now = System.currentTimeMillis()
    val twentyDaysFromNow = now + (20 * 24 * 60 * 60 * 1000L)


    val overdueTodos = todos.filter { it.deadline < now }
    val upcomingTodos = todos.filter { it.deadline in now..twentyDaysFromNow }
    val futureTodos = todos.filter { it.deadline > twentyDaysFromNow }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CalendarCard(
                category = CalendarCategory.OVERDUE,
                todos = overdueTodos,
                onToggleDone = onToggleDone,
                onCardClick = { onDayClick(overdueTodos, "Overdue Tasks") }
            )
        }

        item {
            CalendarCard(
                category = CalendarCategory.UPCOMING,
                todos = upcomingTodos,
                onToggleDone = onToggleDone,
                onCardClick = { onDayClick(upcomingTodos, "Next 20 Days") }
            )
        }

        item {
            CalendarCard(
                category = CalendarCategory.FUTURE,
                todos = futureTodos,
                onToggleDone = onToggleDone,
                onCardClick = { onDayClick(futureTodos, "20+ Days Away") }
            )
        }
    }
}

@Composable
fun CalendarCard(
    category: CalendarCategory,
    todos: List<TodoItem>,
    onToggleDone: (TodoItem) -> Unit,
    onCardClick: () -> Unit
) {
    var removingItems by remember { mutableStateOf(setOf<String>()) }


    val visibleTodos = todos.filter { !removingItems.contains(it.id) }

    val cardScale by animateFloatAsState(
        targetValue = if (removingItems.isNotEmpty()) 0.98f else 1f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 600,
            easing = androidx.compose.animation.core.EaseInOutCubic
        ),
        label = "card_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(cardScale)
            .clickable { onCardClick() }
            .animateContentSize(
                animationSpec = androidx.compose.animation.core.tween(
                    durationMillis = 500,
                    easing = androidx.compose.animation.core.EaseInOutCubic
                )
            ),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        border = BorderStroke(2.dp, category.color)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(category.color, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = category.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = category.color
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${visibleTodos.size} tasks",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "View details",
                        tint = category.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (visibleTodos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                todos.take(3).forEachIndexed { index, todo ->
                    androidx.compose.animation.AnimatedVisibility(
                        visible = !removingItems.contains(todo.id),
                        enter = slideInHorizontally(
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = 400,
                                easing = androidx.compose.animation.core.EaseOutCubic
                            ),
                            initialOffsetX = { -it }
                        ) + fadeIn(
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = 600,
                                easing = androidx.compose.animation.core.EaseOut
                            )
                        ),
                        exit = slideOutHorizontally(
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = 300,
                                easing = androidx.compose.animation.core.EaseInCubic
                            ),
                            targetOffsetX = { it }
                        ) + fadeOut(
                            animationSpec = androidx.compose.animation.core.tween(
                                durationMillis = 200,
                                easing = androidx.compose.animation.core.EaseIn
                            )
                        )
                    ) {
                        CompactTodoCard(
                            todo = todo,
                            onToggleDone = { item ->
                                removingItems = removingItems + item.id
                                kotlinx.coroutines.GlobalScope.launch {
                                    kotlinx.coroutines.delay(300)
                                    onToggleDone(item)
                                    removingItems = removingItems - item.id
                                }
                            }
                        )
                    }
                    if (index < minOf(2, todos.size - 1) && !removingItems.contains(todo.id)) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (visibleTodos.size > 3) {
                    Text(
                        text = "+${visibleTodos.size - 3} more tasks...",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        modifier = Modifier.padding(start = 32.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No tasks in this category",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(start = 28.dp)
                )
            }
        }
    }
}