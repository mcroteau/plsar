package foo.router

import `plsar-auth`.Auth
import plsar.annotate.HttpRouter
import plsar.annotate.verbs.Get
import plsar.annotate.verbs.Post
import plsar.annotate.Text
import plsar.model.web.HttpRequest

@HttpRouter
class AuthRouter {

    @Text
    @Get("/secret")
    fun secret(): String {
        return "Secret!"
    }

    @Get("/signin")
    fun signinScreen(): String {
        return "/pages/signin.kti"
    }

    @Post("/signin")
    fun signin(req: HttpRequest): String {
        val user = req.get("user")?.value as String
        val pass = req.get("pass")?.value as String

        if(Auth.signin(user, pass)){
            return "[redirect]/secret"
        }

        return "[redirect]/signin"
    }

}