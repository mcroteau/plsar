package foo.router

import `plsar-auth`.Auth
import plsar.annotate.HttpRouter
import plsar.annotate.verbs.Get
import plsar.annotate.verbs.Post
import plsar.annotate.Text
import plsar.model.web.HttpRequest
import plsar.model.web.HttpResponse

@HttpRouter
class AuthRouter {

    @Get("/secret")
    fun secret(resp : HttpResponse?): String {
        if(Auth.isAuthenticated){
            return "/pages/passed.kti"
        }
        resp?.set("message", "authenticate pour favor.")
        return "[redirect]/"
    }

    @Get("/")
    fun hello(): String {
        return "/pages/signin.kti"
    }

    @Post("/signin")
    fun signin(req: HttpRequest): String {
        val user = req.get("user")?.value as String
        val pass = req.get("pass")?.value as String

        if(Auth.signin(user, pass)){
            return "[redirect]/secret"
        }

        return "[redirect]/"
    }

    @Get("/signout")
    fun signout() : String {
        Auth.signout()
        return "[redirect]/"
    }

}