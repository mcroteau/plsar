package plsar

import com.sun.net.httpserver.Headers
import com.sun.net.httpserver.HttpServer
import plsar.cargo.ElementStorage
import plsar.cargo.ObjectStorage
import plsar.cargo.PropertyStorage
import plsar.model.Element
import plsar.model.InstanceDetails
import plsar.model.web.EndpointMappings
import plsar.model.web.HttpRequest
import plsar.processor.ElementProcessor
import plsar.processor.ExperienceProcessor
import plsar.startup.ExchangeStartup
import plsar.util.Settings
import plsar.web.HttpTransmission
import plsar.web.Interceptor
import plsar.web.Fragment
import java.io.*
import java.lang.reflect.Type
import java.math.BigDecimal
import java.net.InetSocketAddress
import java.nio.file.Paths
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.jar.JarEntry
import java.util.jar.JarFile
import javax.sql.DataSource

class PLSAR(builder: Builder) {

    val port : Int?
    var cache : Cache? = null
    var support: Support? = null
    var experienceProcessor : ExperienceProcessor ? = null
    var httpServer: HttpServer?
    var fragments: MutableMap<String?, Fragment?>
    var interceptors: MutableMap<String?, Interceptor?>

    fun stop(){
        httpServer?.stop(0)
    }

    @Throws(Exception::class)
    fun start(): PLSAR {
        experienceProcessor = ExperienceProcessor()
        val exchangeStartup = ExchangeStartup(port, fragments, interceptors, experienceProcessor!!)
        exchangeStartup.start()
        cache = exchangeStartup.cache
        val modulator = HttpTransmission(cache)
        httpServer!!.createContext("/", modulator)
        httpServer!!.start()
        return this
    }

    /**
     * Adds view fragment to the list of view fragments
     * to be performed upon a request.
     *
     * Performs like J2EE taglibs
     *
     * @param Fragment
     */
    fun registerFragment(fragment: Fragment): Boolean {
        val key = support!!.getName(fragment.javaClass.name)
        fragments[key] = fragment
        return true
    }

    /**
     * Adds interceptor to the list of interceptors
     * to be performed upon a request.
     *
     * Performs like J2EE Filters
     *
     * @param Interceptor
     */
    fun registerInterceptor(interceptor: Interceptor): Boolean {
        val key = support!!.getName(interceptor.javaClass.name)
        interceptors[key] = interceptor
        return true
    }

    class Builder {
        var port: Int? = null
        var httpServer: HttpServer? = null
        var executors: ExecutorService? = null
        var support: Support? = null

        fun port(port: Int?): Builder {
            this.port = port
            return this
        }

        @Throws(IOException::class)
        fun ambiance(numberThreads: Int): Builder {
            support = Support()
            executors = Executors.newFixedThreadPool(numberThreads)
            httpServer = HttpServer.create(InetSocketAddress(port!!), 0)
            httpServer?.setExecutor(executors)
            return this
        }

        fun create(): PLSAR {
            return PLSAR(this)
        }
    }

    class Cache(builder: Builder) {
        var events: Any? = null
        var settings: Settings?
        var fragments: Map<String?, Fragment?>?
        var interceptors: Map<String?, Interceptor?>?
        var objectStorage: ObjectStorage
        var propertyStorage: PropertyStorage
        var elementStorage: ElementStorage
        var repo: Repo?
        var experienceProcessor: ExperienceProcessor?
        var elementProcessor: ElementProcessor? = null
        var endpointMappings: EndpointMappings? = null

        fun getElement(name: String): Any? {
            val key = name.lowercase()
            return if (elementStorage.elements.containsKey(key)) {
                elementStorage.elements[key]?.element
            } else null
        }

        val elements: Map<String?, Element?>?
            get() = elementStorage.elements
        var resources: List<String?>? = null
            get() = settings?.resources
        var propertiesFiles: List<String?>? = null
            get() = settings?.propertiesFiles

        val objects: MutableMap<String, InstanceDetails>
            get() = objectStorage.objects

        class Builder {
            var repo: PLSAR.Repo? = null
            var settings: Settings? = null
            var experienceProcessor: ExperienceProcessor? = null
            var fragments: Map<String?, Fragment?>? = null
            var interceptors: Map<String?, Interceptor?>? = null
            fun withSettings(settings: Settings?): Builder {
                this.settings = settings
                return this
            }

            fun withFragments(fragments: Map<String?, Fragment?>?): Builder {
                this.fragments = fragments
                return this
            }

            fun withInterceptors(interceptors: Map<String?, Interceptor?>?): Builder {
                this.interceptors = interceptors
                return this
            }

