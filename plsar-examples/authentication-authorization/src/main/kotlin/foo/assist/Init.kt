package foo.assist

import `plsar-auth`.Auth
import `plsar-auth`.support.DbAccess
import plsar.PLSAR
import plsar.model.Element
import plsar.annotate.Events
import plsar.annotate.Inject
import plsar.annotate.Service
import plsar.events.StartupEvent

@Events
class Init : StartupEvent {

    override fun setupComplete(cache: PLSAR.Cache?) {
        var authAccess = cache?.getElement("authAccess") as AuthAccess
        Auth.configure(authAccess)
    }

}