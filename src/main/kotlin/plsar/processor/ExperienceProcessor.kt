package plsar.processor

import com.sun.net.httpserver.HttpExchange
import plsar.exception.PlsarException
import plsar.model.Iterable
import plsar.model.web.HttpRequest
import plsar.model.web.HttpResponse
import plsar.web.Fragment
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.math.BigDecimal
import java.util.*

class ExperienceProcessor {
    var namespace : String? = ""
    val NEWLINE = "\r\n"
    val FOREACH = "<$namespace:each"

    @Throws(
        PlsarException::class,
        NoSuchFieldException::class,
        IllegalAccessException::class,
        NoSuchMethodException::class,
        InvocationTargetException::class
    )
    fun process(pointcuts: Map<String?, Fragment?>?,
                view: String,
                httpResponse: HttpResponse,
                request: HttpRequest?,
                exchange: HttpExchange?
                ): String {

        val entries = Arrays.asList(*view.split("\n".toRegex()).toTypedArray())
        setNamespace(entries)

        if (exchange != null) {
            evaluatePointcuts(request, exchange, entries, pointcuts)
        }

        for (a6 in entries.indices) {
            var entryBase = entries[a6]

            if(entryBase.startsWith("<!--")){
                entryBase =  ""
                entries[a6] = ""
            }

            if (entryBase.contains("<$namespace:set")) {
                setVariable(entryBase, httpResponse)
            }

            if (entryBase.contains("<$namespace:each")) {
                val iterable = getIterable(a6, entryBase, httpResponse, entries)
                val eachOut = StringBuilder()
                //                System.out.println("z" + iterable.getStop() + ":" +  entries.get(iterable.getStop()));
                for (a7 in iterable.pojos!!.indices) {
                    val obj = iterable.pojos!![a7]
                    var ignore: List<Int?> = ArrayList()
                    for (a8 in iterable.start until iterable.stop) {
                        var entry = entries[a8]
                        if(entry.startsWith("<!--")){
                            entry =  ""
                            entries[a8] = ""
                        }

                        if (entry.contains("<$namespace:if condition=")) {
                            ignore = evaluateEachCondition(a8, entry, obj, httpResponse, entries)
                        }
                        if (ignore.contains(a8)) continue
                        if (entry.contains("<$namespace:each")) {
                            val deepIterable = getIterableObj(a8, entry, obj, entries)
                            val deepEachOut = StringBuilder()
                            deepIterateEvaluate(a8, deepEachOut, deepIterable, httpResponse, entries)
                            eachOut.append(deepEachOut.toString())
                        }
                        entry = evaluateEntry(0, 0, iterable.field, entry, httpResponse)
                        if (entry.contains("<$namespace:set")) {
                            setEachVariable(entry, httpResponse, obj)
                        }
                        evaluateEachEntry(entry, eachOut, obj, iterable.field)
                    }
                }
                entries[a6] = eachOut.toString()
                entries[iterable.stop] = ""
                for (a7 in a6 + 1 until iterable.stop) {
                    entries[a7] = ""
                }
            } else {
                if (entryBase.contains("<$namespace:if condition=")) {
                    evaluateCondition(a6, entryBase, httpResponse, entries)
                }
                entryBase = evaluateEntry(0, 0, "", entryBase, httpResponse)
                entries[a6] = entryBase
            }
        }
        val entriesCleaned = cleanup(entries)
        val output = StringBuilder()
        for (a6 in entriesCleaned.indices) {
            output.append(entriesCleaned[a6] + NEWLINE)
        }
        val finalOut = retrieveFinal(output)
        return finalOut.toString()
    }

    private fun cleanup(entries: MutableList<String>): List<String> {
        for (a2 in entries.indices) {
            val entry = entries[a2]
            if(entry.contains("<plsar:namespace"))entries[a2] = ""
            if (entry.contains("<$namespace:if")) entries[a2] = ""
            if (entry.contains("</$namespace:if>")) entries[a2] = ""
        }
        return entries
    }

