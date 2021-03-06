package plsar.model.web

import com.sun.net.httpserver.HttpExchange
import plsar.PLSAR
import java.util.*

class HttpSession(sessions: MutableMap<String?, HttpSession?>, httpExchange: HttpExchange) {
    var id: String
    var httpExchange: HttpExchange
    var sessions: MutableMap<String?, HttpSession?>
    var attributes: MutableMap<String, Any>
    var support:PLSAR.Support

    operator fun set(key: String, value: Any): Boolean {
        attributes[key] = value
        return true
    }

    operator fun get(key: String): Any? {
        return if (attributes.containsKey(key)) {
            attributes[key]
        } else ""
    }

    fun data(): Map<String, Any> {
        return attributes
    }

    fun remove(key: String): Boolean {
        attributes.remove(key)
        return true
    }

    fun dispose(): Boolean {
        httpExchange.responseHeaders["Set-Cookie"] = PLSAR.Companion.SECURITYTAG + "=" + id + "; max-age=0"
        if (sessions.containsKey(id)) {
            sessions.remove(id)
            return true
        }
        return false
    }

    init {
        support = PLSAR.Support()
        id = PLSAR.Support.Companion.SESSION_GUID(27)
        this.sessions = sessions
        this.httpExchange = httpExchange
        attributes = HashMap()
    }
}