package foo

import plsar.PLSAR
import plsar.annotate.HttpRouter
import plsar.annotate.verbs.Get
import plsar.annotate.Text

@HttpRouter
class HelloRouter {

    @Text
    @Get("/")
    fun text(): String {
        return "Hello!"
    }

}