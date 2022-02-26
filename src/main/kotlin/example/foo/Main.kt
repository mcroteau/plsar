package example.foo

import plsar.PLSAR

fun main(){
    val plsar = PLSAR.Builder()
                .port(8080)
                .ambiance(30)
                .create()
    plsar.start()
}