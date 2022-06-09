package foo.repo

import foo.model.User
import foo.model.UserPermission
import foo.model.UserRole
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
            val id: Int? = repo?.getInt(idSql, arrayOf())
            return get(id)
        }

    val id: Int ?
        get() {
            val sql = "select max(id) from users"
            return repo!!.getInt(sql, arrayOf())
        }

    val count: Int ?
        get() {
            val sql = "select count(*) from users"
            return repo!!.getInt(sql, arrayOf())
        }

    operator fun get(id: Int?): User? {
        val sql = "select * from users where id = [+]"
        var user: User? = repo!!.get(sql, arrayOf(id), User::class.java) as User
        if (user == null) user = User()
        return user
    }

    fun getPhone(phone: String?): User {
        val sql = "select * from users where phone = '[+]'"
        return repo?.get(sql, arrayOf(phone), User::class.java) as User
    }

    fun getEmail(email: String?): User {
        val sql = "select * from users where email = '[+]'"
        return repo?.get(sql, arrayOf(email), User::class.java) as User
    }

    val list: List<Any?>
        get() {
            val sql = "select * from users"
            return repo!!.getList(sql, arrayOf(), User::class.java)
        }

    fun save(user: User): Boolean {
        val sql = "insert into users (phone, email, password) values ('[+]','[+]','[+]')"
        repo?.save(
            sql, arrayOf(
                user?.phone,
                user?.email,
                user?.password,
            )
        )
        return true
    }

    fun update(user: User): Boolean {
        val sql = "update users set phone = '[+]', username = '[+]', password = '[+]' where id = [+]"
        repo?.update(
            sql, arrayOf(
                user.phone,
                user.email,
                user.password,
                user.id
            )
        )
        return true
    }

    fun updatePassword(user: User): Boolean {
        val sql = "update users set password = '[+]' where id = [+]"
        repo?.update(
            sql, arrayOf(
                user.password,
                user.id
            )
        )
        return true
    }

    fun getReset(username: String, uuid: String): User {
        val sql = "select * from users where username = '[+]' and uuid = '[+]'"
        return repo?.get(sql, arrayOf(username, uuid), User::class.java) as User
    }

    fun delete(id: Int): Boolean {
        val sql = "delete from users where id = [+]"
        repo?.update(sql, arrayOf(id))
        return true
    }

    fun getUserPassword(phone: String): String? {
        val user: User = getPhone(phone)
        return user?.password
    }

    fun saveUserRole(userId: Int, roleId: Int): Boolean {
        val sql = "insert into user_roles (role_id, user_id) values ([+], [+])"
        repo?.save(sql, arrayOf(roleId, userId))
        return true
    }

    fun savePermission(userId: Int, permission: String): Boolean {
        val sql = "insert into user_permissions (user_id, permission) values ([+], '[+]')"
        repo?.save(sql, arrayOf(userId, permission))
        return true
    }

    fun getUserRoles(id: Int?): Set<String?> {
        val sql = "select r.name as name from user_roles ur inner join roles r on r.id = ur.role_id where ur.user_id = [+]"
        val roles: MutableSet<String?> = HashSet()
        val userRoles = repo!!.getList(sql, arrayOf(id), UserRole::class.java) as MutableList<UserRole>
        for (role in userRoles) {
            roles.add(role.name)
        }
        return roles
    }

    fun getUserPermissions(id: Int?): Set<String?> {
        val sql = "select permission from user_permissions where user_id = [+]"
        val permissions: MutableSet<String?> = HashSet()
        val userPermissions = repo!!.getList(sql, arrayOf(id), UserPermission::class.java) as MutableList<UserPermission>
        for (permission in userPermissions) {
            permissions.add(permission.permission)
        }
        return permissions
    }
}