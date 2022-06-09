package foo.support

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
        var user: User? = userRepo?.getPhone(credential)
        if(user == null){
            user = userRepo?.getEmail(credential)
        }
        return user.password
    }

    override fun getRoles(credential: String?): Set<String> {
        var user: User = userRepo?.getPhone(credential)
        if(user == null){
            user = userRepo.getEmail(credential)
        }
        return userRepo.getUserRoles(user.id)
    }

    override fun getPermissions(credential: String?): Set<String> {
        var user: User = userRepo?.getPhone(credential)
        if(user == null){
            user = userRepo.getEmail(credential)
        }
        return userRepo.getUserPermissions(user.id)
    }
}