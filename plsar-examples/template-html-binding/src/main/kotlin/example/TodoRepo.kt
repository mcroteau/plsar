package example

import plsar.PLSAR
import plsar.annotate.Repo
import plsar.annotate.Inject

@Repo
class TodoRepo {

    @Inject
    var repo: PLSAR.Repo? = null

    fun count(): Long? {
        val sql = "select count(*) from todos"
        return repo?.getLong(sql, arrayOf<Any?>())
    }

    fun getSaved(): Todo? {
        val idSql = "select max(id) from todos"
        val id = repo?.getLong(idSql, arrayOf())
        val sql = "select * from todos where id = [+]"
        var todo = repo?.get(sql, arrayOf(id), Todo::class.java) as Todo
        return todo
    }

    fun get(id: Int): Todo {
        val sql = "select * from todos where id = [+]"
        return repo?.get(sql, arrayOf<Any?>(id), Todo::class.java) as Todo
    }

    fun list(): List<Todo> {
        val sql = "select * from todos"
        return repo?.getList(sql, arrayOf<Any?>(), Todo::class.java) as ArrayList<Todo>
    }

    fun save(todo: Todo) {
        val sql = "insert into todos (title) values ('[+]')"
        repo?.save(
            sql, arrayOf<Any?>(
                todo.title
            )
        )
    }

    fun update(todo: Todo?) {
        val sql = "update todos set title = '[+]', complete = [+] where id = [+]"
        repo?.update(
            sql, arrayOf<Any?>(
                todo?.title,
                todo!!.isComplete,
                todo.id
            )
        )
    }

    fun delete(id: Int) {
        val sql = "delete from todos where id = [+]"
        repo?.delete(sql, arrayOf<Any?>(id))
    }

}