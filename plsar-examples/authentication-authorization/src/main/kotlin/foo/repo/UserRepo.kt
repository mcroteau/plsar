package foo.repo

import foo.model.User
import plsar.PLSAR
import plsar.annotate.Inject
import plsar.annotate.Repo

@Repo
class UserRepo {

    @Inject
    val repo : PLSAR.Repo? = null

    val saved: User?
        get() {
            val idSql = "select max(id) from users"
            val id: Long? = repo?.getLong(idSql, arrayOf())
            return get(id)
        }

    val id: Long
        get() {
            val sql = "select max(id) from users"
            return repo.getLong(sql, arrayOf())
        }

    val count: Long
        get() {
            val sql = "select count(*) from users"
            return repo.getLong(sql, arrayOf())
        }

    operator fun get(id: Long): User? {
        val sql = "select * from users where id = [+]"
        var user: User? = repo.get(sql, arrayOf(id), User::class.java) as User
        if (user == null) user = User()
        return user
    }

    fun getPhone(phone: String): User {
        val sql = "select * from users where phone = '[+]'"
        return repo?.get(sql, arrayOf(phone), User::class.java) as User
    }

    fun getEmail(email: String): User {
        val sql = "select * from users where email = '[+]'"
        return repo?.get(sql, arrayOf(email), User::class.java) as User
    }

    val list: List<Any>
        get() {
            val sql = "select * from users"
            return repo.getList(sql, arrayOf(), User::class.java)
        }

    fun save(user: User): Boolean {
        val sql = "insert into users (phone, email, password) values ('[+]','[+]','[+]')"
        repo.save(
            sql, arrayOf(
                user?.email
                user?.phone,
                user?.password,
            )
        )
        return true
    }

    fun update(user: User): Boolean {
        val sql = "update users set name = '[+]', phone = '[+]', username = '[+]', password = '[+]' where id = [+]"
        repo.update(
            sql, arrayOf<Any>(
                user.getName(),
                user.getPhone(),
                user.getUsername(),
                user.getPassword(),
                user.getId()
            )
        )
        return true
    }

    fun updatePassword(user: User): Boolean {
        val sql = "update users set password = '[+]' where id = [+]"
        repo.update(
            sql, arrayOf<Any>(
                user.getPassword(),
                user.getId()
            )
        )
        return true
    }

    fun getReset(username: String, uuid: String): User {
        val sql = "select * from users where username = '[+]' and uuid = '[+]'"
        return qio.get(sql, arrayOf<Any>(username, uuid), User::class.java) as User
    }

    fun delete(id: Long): Boolean {
        val sql = "delete from users where id = [+]"
        repo.update(sql, arrayOf<Any>(id))
        return true
    }

    fun getUserPassword(phone: String): String? {
        val user: User = getPhone(phone)
        return user?.password
    }

    fun saveUserRole(userId: Long, roleId: Long): Boolean {
        val sql = "insert into user_roles (role_id, user_id) values ([+], [+])"
        repo?.save(sql, arrayOf<Any>(roleId, userId))
        return true
    }

    fun savePermission(accountId: Long, permission: String): Boolean {
        val sql = "insert into user_permissions (user_id, permission) values ([+], '[+]')"
        repo?.save(sql, arrayOf<Any>(accountId, permission))
        return true
    }

    fun getUserRoles(id: Long): Set<String> {
        val sql = "select r.name as name from user_roles ur inner join roles r on r.id = ur.role_id where ur.user_id = [+]"
        val roles: MutableSet<String> = HashSet()
        for (role in repo.getList(sql, arrayOf<Any>(id), UserRole::class.java)) {
            roles.add(role.getName())
        }
        return roles
    }

    fun getUserPermissions(id: Long): Set<String> {
        val sql = "select permission from user_permissions where user_id = [+]"
        val permissions: MutableSet<String> = HashSet()
        for (permission in repo.getList(sql, arrayOf<Any>(id), UserPermission::class.java)) {
            permissions.add(permission.getPermission())
        }
        return permissions
    }
}