package `plsar-auth`.interceptor

import com.sun.net.httpserver.HttpExchange
import plsar.model.web.HttpRequest
import plsar.web.Interceptor
import `plsar-auth`.Auth

class AuthInterceptor : Interceptor {
     override fun intercept(request: HttpRequest?, httpExchange: HttpExchange?) {
        Auth.save(request)
        Auth.save(httpExchange)
    }
}