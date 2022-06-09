package foo.support

import `plsar-auth`.Auth
import `plsar-auth`.support.DbAccess
import plsar.PLSAR
import plsar.annotate.Inject
import plsar.events.StartupEvent

class InitService : StartupEvent {

    @Inject
    val authAccess : DbAccess? = null

    override fun setupComplete(cache: PLSAR.Cache?) {
        Auth.configure(authAccess)
    }
}