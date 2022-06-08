package foo

import plsar.PLSAR
import plsar.annotate.Design
import plsar.annotate.HttpRouter
import plsar.annotate.Inject
import plsar.annotate.Variable
import plsar.annotate.verbs.Get
import plsar.annotate.verbs.Post
import plsar.model.Element
import plsar.model.web.HttpRequest
import plsar.model.web.HttpResponse

@HttpRouter
class PersonRouter {

    @Inject
    var personRepo: PersonRepo? = null

    @Inject
    var todoRepo: TodoRepo? = null

    @Inject
    var support: PLSAR.Support? = null

    @Get("/todos/person/add/{id}")
    @Design("designs/default.htm")
    fun addPersonRender(resp: HttpResponse,
                        @Variable id: Int): String {
        val todo = todoRepo?.get(id)
        val people = personRepo?.list(id)
        resp.set("people", people)
        resp.set("todo", todo)
        return "/pages/person/add.htm"
    }

    @Post("/todos/person/add")
    fun addPerson(req: HttpRequest?,
                  resp: HttpResponse?): String {
        val todoPerson = support?.get(req!!, Person::class.java) as Person
        personRepo!!.save(todoPerson)
        return "[redirect]/todos/person/add/" + todoPerson.todoId
    }

    @Post("/todos/person/delete/{id}")
    fun deletePerson(
        resp: HttpResponse,
        @Variable id: Int?): String {
        personRepo!!.delete(id!!)
        resp.set("message", "successfully deleted person from todo!")
        return "[redirect]/"
    }
}