            fun withUxProcessor(experienceProcessor: ExperienceProcessor?): Builder {
                this.experienceProcessor = experienceProcessor
                return this
            }

            fun withRepo(repo: Repo?): Builder {
                this.repo = repo
                return this
            }

            fun make(): Cache {
                return Cache(this)
            }
        }

        init {
            repo = builder.repo
            fragments = builder.fragments
            interceptors = builder.interceptors
            settings = builder.settings
            experienceProcessor = builder.experienceProcessor
            elementStorage = ElementStorage()
            propertyStorage = PropertyStorage()
            objectStorage = ObjectStorage()
        }
    }


    class Repo {

        var ds: DataSource? = null

        fun setDataSource(ds: DataSource?) {
            this.ds = ds
        }

        operator fun get(preSql: String, params: Array<Any?>, cls: Class<*>): Any? {
            var result: Any? = null
            var sql = ""
            try {
                sql = hydrateSql(preSql, params)
                val connection = ds!!.connection
                val stmt = connection.createStatement()
                val rs = stmt.executeQuery(sql)
                if (rs.next()) {
                    result = extractData(rs, cls)
                }
                if (result == null) {
                    throw Exception("$cls not found using '$sql'")
                }
                connection.commit()
                connection.close()
            } catch (ex: SQLException) {
                println("bad sql grammar : $sql")
                println("\n\n\n")
                ex.printStackTrace()
            } catch (ex: Exception) {
            }
            return result
        }

        fun getInt(preSql: String, params: Array<Any?>): Int? {
            var result: Int? = null
            var sql = ""
            try {
                sql = hydrateSql(preSql, params)
                val connection = ds!!.connection
                val stmt = connection.createStatement()
                val rs = stmt.executeQuery(sql)
                if (rs.next()) {
                    result = rs.getObject(1).toString().toInt()
                }
                if (result == null) {
                    throw Exception("no results using '$sql'")
                }
                connection.commit()
                connection.close()
            } catch (ex: SQLException) {
                println("bad sql grammar : $sql")
                println("\n\n\n")
                ex.printStackTrace()
            } catch (ex: Exception) {
            }
            return result
        }

        fun getLong(preSql: String, params: Array<Any?>): Long? {
            var result: Long? = null
            var sql = ""
            try {
                sql = hydrateSql(preSql, params)
                val connection = ds!!.connection
                val stmt = connection.createStatement()
                val rs = stmt.executeQuery(sql)
                if (rs.next()) {
                    result = rs.getObject(1).toString().toLong()
                }
                if (result == null) {
                    throw Exception("no results using '$sql'")
                }
                connection.commit()
                connection.close()
            } catch (ex: SQLException) {
                println("bad sql grammar : $sql")
                println("\n\n\n")
                ex.printStackTrace()
            } catch (ex: Exception) {
            }
            return result
        }

        fun save(preSql: String, params: Array<Any?>): Boolean {
            try {
                val sql = hydrateSql(preSql, params)
                val connection = ds!!.connection
                val stmt = connection.createStatement()
                stmt.execute(sql)
                connection.commit()
                connection.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
                return false
            }
            return true
        }

