package plsar.web

import com.sun.net.httpserver.HttpExchange
import plsar.model.web.HttpRequest

interface Fragment {
    val key: String?
    val isEvaluation: Boolean
    fun passesCondition(httpRequest: HttpRequest?, exchange: HttpExchange?): Boolean
    fun process(httpRequest: HttpRequest?, exchange: HttpExchange?): String?

    companion object {
        const val KEY = "key:attribute"
        const val EVALUATION = false
    }
}