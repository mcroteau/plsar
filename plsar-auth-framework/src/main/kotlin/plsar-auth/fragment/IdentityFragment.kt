package `plsar-auth`.pointcut

import com.sun.net.httpserver.HttpExchange
import `plsar-auth`.Auth
import plsar.model.web.HttpRequest
import plsar.web.Fragment

class IdentityFragment : Fragment {

    override val key = "auth:id"
    override val isEvaluation = false

    override fun passesCondition(httpRequest: HttpRequest?, exchange: HttpExchange?): Boolean {
        return true
    }

    override fun process(httpRequest: HttpRequest?, exchange: HttpExchange?): String {
        return Auth.get("userId")
    }
}