        fun getList(preSql: String, params: Array<Any?>, cls: Class<*>): List<Any?> {
            var results: MutableList<Any?> = ArrayList()
            try {
                val sql = hydrateSql(preSql, params)
                val connection = ds!!.connection
                val stmt = connection.createStatement()
                val rs = stmt.executeQuery(sql)
                results = ArrayList()
                while (rs.next()) {
                    val obj = extractData(rs, cls)
                    results.add(obj)
                }
                connection.commit()
                connection.close()
            } catch (ccex: ClassCastException) {
                println("")
                println("Wrong Class type, attempted to cast the return data as a $cls")
                println("")
                ccex.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return results
        }

        fun update(preSql: String, params: Array<Any?>): Boolean {
            try {
                val sql = hydrateSql(preSql, params)
                val connection = ds!!.connection
                val stmt = connection.createStatement()
                val rs = stmt.execute(sql)
                connection.commit()
                connection.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
                return false
            }
            return true
        }

        fun delete(preSql: String, params: Array<Any?>): Boolean {
            try {
                val sql = hydrateSql(preSql, params)
                val connection = ds!!.connection
                val stmt = connection.createStatement()
                stmt.execute(sql)
                connection.commit()
                connection.close()
            } catch (ex: Exception) {
                return false
            }
            return true
        }

        protected fun hydrateSql(sql: String, params: Array<Any?>): String {
            var sql = sql
            for (`object` in params) {
                if (`object` != null) {
                    var parameter = `object`.toString()
                    if (`object`.javaClass.typeName == "java.lang.String") {
                        parameter = parameter.replace("'", "''")
                            .replace("$", "\\$")
                            .replace("#", "\\#")
                            .replace("@", "\\@")
                    }
                    sql = sql.replaceFirst("\\[\\+\\]".toRegex(), parameter)
                } else {
                    sql = sql.replaceFirst("\\[\\+\\]".toRegex(), "null")
                }
            }
            return sql
        }

        @Throws(Exception::class)
        protected fun extractData(rs: ResultSet, cls: Class<*>): Any {
            var `object` = Any()
            val constructors = cls.constructors
            for (constructor in constructors) {
                if (constructor.parameterCount == 0) {
                    `object` = constructor.newInstance()
                }
            }
            val fields = `object`.javaClass.declaredFields
            for (field in fields) {
                field.isAccessible = true
                val originalName = field.name
                val regex = "([a-z])([A-Z]+)"
                val replacement = "$1_$2"
                val name = originalName.replace(regex.toRegex(), replacement).toLowerCase()
                val type: Type = field.type
                if (hasColumn(rs, name)) {
                    if (type.typeName == "int" || type.typeName == "java.lang.Integer") {
                        field[`object`] = rs.getInt(name)
                    } else if (type.typeName == "double" || type.typeName == "java.lang.Double") {
                        field[`object`] = rs.getDouble(name)
                    } else if (type.typeName == "float" || type.typeName == "java.lang.Float") {
                        field[`object`] = rs.getFloat(name)
                    } else if (type.typeName == "long" || type.typeName == "java.lang.Long") {
                        field[`object`] = rs.getLong(name)
                    } else if (type.typeName == "boolean" || type.typeName == "java.lang.Boolean") {
                        field[`object`] = rs.getBoolean(name)
                    } else if (type.typeName == "java.math.BigDecimal") {
                        field[`object`] = rs.getBigDecimal(name)
                    } else if (type.typeName == "java.lang.String") {
                        field[`object`] = rs.getString(name)
                    }
                }
            }
            return `object`
        }

        companion object {
            @Throws(SQLException::class)
            fun hasColumn(rs: ResultSet, columnName: String): Boolean {
                val rsmd = rs.metaData
                for (x in 1..rsmd.columnCount) {
                    if (columnName == rsmd.getColumnName(x).toLowerCase()) {
                        return true
                    }
                }
                return false
            }
        }
    }


    class Support {

        var isJar: Boolean

        fun removeLast(s: String?): String {
            return if (s == null || s.length == 0) "" else s.substring(0, s.length - 1)
        }

