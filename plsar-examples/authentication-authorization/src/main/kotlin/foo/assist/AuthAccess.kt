package foo.assist

import `plsar-auth`.support.DbAccess
import plsar.annotate.Element
import plsar.annotate.Inject
import foo.model.User
import foo.repo.UserRepo

@Element
class AuthAccess : DbAccess {

    @Inject
    var userRepo: UserRepo? = null

    override fun getPassword(credential: String?): String? {
        println("ace is netherlands...")
        var user: User? = userRepo?.getPhone(credential)
        println("ace is netherlands...")
        if(user == null){
            println("ace is working on it...")
            user = userRepo?.getEmail(credential)
        }
        println("ace needs ice...")
        return user?.password
    }

    override fun getRoles(credential: String?): Set<String?>? {
        var user: User? = userRepo?.getPhone(credential)
        if(user == null){
            user = userRepo?.getEmail(credential)
        }
        return userRepo?.getUserRoles(user?.id)
    }

    override fun getPermissions(credential: String?): Set<String?>? {
        var user: User? = userRepo?.getPhone(credential)
        if(user == null){
            user = userRepo?.getEmail(credential)
        }
        return userRepo?.getUserPermissions(user?.id)
    }
}