package foo

import `plsar-auth`.interceptor.AuthInterceptor
import `plsar-auth`.pointcut.AuthFragment
import `plsar-auth`.pointcut.GuestFragment
import `plsar-auth`.pointcut.IdentityFragment
import `plsar-auth`.pointcut.UserFragment
import plsar.PLSAR

fun main(){

    val plsar = PLSAR.Builder()
                    .port(8080)
                    .ambiance(20)//# threads
                    .create()

    plsar.registerInterceptor(AuthInterceptor())

    plsar.registerFragment(IdentityFragment())
    plsar.registerFragment(UserFragment())
    plsar.registerFragment(AuthFragment())
    plsar.registerFragment(GuestFragment())

    plsar.start()
}