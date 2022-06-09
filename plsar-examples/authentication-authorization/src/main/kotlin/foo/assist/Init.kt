package foo.assist

import `plsar-auth`.Auth
import `plsar-auth`.support.DbAccess
import plsar.PLSAR
import plsar.annotate.Inject
import plsar.events.StartupEvent

class Init : StartupEvent {

    @Inject
    val authAccess : DbAccess? = null

    override fun setupComplete(cache: PLSAR.Cache?) {
        Auth.configure(authAccess)
    }
}