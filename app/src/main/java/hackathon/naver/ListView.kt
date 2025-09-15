package hackathon.naver

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
@Composable
fun ListView(
    todos: List<TodoItem>,
    onToggleDone: (TodoItem) -> Unit,
    onDelete: (TodoItem) -> Unit,
    onEdit: ((TodoItem) -> Unit)? = null
) {
    var removingItems by remember { mutableStateOf(setOf<String>()) }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = todos.sortedBy { it.deadline },
                key = { it.id }
            ) { todo ->
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
                            durationMillis = 500,
                            easing = androidx.compose.animation.core.EaseOut
                        )
                    ),
                    exit = slideOutHorizontally(
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 350,
                            easing = androidx.compose.animation.core.EaseInCubic
                        ),
                        targetOffsetX = { it }
                    ) + fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(
                            durationMillis = 300,
                            easing = androidx.compose.animation.core.EaseIn
                        )
                    )
                ) {
                    TodoCard(
                        todo = todo,
                        onToggleDone = { item ->
                            removingItems = removingItems + item.id
                            kotlinx.coroutines.GlobalScope.launch {
                                kotlinx.coroutines.delay(350)
                                onToggleDone(item)
                            }
                        },
                        onDelete = { item ->
                            removingItems = removingItems + item.id

                            kotlinx.coroutines.GlobalScope.launch {
                                kotlinx.coroutines.delay(350)
                                onDelete(item)
                            }
                        },
                        onEdit = onEdit
                    )
                }
            }
        }
        }
    }
}