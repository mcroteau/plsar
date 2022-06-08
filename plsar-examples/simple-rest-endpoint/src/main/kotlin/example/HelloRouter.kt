package example

import plsar.PLSAR
import plsar.annotate.*
import plsar.annotate.HttpRouter
import plsar.annotate.verbs.Get
import plsar.annotate.verbs.Post
import plsar.model.web.HttpRequest
import plsar.model.web.HttpResponse

@HttpRouter
class HelloRouter {

    @Text
    @Get("/text")
    fun text(): String {
        return "Hello!"
    }

}