package example.foo

import com.sun.net.httpserver.HttpExchange
import plsar.model.web.HttpRequest
import plsar.web.Pointcut

class MockPointcut : Pointcut {
    override val key: String?
        get() = "todo:pointcut"
    override val isEvaluation: Boolean
        get() = false

    override fun isTrue(httpRequest: HttpRequest?, exchange: HttpExchange?): Boolean {
        return false
    }

    override fun process(httpRequest: HttpRequest?, exchange: HttpExchange?): String? {
        return "Noop!"
    }
}