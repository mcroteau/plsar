package `plsar-auth`.pointcut

import com.sun.net.httpserver.HttpExchange
import plsar.model.web.HttpRequest
import plsar.web.Fragment
import `plsar-auth`.Auth

class AuthFragment : Fragment {

    override val key = "auth:authenticated"
    override val isEvaluation = true

    override fun passesCondition(httpRequest: HttpRequest?, exchange: HttpExchange?): Boolean {
        return Auth.isAuthenticated
    }

    override fun process(httpRequest: HttpRequest?, exchange: HttpExchange?): String {
        return ""
    }
}