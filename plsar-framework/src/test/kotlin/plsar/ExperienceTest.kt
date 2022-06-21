import foo.Person
import foo.PersonRepo
import foo.Todo
import foo.TodoRepo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import plsar.BaseTest
import plsar.model.web.HttpResponse
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExperienceTest : BaseTest() {

    @Test
    fun testIfNotZero(){

        var todoUno = Todo()
        todoUno.title = "Foo"

        val todoRepo = cache?.getElement("todorepo") as TodoRepo
        todoRepo.save(todoUno)

        val todos = todoRepo.list()

        var resp = HttpResponse()
        resp.set("todos", todos)

        val templ = """
            <plsar:namespace var="eos"/>\n
            <eos:if condition=${'$'}{todos.size() > 0}">\n
Boop!
            </eos:if>
            """
        val expects = "Boop!"
        val experienceProcessor = plsar?.experienceProcessor

        var result = experienceProcessor?.process(null, templ, resp, null, null)
        result = result?.trimIndent()?.filter { !it.isWhitespace() }?.replace("[\\n\\t ]", "")

        assertEquals(expects, result)
    }

    @Test
    fun testEach() {

        var todoUno = Todo()
        todoUno.title = "Foo"
        var todoDos = Todo()
        todoDos.title = "Bar"
        var todoTres = Todo()
        todoTres.title = "Baz"

        val todoRepo = cache?.getElement("todorepo") as TodoRepo
        todoRepo.save(todoUno)
        todoRepo.save(todoDos)
        todoRepo.save(todoTres)

        val todos = todoRepo.list()

        var resp = HttpResponse()
        resp.set("todos", todos)

        val experienceProcessor = plsar?.experienceProcessor

        var templ = StringBuilder()
        templ.append("<plsar:namespace var=\"eos\"/>\n")
        templ.append("<eos:each in=\"${'$'}{todos}\" item=\"todo\">\n")
        templ.append("<span>\${todo.title}</span>\n")
        templ.append("</eos:each>")

        val expects = "<span>Foo</span><span>Bar</span><span>Baz</span>"

        var result = experienceProcessor?.process(mutableMapOf(), templ.toString(), resp, null, null)
        result = result?.trimIndent()?.filter { !it.isWhitespace() }?.replace("[\\n\\t ]", "")

        assertEquals(expects, result)
    }


    @Throws()
    @Test
    fun testConditions() {

        var resp = this.create()

        val experienceProcessor = plsar?.experienceProcessor
        var templ = this.getFile("conditions.htm")
        var expects = "3israulisabdulmeetscondition!meetsoppositesize>0!isfoo!isjabar"

        var result = experienceProcessor?.process(mutableMapOf(), templ.toString(), resp, null, null)
        result = result?.trimIndent()?.filter { !it.isWhitespace() }?.replace("[\\n\\t ]", "")

        assertEquals(expects, result)
    }


//    @Test
//    fun testEachSub() {
//        var resp = this.create()
//        var experienceProcessor = plsar?.experienceProcessor
//        var templ = this.getFile("foreach.htm")
//        var expects = ""
//        var result = experienceProcessor?.process(mutableMapOf(), templ.toString(), resp, null, null)
//        assertEquals(expects, result)
//    }



    fun create(): HttpResponse {
        var todoUno = Todo()
        todoUno.title = "Foo"
        var todoDos = Todo()
        todoDos.title = "Bar"
        var todoTres = Todo()
        todoTres.title = "Baz"

        val todoRepo = cache?.getElement("todorepo") as TodoRepo
        val personRepo = cache?.getElement("personrepo") as PersonRepo
        todoRepo.save(todoUno)
        val savedTodo = todoRepo.getSaved()

        var personUne = Person()
        personUne.name = "Alice"
        personUne.todoId = savedTodo?.id
        personUne.isRad = true
        personRepo.save(personUne)

        var personDeux = Person()
        personDeux.name = "Raul"
        personDeux.todoId = savedTodo?.id
        personDeux.isRad = false
        personRepo.save(personDeux)

        val people = personRepo.list(savedTodo?.id)

        todoUno.person = personDeux

        todoRepo.save(todoDos)
        todoRepo.save(todoTres)

        var todos = todoRepo.list()
        todos[0].people = people

        var resp = HttpResponse()
        resp.set("todos", todos)
        resp.set("todo", todoUno)
        resp.set("name", "Abdul")
        resp.set("condition", true)
        resp.set("opposite", false)
        resp.set("person", personUne)
        return resp
    }


    fun getFile(name : String?) : String ? {
        val path = Paths.get("src", "test", "resources", name)
        val file = File(path.toAbsolutePath().toUri())
        val inputStream = FileInputStream(file)
        val scanner = Scanner(inputStream)
        var content = StringBuilder()
        while(scanner.hasNextLine()){
            content.append(scanner.nextLine() + "\n")
        }
        return content.toString()
    }

}