        val isFat: Boolean
            get() {
                var uri: String? = null
                try {
                    uri = classesUri
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
                return if (uri!!.contains("jar:file:")) true else false
            }

        fun getPayload(bytes: ByteArray?): String {
            val sb = StringBuilder()
            for (b in bytes!!) {
                sb.append(b.toChar())
            }
            return sb.toString()
        }

        fun getPayloadBytes(requestStream: InputStream): ByteArray {
            val bos = ByteArrayOutputStream()
            try {
                val buf = ByteArray(1024 * 19)
                var bytesRead = 0
                while (requestStream.read(buf).also { bytesRead = it } != -1) {
                    bos.write(buf, 0, bytesRead)
                }
                requestStream.close()
                bos.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
            }
            return bos.toByteArray()
        }

        val jarEntries: Enumeration<JarEntry>
            get() {
                val jarFile = jarFile
                return jarFile!!.entries()
            }

        fun getName(nameWithExt: String?): String {
            val index = nameWithExt!!.lastIndexOf(".")
            var qualifiedName = nameWithExt
            if (index > 0) {
                qualifiedName = qualifiedName.substring(index + 1)
            }
            return qualifiedName.toLowerCase()
        }

        val main: String
            get() {
                try {
                    val jarFile = jarFile
                    val jarEntry = jarFile!!.getJarEntry("META-INF/MANIFEST.MF")
                    val `in` = jarFile.getInputStream(jarEntry)
                    val scanner = Scanner(`in`)
                    var line = ""
                    do {
                        line = scanner.nextLine()
                        if (line.contains("Main-Class")) {
                            line = line.replace("Main-Class", "")
                            break
                        }
                    } while (scanner.hasNext())
                    line = line.replace(":", "").trim { it <= ' ' }
                    return line
                } catch (ioex: IOException) {
                    ioex.printStackTrace()
                }
                throw IllegalStateException("Apologies, it seems you are trying to run this as a jar but have not main defined.")
            }
        val jarFile: JarFile?
            get() {
                try {
                    val jarUri = PLSAR::class.java.classLoader.getResource("plsar/")
                    val jarPath = jarUri.path.substring(5, jarUri.path.indexOf("!"))
                    return JarFile(jarPath)
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
                return null
            }

        @get:Throws(Exception::class)
        val classesUri: String?
            get() {
                var classesUri = Paths.get("src", "main", "kotlin")
                    .toAbsolutePath()
                    .toString()
                val classesDir = File(classesUri)
                if (classesDir.exists()) {
                    return classesUri
                }
                classesUri = this.javaClass.getResource("").toURI().toString()
                if (classesUri == null) {
                    throw Exception("A8i : unable to locate class uri")
                }
                return classesUri
            }

        operator fun get(request: HttpRequest, cls: Class<*>): Any? {
            return propagate(request, cls)
        }

        fun propagate(request: HttpRequest, cls: Class<*>): Any? {
            var `object`: Any? = null
            try {
                `object` = cls.getConstructor().newInstance()
                val fields = cls.declaredFields
                for (field in fields) {
                    val name = field.name
                    val value = request.value(name)
                    if (value != null &&
                        value != ""
                    ) {
                        field.isAccessible = true
                        val type: Type = field.type
                        if (type.typeName == "int" || type.typeName == "java.lang.Integer") {
                            field[`object`] = Integer.valueOf(value)
                        }
                        if (type.typeName == "double" || type.typeName == "java.lang.Double") {
                            field[`object`] = java.lang.Double.valueOf(value)
                        }
                        if (type.typeName == "float" || type.typeName == "java.lang.Float") {
                            field[`object`] = java.lang.Float.valueOf(value)
                        }
                        if (type.typeName == "long" || type.typeName == "java.lang.Long") {
                            field[`object`] = java.lang.Long.valueOf(value)
                        }
                        if (type.typeName == "boolean" || type.typeName == "java.lang.Boolean") {
                            field[`object`] = java.lang.Boolean.valueOf(value)
                        }
                        if (type.typeName == "java.math.BigDecimal") {
                            field[`object`] = BigDecimal(value)
                        }
                        if (type.typeName == "java.lang.String") {
                            field[`object`] = value
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return `object`
        }

        val project: String
            get() = if (isJar) {
                val jarFile = jarFile
                val path = jarFile!!.name
                var bits = path.split("/".toRegex()).toTypedArray()
                if (bits.size == 0) {
                    bits = path.split("\\".toRegex()).toTypedArray()
                }
                val namePre = bits[bits.size - 1]
                namePre.replace(".jar", "")
            } else {
                ""
            }

        fun convert(`in`: InputStream): StringBuilder {
            val builder = StringBuilder()
            val scanner = Scanner(`in`)
            do {
                builder.append(scanner.nextLine().trimIndent())
            } while (scanner.hasNext())
            try {
                `in`.close()
            } catch (ioex: IOException) {
                ioex.printStackTrace()
            }
            return builder
        }

        fun getCookie(cookieName: String, headers: Headers?): String {
            var value = ""
            if (headers != null) {
                val cookies = headers["Cookie"]
                if (cookies != null) {
                    for (cookie in cookies) {
                        val bits = cookie.split(";".toRegex()).toTypedArray()
                        for (completes in bits) {
                            val parts = completes.split("=".toRegex()).toTypedArray()
                            val key = parts[0].trim { it <= ' ' }
                            if (parts.size > 1) {
                                if (key == cookieName) {
                                    value = parts[1].trim { it <= ' ' }
                                }
                            }
                        }
                    }
                }
            }
            //returning the last one.
            return value
        }

        companion object {
            fun SESSION_GUID(z: Int): String {
                val CHARS = ".01234567890"
                val guid = StringBuilder()
                guid.append("A8i.")
                val rnd = Random()
                while (guid.length < z) {
                    val index = (rnd.nextFloat() * CHARS.length).toInt()
                    guid.append(CHARS[index])
                }
                return guid.toString()
            }

            @get:Throws(Exception::class)
            val resourceUri: String
                get() {
                    val resourceUri = Paths.get("src", "main", "resources")
                        .toAbsolutePath()
                        .toString()
                    val resourceDir = File(resourceUri)
                    if (resourceDir.exists()) {
                        return resourceUri
                    }
                    val RESOURCES_URI = "/src/main/resources/"
                    val indexUri = PLSAR::class.java.getResource(RESOURCES_URI)
                        ?: throw FileNotFoundException("A8i : unable to find resource $RESOURCES_URI")
                    return indexUri.toURI().toString()
                }
        }

        init {
            isJar = isFat
        }

    }



    companion object {
        const val SECURITYTAG = "plsar.sessions"
        const val RESOURCES = "/src/main/resources/"
    }

    init {
        port  = builder.port
        support = builder.support
        httpServer = builder.httpServer
        fragments = HashMap()
        interceptors = HashMap()
    }
}