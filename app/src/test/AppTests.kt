package app

import org.junit.Test
import org.junit.Assert.*
import app.TodoItem
import app.TodoGroup
import app.Priority
import app.getUrgencyColor
import java.util.UUID

/**
 * Class kiểm thử tập trung vào các logic nghiệp vụ (Business Logic)
 * và các hàm tiện ích (Utility Functions) của ứng dụng.
 */
class AppTests {
    @Test
    fun testCreateTodoItem_StoresDataCorrectly() {    
        val now = System.currentTimeMillis()
        val name = "Complete Assignment"
        val group = TodoGroup.SCHOOL
        val priority = Priority.HIGH

      
        val todo = TodoItem(
            name = name,
            deadline = now,
            group = group,
            priority = priority
        )

      
        assertEquals("Tên task không khớp", name, todo.name)
        assertEquals("Nhóm không khớp", group, todo.group)
        assertEquals("Độ ưu tiên không khớp", priority, todo.priority)
        assertFalse("Mặc định isDone phải là false", todo.isDone)
        assertNotNull("ID không được null", todo.id)
    }

    @Test
    fun testUrgencyColor_PastDeadline_ReturnsRed() { 
        val pastDeadline = System.currentTimeMillis() - (24 * 60 * 60 * 1000)

        val color = getUrgencyColor(pastDeadline)
        val expectedColorValue = 0xFFD32F2F.toULong() 
        assertEquals(expectedColorValue, color.value)
    }

    @Test
    fun testUrgencyColor_FutureDeadline_ReturnsGreen() {
        val futureDeadline = System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000)
        val color = getUrgencyColor(futureDeadline)
        val expectedColorValue = 0xFF4CAF50.toULong()
        assertEquals(expectedColorValue, color.value)
    }


    @Test
    fun testSearch_FilterByName_ReturnsCorrectResults() {
        val todo1 = TodoItem(name = "Buy Milk", deadline = 0, group = TodoGroup.OTHER, priority = Priority.LOW)
        val todo2 = TodoItem(name = "Do Math Homework", deadline = 0, group = TodoGroup.SCHOOL, priority = Priority.HIGH)
        val todo3 = TodoItem(name = "Meeting with Boss", deadline = 0, group = TodoGroup.WORK, priority = Priority.MEDIUM)
        
        val allTodos = listOf(todo1, todo2, todo3)
        val searchQuery = "Math"

        val filteredList = allTodos.filter { 
            it.name.contains(searchQuery, ignoreCase = true) 
        }

        assertEquals(1, filteredList.size)
        assertEquals("Do Math Homework", filteredList[0].name)
    }

    @Test
    fun testSearch_CaseInsensitive() {
        val todo = TodoItem(name = "CLEAN HOUSE", deadline = 0, group = TodoGroup.HOUSEWORK, priority = Priority.LOW)
        val list = listOf(todo)
        val query = "clean" 

        val result = list.filter { it.name.contains(query, ignoreCase = true) }

        assertEquals(1, result.size)
    }
    @Test
    fun testSystem_CompleteTaskFlow_MovesItemToHistory() {
        val task = TodoItem(name = "System Test Task", deadline = 0, group = TodoGroup.WORK, priority = Priority.MEDIUM)
        var currentTodos = listOf(task)
        var historyTodos = listOf<TodoItem>()

        val updatedTask = task.copy(
            isDone = true,
            completedAt = System.currentTimeMillis()
        )

        currentTodos = currentTodos.filter { it.id != task.id }

        historyTodos = listOf(updatedTask) + historyTodos

        assertEquals("Task phải bị xóa khỏi danh sách Todo", 0, currentTodos.size)
        assertEquals("Task phải xuất hiện trong History", 1, historyTodos.size)
        assertTrue("Task trong History phải có trạng thái isDone = true", historyTodos[0].isDone)
        assertEquals("Tên task trong History phải khớp", "System Test Task", historyTodos[0].name)
    }
}