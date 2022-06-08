package foo

import plsar.PLSAR
import plsar.annotate.Inject
import plsar.annotate.Repo

@Repo
class PersonRepo {

    @Inject
    val repo : PLSAR.Repo ? = null

    fun list(id: Int?): List<Person> {
        val sql = "select * from todo_people where todo_id = [+]"
        return repo?.getList(sql, arrayOf<Any?>(id), Person::class.java) as ArrayList<Person>
    }

    fun save(person: Person) {
        val sql = "insert into todo_people (todo_id, name) values ([+],'[+]')"
        repo?.save(
            sql, arrayOf<Any?>(
                person.todoId,
                person.name
            )
        )
    }

    fun delete(id: Int?) {
        val sql = "delete from todo_people where id = [+]"
        repo?.delete(sql, arrayOf<Any?>(id))
    }

}