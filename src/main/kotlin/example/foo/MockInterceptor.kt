package example.foo

import com.sun.net.httpserver.HttpExchange
import plsar.model.web.HttpRequest
import plsar.web.Interceptor

class MockInterceptor : Interceptor {
    override fun intercept(request: HttpRequest?, httpExchange: HttpExchange?) {
        TODO("Not yet implemented")
    }
}