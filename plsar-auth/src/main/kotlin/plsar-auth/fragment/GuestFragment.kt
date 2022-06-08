package `plsar-auth`.pointcut

import com.sun.net.httpserver.HttpExchange
import `plsar-auth`.Auth
import plsar.model.web.HttpRequest
import plsar.web.Fragment

class GuestFragment : Fragment {

    override val key = "auth:guest"
    override val isEvaluation = true

    override fun passesCondition(httpRequest: HttpRequest?, exchange: HttpExchange?): Boolean {
        return !Auth.isAuthenticated
    }

    override fun process(httpRequest: HttpRequest?, exchange: HttpExchange?): String {
        return ""
    }
}