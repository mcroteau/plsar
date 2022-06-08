package foo

import plsar.PLSAR
import plsar.annotate.*
import plsar.annotate.verbs.Get
import plsar.annotate.verbs.Post
import plsar.model.web.HttpRequest
import java.io.*
import java.nio.file.Paths

@HttpRouter
class MediaRouter {

    @Inject
    var support: PLSAR.Support? = null

    @Get("/media/add")
    @Title("Add Media")
    @Design("designs/default.htm")
    @Meta("media,add", "adding media")
    fun addPersonRender(): String {
        return "/pages/media/add.kvi"
    }

    @Post("/media/store")
    fun addPerson(req: HttpRequest?): String {

        val bytes : ByteArray? = req?.getPayload("media")
        val name = req?.get("media")!!.fileName
        val uri = Paths.get("webapp","assets", "media", "$name").toUri()

        var file = File(uri)
        file.getParentFile().mkdirs()
        file.createNewFile()

        val fos : OutputStream = FileOutputStream(file)
        if (bytes != null) fos.write(bytes)

        return "[redirect]/media/add"
    }
}