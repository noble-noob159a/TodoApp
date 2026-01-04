package app

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.History
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

@Composable
fun GroupView(
    todos: List<TodoItem>,
    onToggleDone: (TodoItem) -> Unit
) {
    Column {
        if (todos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No todo tasks yet",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                }
            }
        }else
        {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            )
            {
                TodoGroup.values().forEach { group ->
                    val groupTodos = todos.filter { it.group == group }
                    if (groupTodos.isNotEmpty()) {
                        item {
                            GroupCard(
                                group = group,
                                todos = groupTodos,
                                onToggleDone = onToggleDone
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun GroupCard(
    group: TodoGroup,
    todos: List<TodoItem>,
    onToggleDone: (TodoItem) -> Unit
) {
    var removingItems by remember { mutableStateOf(setOf<String>()) }

    val visibleTodos = todos.filter { !removingItems.contains(it.id) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(group.color, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = group.displayName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = group.color
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${visibleTodos.size} items",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            todos.forEachIndexed { index, todo ->
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

                if (todo != visibleTodos.lastOrNull() && !removingItems.contains(todo.id)) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}