package plsar.processor

import plsar.PLSAR
import plsar.PLSAR.Support
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.*

class PropertiesProcessor(var cache: PLSAR.Cache?) {

    var support: PLSAR.Support

    @Throws(Exception::class)
    protected fun getPropertiesFile(propertyFile: String?): InputStream {
        var inputStream = this.javaClass.getResourceAsStream(PLSAR.RESOURCES + propertyFile)
        if (inputStream == null) {
            val resourceUri: String = PLSAR.Support.Companion.resourceUri
            val file = File(resourceUri + File.separator + propertyFile)
            if (!file.exists()) {
                throw Exception("$propertyFile properties file cannot be located...")
            }
            inputStream = FileInputStream(file)
        }
        return inputStream
    }

    @Throws(IOException::class)
    fun run() {
        if (cache?.propertiesFiles != null) {
            for (propertyFile in cache?.propertiesFiles!!) {
                var inputStream: InputStream? = null
                var prop: Properties? = null
                try {
                    inputStream = getPropertiesFile(propertyFile)
                    prop = Properties()
                    prop.load(inputStream)
                    val properties = prop.propertyNames()
                    while (properties.hasMoreElements()) {
                        val key = properties.nextElement() as String
                        val value = prop.getProperty(key)
                        cache?.propertyStorage?.properties?.set(key, value)
                    }
                } catch (ioe: IOException) {
                    ioe.printStackTrace()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                } finally {
                    inputStream?.close()
                }
            }
        }
    }

    init {
        support = PLSAR.Support()
    }
}