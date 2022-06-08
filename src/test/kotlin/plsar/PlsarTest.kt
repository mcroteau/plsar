import example.MockInterceptor
import example.MockFragment
import example.Todo
import example.TodoRepo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import plsar.BaseTest

class PlsarTest : BaseTest() {

    @Test
    fun testRegisterPointcut() {
        plsar?.registerPointcut(MockFragment())
        assertEquals(plsar?.pointcuts?.size, 1)
    }

    @Test
    fun testRegisterInterceptor(){
        plsar?.registerInterceptor(MockInterceptor())
        assertEquals(plsar?.interceptors?.size, 1)
    }

    @Test
    fun testSaveTodo(){
        var todo = Todo()
        todo.title = "Foo"
        val todoRepo = cache?.getElement("todorepo") as TodoRepo
        todoRepo.save(todo)

        val todos = todoRepo.list()
        assertEquals(todos.size, 1)
    }

    @Test
    fun testList(){
        var todo = Todo()
        todo.title = "Foo"
        val todoRepo = cache?.getElement("todorepo") as TodoRepo
        todoRepo.save(todo)

        val todos = todoRepo?.list()
        assertEquals(todos?.size, 1)
    }

    @Test
    fun testDelete(){
        var todo = Todo()
        todo.title = "Foo"
        val todoRepo = cache?.getElement("todorepo") as TodoRepo
        todoRepo.save(todo)

        todoRepo.delete(1)
        val todos = todoRepo.list()
        assertEquals(todos.size, 0)
    }

    @Test
    fun testGetCache(){
        assertNotNull(cache)
    }


}