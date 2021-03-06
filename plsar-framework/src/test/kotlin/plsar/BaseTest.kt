package plsar

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import plsar.processor.ExperienceProcessor
import plsar.startup.ContainerInit
import plsar.util.Settings
import plsar.web.Interceptor
import plsar.web.Fragment

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class BaseTest {

    var plsar : PLSAR ? = null
    var cache : PLSAR.Cache? = null
    var support : PLSAR.Support? = null

    @AfterEach
    fun shutdown(){
        plsar?.stop()
    }

    @BeforeEach
    fun setup(){
        plsar = PLSAR.Builder().port(8080).ambiance(10).create()
        plsar?.start()

        support = PLSAR.Support()
        val settings = Settings()
        settings.isCreateDb = true
        settings.isDropDb = true
        settings.isNoAction = false
        settings.resources = ArrayList()

        val propertiesFiles: MutableList<String> = java.util.ArrayList()
        propertiesFiles.add("plsar.props")
        settings.propertiesFiles = propertiesFiles

        val repo = PLSAR.Repo()
        cache = PLSAR.Cache.Builder()
            .withSettings(settings)
            .withUxProcessor(ExperienceProcessor())
            .withRepo(repo)
            .make()
        ContainerInit.Builder()
            .withPort(8080)
            .withRepo(repo)
            .withCache(cache)
            .withSettings(settings)
            .build()
    }
}