    private fun retrieveFinal(eachOut: StringBuilder): StringBuilder {
        val finalOut = StringBuilder()
        val parts = eachOut.toString().split("\n".toRegex()).toTypedArray()
        for (bit in parts) {
            if (bit.trim { it <= ' ' } != "") finalOut.append(bit + NEWLINE)
        }
        return finalOut
    }

    @Throws(PlsarException::class)
    fun setNamespace(entries:MutableList<String>) : String? {
        for(a4 in entries.indices){
            val entry = entries[a4]
            if(entry.contains("<plsar:namespace")){
                val startVar = entry.indexOf("var=")
                val endVar = entry.indexOf("\"", startVar + 5)//var="
                namespace = entry.substring(startVar + 5, endVar)
                return namespace;
            }
        }
        throw PlsarException("namespace not found. searching for <plsar:namespace var=\"foo\">.")
    }

    @Throws(
        NoSuchFieldException::class,
        IllegalAccessException::class,
        PlsarException::class,
        NoSuchMethodException::class,
        InvocationTargetException::class
    )
    private fun deepIterateEvaluate(a8: Int,
                                eachOut: StringBuilder,
                                iterable: Iterable,
                                httpResponse: HttpResponse,
                                entries: MutableList<String>
                            ) {
        for (a7 in iterable.pojos!!.indices) {
            val obj = iterable.pojos!![a7]
            var ignore: List<Int?> = ArrayList()
            for (a6 in iterable.start until iterable.stop) {
                var entry = entries[a6]
                if(entry.startsWith("<!--")){
                    entry =  ""
                    entries[a6] = ""
                }
                if (entry.contains("<$namespace:if condition=")) {
                    ignore = evaluateEachCondition(a8, entry, obj, httpResponse, entries)
                }
                if (ignore.contains(a8)) continue
                if (entry.contains(FOREACH)) continue
                entry = evaluateEntry(0, 0, iterable.field, entry, httpResponse)
                if (entry.contains("<$namespace:set")) {
                    setEachVariable(entry, httpResponse, obj)
                }
                evaluateEachEntry(entry, eachOut, obj, iterable.field)
            }
        }
        for (a7 in a8 + 1 until iterable.stop) {
            entries[a7] = ""
        }
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun evaluateEachCondition(
                a8: Int,
                entry: String,
                obj: Any?,
                httpResponse: HttpResponse,
                entries: List<String>
            ): List<Int?> {
        var ignore: List<Int?> = ArrayList()
        val stop = getEachConditionStop(a8, entries)
        val startIf = entry.indexOf("<$namespace:if condition=")

        val startExpression = entry.indexOf("\${", startIf)
        val endExpression = entry.indexOf("}", startExpression)

        val expressionNite = entry.substring(startExpression, endExpression + 1)
        val expression = entry.substring(startExpression + 2, endExpression)

        val condition = getCondition(expression)
        val bits = expression.split(condition.toRegex()).toTypedArray()
        val subjectPre = bits[0].trim { it <= ' ' }
        val predicatePre = bits[1].trim { it <= ' ' }


        //<$namespace:if condition="${town.id == organization.townId}">

        //todo:?2 levels
        //todo: switch
        if (subjectPre.contains(".")) {
            val startSubject = subjectPre.indexOf(".")
            val subjectKey = subjectPre.substring(startSubject + 1).trim { it <= ' ' }
            val subjectObj = getValueRecursive(0, subjectKey, obj)
            val subject = subjectObj.toString()
            if (predicatePre == "null") {
                if (subjectObj == null && condition == "!=") {
                    ignore = getIgnoreEntries(a8, stop)
                }
                if (subjectObj != null && condition == "==") {
                    ignore = getIgnoreEntries(a8, stop)
                }


            } else {
                val predicateKeys = predicatePre.split("\\.".toRegex()).toTypedArray()
                val key = predicateKeys[0]
                val field = predicateKeys[1]
                if(httpResponse[key] == null){
                    throw PlsarException("${'$'}{$key} not found in HttpResponse")
                }
                val keyObj = httpResponse[key]
                val fieldObj = keyObj!!.javaClass.getDeclaredField(field)
                fieldObj.isAccessible = true
                val predicate = fieldObj[keyObj].toString()
                if (predicate == subject && condition == "!=") {
                    ignore = getIgnoreEntries(a8, stop)
                }
                if (predicate != subject && condition == "==") {
                    ignore = getIgnoreEntries(a8, stop)
                }
            }
        } else {
            //todo: one key
        }
        val a = entries[a8]
        val b = entries[stop]
        return ignore
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun setVariable(entry: String, httpResponse: HttpResponse) {
        val startVariable = entry.indexOf("variable=\"")
        val endVariable = entry.indexOf("\"", startVariable + 10)
        //music.
        val variableKey = entry.substring(startVariable + 10, endVariable)
        val startValue = entry.indexOf("value=\"")
        val endValue = entry.indexOf("\"", startValue + 7)
        var valueKey: String
        valueKey = if (entry.contains("value=\"\${")) {
            entry.substring(startValue + 9, endValue)
        } else {
            entry.substring(startValue + 7, endValue)
        }
        if (valueKey.contains(".")) {
            valueKey = valueKey.replace("}", "")
            val keys = valueKey.split("\\.".toRegex()).toTypedArray()
            val key = keys[0]
            if (httpResponse.data().containsKey(key)) {
                val obj = httpResponse[key]
                val field = keys[1]
                val fieldObj = obj!!.javaClass.getDeclaredField(field)
                fieldObj.isAccessible = true
                val valueObj = fieldObj[obj]
                val value = valueObj.toString()
                httpResponse[variableKey] = value
            }
        } else {
            httpResponse[variableKey] = valueKey
        }
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun setEachVariable(entry: String, httpResponse: HttpResponse, obj: Any?) {
        val startVariable = entry.indexOf("variable=\"")
        val endVariable = entry.indexOf("\"", startVariable + 10)
        val variableKey = entry.substring(startVariable + 10, endVariable)
        val startValue = entry.indexOf("value=\"")
        val endValue = entry.indexOf("\"", startValue + 7)
        val valueKey: String
        valueKey = if (entry.contains("value=\"\${")) {
            entry.substring(startValue + 9, endValue)
        } else {
            entry.substring(startValue + 7, endValue)
        }
        if (valueKey.contains(".")) {
            val value = getValueRecursive(0, valueKey, obj)
            httpResponse[variableKey] = value.toString()
        } else {
            httpResponse[variableKey] = valueKey
        }
    }

    private fun evaluatePointcuts(
        request: HttpRequest?,
        exchange: HttpExchange,
        entries: MutableList<String>,
        pointcuts: Map<String?, Fragment?>?
    ) {
        for ((_, pointcut) in pointcuts!!) {
            val key = pointcut?.key //dice:rollem in <dice:rollem> is key
            val open = "<$key>"
            val rabbleDos = "<$key/>"
            val close = "</$key>"
            for (a6 in entries.indices) {
                var entryBase = entries[a6]
                if (entryBase.trim { it <= ' ' }.startsWith("<!--")) entries[a6] = ""
                if (entryBase.trim { it <= ' ' }.startsWith("<%--")) entries[a6] = ""
                if (entryBase.contains(rabbleDos) &&
                    !pointcut!!.isEvaluation
                ) {
                    val output = pointcut.process(request, exchange)
                    if (output != null) {
                        entryBase = entryBase.replace(rabbleDos, output)
                        entries[a6] = entryBase
                    }
                }
                if (entryBase.contains(open)) {
                    val stop = getAttributeClose(a6, close, entries)
                    if (pointcut!!.isEvaluation) {
                        val isTrue = pointcut.isTrue(request, exchange)
                        if (!isTrue) {
                            for (a4 in a6 until stop) {
                                entries[a4] = ""
                            }
                        }
                    }
                    if (!pointcut.isEvaluation) {
                        val output = pointcut.process(request, exchange)
                        if (output != null) {
                            entryBase = entryBase.replace(open, output)
                            entryBase = entryBase.replace(open + close, output)
                            entries[a6] = entryBase
                            for (a4 in a6 + 1 until stop) {
                                entries[a4] = ""
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getAttributeClose(a6: Int, closeKey: String, entries: List<String>): Int {
        for (a5 in a6 until entries.size) {
            val entry = entries[a5]
            if (entry.contains(closeKey)) {
                return a5
            }
        }
        return a6
    }

    @Throws(
        NoSuchMethodException::class,
        InvocationTargetException::class,
        IllegalAccessException::class,
        PlsarException::class,
        NoSuchFieldException::class
    )
    private fun evaluateCondition(a6: Int, entry: String, httpResponse: HttpResponse, entries: MutableList<String>) {
        val stop = getConditionStop(a6, entries)
        val startIf = entry.indexOf("<$namespace:if condition=")

        val startExpression = entry.indexOf("\${", startIf)
        val endExpression = entry.indexOf("}", startExpression)

        val expressionNite = entry.substring(startExpression, endExpression + 1)
        val expression = entry.substring(startExpression + 2, endExpression)

        val condition = getCondition(expression)
        val parts: Array<String>

        parts = if (condition != "") { //check if condition exists
            expression.split(condition.toRegex()).toTypedArray()
        } else {
            arrayOf(expression)
        }

        if (parts.size > 1) {
            val left = parts[0].trim()

            if (left.contains(".")) {
                var predicate = parts[1].trim()
                val keys = left.split("\\.".toRegex()).toTypedArray()
                val objName = keys[0]
                val queryStart = left.indexOf(".")
                val query = left.substring(queryStart + 1)

                if (!keys[1].contains("(")) {
                    val field = keys[1].trim()
                    if (httpResponse.data().containsKey(objName)) {
                        if(predicate.contains("'")) predicate = predicate.replace("'", "")
                        if (predicate != null && predicate != "") {
                            val obj = httpResponse[objName]
                            var value = getValueRecursive(0, query, obj).toString()
                            checkCondition(a6, stop, value, condition, predicate, entries)
                        }
                    } else {
                        clearUxPartial(a6, stop, entries)
                    }
                } else {
                    val methodName = keys[1]
                        .replace("(", "")
                        .replace(")", "")
                    if (httpResponse.data().containsKey(objName)) {
                        val obj = httpResponse[objName]
                        val method = obj!!.javaClass.getDeclaredMethod(methodName)
                        val type: Type = method.returnType
                        val subject: Any = method.invoke(obj).toString()
                        if (!isConditionMet(subject.toString(), predicate, condition, type)) {
                            clearUxPartial(a6, stop, entries)
                        }
                    } else {
                        clearUxPartial(a6, stop, entries)
                    }
                }
            } else {
                //if single lookup
                val subject = parts[0].trim()
                var predicate = parts[1].trim()

                if (httpResponse.data().containsKey(subject)) {
                    if (predicate.contains("'")) predicate = predicate.replace("'".toRegex(), "")
                    val value = httpResponse[subject].toString()

                    checkCondition(a6, stop, value, condition, predicate, entries)
                } else {
                    if (condition == "!=" &&
                        (predicate == "''" || predicate == "null")
                    ) {
                        clearUxPartial(a6, stop, entries)
                    }
                }
            }
        } else {
            var notTrueExists = false
            var subjectPre = parts[0].trim()
            if (subjectPre.startsWith("!")) {
                notTrueExists = true
                subjectPre = subjectPre.replace("!", "")
            }

            //todo : resolve
            if (subjectPre.contains(".")) {
                val keys = subjectPre.split("\\.".toRegex()).toTypedArray()
                val subject = keys[0]
                val startField = subjectPre.indexOf(".")
                val field = subjectPre.substring(startField + 1)
                if (httpResponse.data().containsKey(subject)) {
                    val obj = httpResponse[subject]
                    val fieldObj = obj!!.javaClass.getDeclaredField(field)
                    fieldObj.isAccessible = true
                    val valueObj = fieldObj[obj]
                    val isTrue = java.lang.Boolean.valueOf(valueObj.toString())
                    if (isTrue == true && notTrueExists == true) {
                        clearUxPartial(a6, stop, entries)
                    }
                    if (isTrue == false && notTrueExists == false) {
                        clearUxPartial(a6, stop, entries)
                    }
                } else {
                    if (!notTrueExists) {
                        clearUxPartial(a6, stop, entries)
                    }
                }
            } else {
                if (httpResponse.data().containsKey(subjectPre)) {
                    val obj = httpResponse[subjectPre]
                    val isTrue = java.lang.Boolean.valueOf(obj.toString())
                    if (isTrue == true && notTrueExists == true) {
                        clearUxPartial(a6, stop, entries)
                    }
                    if (isTrue == false && notTrueExists == false) {
                        clearUxPartial(a6, stop, entries)
                    }

                } else {
                    if (!notTrueExists) {
                        clearUxPartial(a6, stop, entries)
                    }
                }
            }
        }
        entries[a6] = ""
        entries[stop] = ""
        entry.replace(expressionNite, "condition issue : '$expression'")
    }

    fun checkCondition(a6: Int, stop: Int, value: String?, condition : String?, predicate: String?, entries: MutableList<String>){
        if (value.equals(predicate) && condition.equals("!=")) {
            clearUxPartial(a6, stop, entries)
        }
        if (!value.equals(predicate) && condition.equals("==")) {
            clearUxPartial(a6, stop, entries)
        }
    }


    fun getIgnoreEntries(a6: Int, stop: Int): List<Int?> {
        val ignore: MutableList<Int?> = ArrayList()
        for (a4 in a6 until stop) {
            ignore.add(a4)
        }
        return ignore
    }

    fun clearUxPartial(a6: Int, stop: Int, entries: MutableList<String>) {
        for (a4 in a6 until stop) {
            entries[a4] = ""
        }
    }

    @Throws(PlsarException::class)
    private fun isConditionMet(subject: String, predicate: String?, condition: String, type: Type): Boolean {
        if (type.typeName == "int" || type.typeName == "java.lang.Integer") {
            if (condition == ">") {
                if (Integer.valueOf(subject) > Integer.valueOf(predicate)) return true
            }
            if (condition == "<") {
                if (Integer.valueOf(subject) < Integer.valueOf(predicate)) return true
            }
            if (condition == "==") {
                if (Integer.valueOf(subject) === Integer.valueOf(predicate)) return true
            }
            if (condition == "<=") {
                if (Integer.valueOf(subject) <= Integer.valueOf(predicate)) return true
            }
            if (condition == ">=") {
                if (Integer.valueOf(subject) >= Integer.valueOf(predicate)) return true
            }
        } else {
            throw PlsarException("integers only covered right now.")
        }
        return false
    }

    private fun getEachConditionStop(a6: Int, entries: List<String>): Int {
        for (a5 in a6 + 1 until entries.size) {
            if (entries[a5].contains("</$namespace:if>")) return a5
        }
        return a6
    }

    private fun getConditionStop(a6: Int, entries: List<String>): Int {
        var startCount = 1
        var endCount = 0
        for (a5 in a6 + 1 until entries.size) {
            val entry = entries[a5]
            if (entry.contains("</$namespace:if>")) {
                endCount++
            }
            if (entry.contains("<$namespace:if condition=")) {
                startCount++
            }
            if (startCount == endCount && entry.contains("</$namespace:if>")) {
                return a5
            }
        }
        return a6
    }

    private fun getCondition(expression: String): String {
        if (expression.contains(">")) return ">"
        if (expression.contains("<")) return "<"
        if (expression.contains("==")) return "=="
        if (expression.contains("eq")) return "=="
        if (expression.contains(">=")) return ">="
        if (expression.contains("<=")) return "<="
        if (expression.contains("not eq")) return "!="
        return if (expression.contains("!=")) "!=" else ""
    }

    private fun retrofit(a6: Int, size: Int, entries: MutableList<String>) {
        for (a10 in a6 until a6 + size + 1) {
            entries[a10] = ""
        }
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun evaluateEachEntry(entry: String, output: StringBuilder, obj: Any?, activeKey: String?) {
        var entry = entry
        if (entry.contains("<$namespace:each")) return
        if (entry.contains("</$namespace:each>")) return
        if (entry.contains("<$namespace:if condition")) return
        if (entry.contains("\${")) {
            val startExpression = entry.indexOf("\${")
            val endExpression = entry.indexOf("}", startExpression)
            val expression = entry.substring(startExpression, endExpression + 1)
            val keys = entry.substring(startExpression + 2, endExpression).split("\\.".toRegex()).toTypedArray()
            if (keys[0] == activeKey) {
                val startField = expression.indexOf(".")
                val endField = expression.indexOf("}")
                val field = expression.substring(startField + 1, endField)
                val valueObj = getValueRecursive(0, field, obj)
                var value = ""
                if (valueObj != null) value = valueObj.toString()
                entry = entry.replace(expression, value)
                val startRemainder = entry.indexOf("\${")
                if (startRemainder != -1) {
                    evaluateEntryRemainder(startExpression, entry, obj, output)
                } else {
                    output.append(entry + NEWLINE)
                }
            }
        } else {
            output.append(entry + NEWLINE)
        }
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun evaluateEntryRemainder(startExpressionRight: Int, entry: String, obj: Any?, output: StringBuilder) {
        var entry = entry
        val startExpression = entry.indexOf("\${", startExpressionRight - 1)
        val endExpression = entry.indexOf("}", startExpression)
        val expression = entry.substring(startExpression, endExpression + 1)
        val startField = expression.indexOf(".")
        val endField = expression.indexOf("}")
        val field = expression.substring(startField + 1, endField)
        val valueObj = getValueRecursive(0, field, obj)
        var value = ""
        if (valueObj != null) value = valueObj.toString()
        entry = entry.replace(expression, value)
        val startRemainder = entry.indexOf("\${", startExpressionRight - value.length)
        if (startRemainder != -1) {
            evaluateEntryRemainder(startExpression, entry, obj, output)
        } else {
            output.append(entry + NEWLINE)
        }
    }

    @Throws(PlsarException::class, NoSuchFieldException::class, IllegalAccessException::class)
    private fun getIterableObj(a6: Int, entry: String, obj: Any?, entries: List<String>): Iterable {
        val objs: List<Any?>?
        val startEach = entry.indexOf("<$namespace:each")
        val startIterate = entry.indexOf("in=", startEach)
        val endIterate = entry.indexOf("\"", startIterate + 4) //4 eq i.n.=.".
        val iterableKey = entry.substring(startIterate + 6, endIterate - 1) //in="${ and }
        val iterableFudge = "\${$iterableKey}"
        val startField = iterableFudge.indexOf(".")
        val endField = iterableFudge.indexOf("}", startField)
        val field = iterableFudge.substring(startField + 1, endField)

        val startItem = entry.indexOf("item=", endIterate)
        val endItem = entry.indexOf("\"", startItem + 6)
        val activeField = entry.substring(startItem + 6, endItem)

        objs = getIterableValueRecursive(0, field, obj) as ArrayList<*>?
        val iterable = Iterable()
        val stop = getStopDeep(a6, entries)
        iterable.start = a6 + 1
        iterable.stop = stop
        iterable.pojos = objs
        iterable.field = activeField
        return iterable
    }

    @Throws(PlsarException::class, NoSuchFieldException::class, IllegalAccessException::class)
    private fun getIterable(a6: Int, entry: String, httpResponse: HttpResponse, entries: List<String>): Iterable {
        var objs: List<Any?> = ArrayList()
        val startEach = entry.indexOf("<$namespace:each")
        val startIterate = entry.indexOf("in=", startEach)
        val endIterate = entry.indexOf("\"", startIterate + 4)//4 eq i.n.=.".

        val iterableKey = entry.substring(startIterate + 6, endIterate - 1)//in="${ and }

        val startItem = entry.indexOf("item=", endIterate)
        val endItem = entry.indexOf("\"", startItem + 6)//item="

        val activeField = entry.substring(startItem + 6, endItem)
        val expression = entry.substring(startIterate + 4, endIterate + 1)

        if (iterableKey.contains(".")) {
            objs = getIterableInitial(iterableKey, expression, httpResponse)
        } else if (httpResponse.data().containsKey(iterableKey)) {
            objs = httpResponse[iterableKey] as ArrayList<*>
        }
        val iterable = Iterable()
        val stop = getStop(a6 + 1, entries)

        iterable.start = a6 + 1
        iterable.stop = stop
        iterable.pojos = objs
        iterable.field = activeField

        return iterable
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun getIterableInitial(iterable: String, expression: String, httpResponse: HttpResponse): List<Any?> {
        val startField = expression.indexOf("\${")
        val endField = expression.indexOf(".", startField)
        val key = expression.substring(startField + 2, endField)
        if (httpResponse.data().containsKey(key)) {
            val obj = httpResponse[key]
            val objList: Any = getIterableRecursive(iterable, expression, obj)
            return objList as ArrayList<*>
        }
        return ArrayList()
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun getIterableRecursive(iterable: String, expression: String, objBase: Any?): List<Any> {
        val objs: List<Any> = ArrayList()
        val startField = expression.indexOf(".")
        val endField = expression.indexOf("}")
        val field = expression.substring(startField + 1, endField)
        val obj = getValueRecursive(0, field, objBase)
        return if (obj != null) {
            obj as ArrayList<*>
        } else objs
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun getIterableValueRecursive(idx: Int, baseField: String, baseObj: Any?): Any? {
        var idx = idx
        val fields = baseField.split("\\.".toRegex()).toTypedArray()
        if (fields.size > 1) {
            idx++
            val key = fields[0]
            val fieldObj = baseObj!!.javaClass.getDeclaredField(key)
            if (fieldObj != null) {
                fieldObj.isAccessible = true
                val obj = fieldObj[baseObj]
                val start = baseField.indexOf(".")
                val fieldPre = baseField.substring(start + 1)
                if (obj != null) {
                    return getValueRecursive(idx, fieldPre, obj)
                }
            }
        } else {
            val fieldObj = baseObj!!.javaClass.getDeclaredField(baseField)
            if (fieldObj != null) {
                fieldObj.isAccessible = true
                val obj = fieldObj[baseObj]
                if (obj != null) {
                    return obj
                }
            }
        }
        return ArrayList<Any?>()
    }

    @Throws(NoSuchFieldException::class, IllegalAccessException::class)
    private fun getValueRecursive(idx: Int, baseField: String, baseObj: Any?): Any? {
        var idx = idx
        val fields = baseField.split("\\.".toRegex()).toTypedArray()
        if (fields.size > 1) {
            idx++
            val key = fields[0]
            val fieldObj = baseObj!!.javaClass.getDeclaredField(key)
            fieldObj.isAccessible = true
            val obj = fieldObj[baseObj]
            val start = baseField.indexOf(".")
            val fieldPre = baseField.substring(start + 1)
            if (obj != null) {
                return getValueRecursive(idx, fieldPre, obj)
            }
        } else {
            try {
                val fieldObj = baseObj!!.javaClass.getDeclaredField(baseField)
                fieldObj.isAccessible = true
                val obj = fieldObj[baseObj]
                if (obj != null) {
                    return obj
                }
            } catch (ex: Exception) {
            }
        }
        return null
    }

    @Throws(
        NoSuchFieldException::class,
        IllegalAccessException::class,
        PlsarException::class,
        NoSuchMethodException::class,
        InvocationTargetException::class
    )
    private fun evaluateEntry(
        idx: Int,
        start: Int,
        activeField: String?,
        entry: String,
        httpResponse: HttpResponse
    ): String {
        var idx = idx
        var entry = entry
        if (entry.contains("\${") &&
            !entry.contains("<$namespace:each") &&
            !entry.contains("<$namespace:if")
        ) {
            val startExpression = entry.indexOf("\${", start)
            if (startExpression == -1) return entry
            val endExpression = entry.indexOf("}", startExpression)
            val expression = entry.substring(startExpression, endExpression + 1)
            val fieldBase = entry.substring(startExpression + 2, endExpression)
            if (fieldBase != activeField) {
                if (fieldBase.contains(".")) {
                    val fields = fieldBase.split("\\.".toRegex()).toTypedArray()
                    val key = fields[0]
                    if (httpResponse.data().containsKey(key)) {
                        val obj = httpResponse[key]
                        val startField = fieldBase.indexOf(".")
                        val passiton = fieldBase.substring(startField + 1)

                        //todo: allow for parameters?
                        if (passiton.contains("()")) {
                            val method = passiton.replace("(", "")
                                .replace(")", "")
                            try {
                                val methodObj = obj!!.javaClass.getDeclaredMethod(method)
                                val valueObj = methodObj.invoke(obj)
                                val value = valueObj.toString()
                                entry = entry.replace(expression, value)
                            } catch (ex: Exception) {
                            }
                        } else {
                            val value = getValueRecursive(0, passiton, obj)
                            if (value != null) {
                                entry = entry.replace(expression, value.toString())
                            } else if (activeField == "") {
                                //make empty!
                                entry = entry.replace(expression, "")
                            }
                        }
                    } else if (activeField == "") {
                        //make empty!
                        entry = entry.replace(expression, "")
                    }
                } else {
                    if (httpResponse.data().containsKey(fieldBase)) {
                        val obj = httpResponse[fieldBase]
                        entry = entry.replace(expression, obj.toString())
                    } else if (activeField == "") {
                        entry = entry.replace(expression, "")
                    }
                }
                if (entry.contains("\${")) {
                    idx++
                    if (idx >= entry.length) return entry
                    entry = evaluateEntry(idx, startExpression + idx, activeField, entry, httpResponse)
                }
            }
        }
        return entry
    }

    @Throws(PlsarException::class)
    private fun invokeMethod(fieldBase: String, obj: Any): String {
        val startMethod = fieldBase.indexOf(".")
        val endMethod = fieldBase.indexOf("(", startMethod)
        val name = fieldBase.substring(startMethod + 1, endMethod)
            .replace("(", "")
        val startSig = fieldBase.indexOf("(")
        val endSig = fieldBase.indexOf(")")
        val paramFix = fieldBase.substring(startSig + 1, endSig)
        val parameters = paramFix.split(",".toRegex()).toTypedArray()
        if (parameters.size > 0) {
            try {
                val method = getObjMethod(name, obj)
                val finalParams = getMethodParameters(method, parameters)
                if (method != null) {
                    return method.invoke(obj, *finalParams.toTypedArray()).toString()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } else {
            try {
                val method = obj.javaClass.getDeclaredMethod(name)
                if (method != null) {
                    return method.invoke(obj).toString()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return ""
    }

    private fun getObjMethod(methodName: String, obj: Any): Method? {
        val methods = obj.javaClass.declaredMethods
        for (method in methods) {
            val namePre = method.name
            if (namePre == methodName) return method
        }
        return null
    }

    @Throws(PlsarException::class)
    private fun getMethodParameters(method: Method?, parameters: Array<String>): List<Any?> {
        if (method!!.parameterTypes.size != parameters.size) throw PlsarException("parameters on $method don't match.")
        val finalParams: MutableList<Any?> = ArrayList()
        for (a6 in parameters.indices) {
            val parameter = parameters[a6]
            val type: Type = method.parameterTypes[a6]
            var obj: Any? = null
            if (type.typeName == "int" || type.typeName == "java.lang.Integer") {
                obj = Integer.valueOf(parameter)
            }
            if (type.typeName == "double" || type.typeName == "java.lang.Double") {
                obj = java.lang.Double.valueOf(parameter)
            }
            if (type.typeName == "java.math.BigDecimal") {
                obj = BigDecimal(parameter)
            }
            if (type.typeName == "float" || type.typeName == "java.lang.Float") {
                obj = java.lang.Float.valueOf(parameter)
            }
            finalParams.add(obj)
        }
        return finalParams
    }

    private fun getStopDeep(idx: Int, entries: List<String>): Int {
        val a10 = idx
        for (a6 in idx until entries.size) {
            val entry = entries[a6]
            if (entry.contains("</$namespace:each>")) {
                return a6
            }
        }
        return idx
    }

    private fun getStop(a6: Int, entries: List<String>): Int {
        var count = 0
        var startRendered = false
        for (a4 in a6 until entries.size) {
            val entry = entries[a4]
            if (entry.contains("<$namespace:each")) {
                startRendered = true
            }
            if (!startRendered && entry.contains("</$namespace:each>")) {
                return a4
            }
            if (startRendered && entry.contains("</$namespace:each>")) {
                if (count == 1) {
                    return a4
                }
                count++
            }
        }
        return 0
    }
}