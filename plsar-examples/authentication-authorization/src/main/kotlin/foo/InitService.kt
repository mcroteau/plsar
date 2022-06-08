package foo

import `plsar-auth`.Auth
import plsar.PLSAR
import plsar.events.StartupEvent

class InitService : StartupEvent {
    override fun setupComplete(cache: PLSAR.Cache?) {
        Auth.configure()
    }
}