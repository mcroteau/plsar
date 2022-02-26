# ∆ PLSAR 
`eco-system`

PLSAR is a web framework dedicated to 
rapid development.

PLSAR ships with everything you need to 
build a web system including dependency 
injection, http routing, data persistence and 
view data binding.

## Get Started
**src/main/kotlin/Main.kt**
```
import plsar.PLSAR

fun main(){
    val plsar = PLSAR.Builder()
                .port(8080)
                .executors(30)
                .create()
    plsar.start()
}
```


You need is a Router to route your 
incoming requests.

## Http Routing
**src/main/kotlin/HelloRouter.kt**
```
@HttpRouter
class HelloRouter {

    @Text
    @Get("/")
    fun text(): String {
        return "hello world"
    }

}
```

Now all that is left is a config file.

## Configuration
**src/main/resources/plsar.props**
```
plsar.env=basic
plsar.assets=
plsar.properties=this
```


