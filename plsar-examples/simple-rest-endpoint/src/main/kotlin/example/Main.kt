package example

import plsar.PLSAR

fun main(){
    val plsar = PLSAR.Builder()
                .port(8080)
                .ambiance(130)//# threads
                .create()
    plsar.start()
}