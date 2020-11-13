package default

import io.github.shinglem.core.main.VertxMain
import io.github.shinglem.web.annotions.*
import io.github.shinglem.web.annotions.Controller
import io.github.shinglem.web.annotions.CookiesMap
import io.github.shinglem.web.annotions.GET
import io.github.shinglem.web.exp.annotions.Route
import io.github.shinglem.web.verticle.WebVerticle
import io.vertx.core.http.Cookie
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

fun main() {

        VertxMain.start<WebVerticle>()

}

data class Entity(
        val str : String,
        val num : Int,
        val boolean: Boolean,
        val json: JsonObject,
        val array: JsonArray,
)

@Controller
object TestController {

        @Route("/route")
        fun routeTest() {

        }

        @GET("/get")
        fun getTest() {

        }

        @Route("/paramTest/:str")
        fun paramTest(
                @Context routingContext: io.vertx.ext.web.RoutingContext,
                @StringParam str: String,
                @NumberParam num : Int,
                @BoolParam boolean: Boolean,
                @JsonObjectParam json: JsonObject,
                @JsonArrayParam array: JsonArray,
                @EntityParam entity : Entity,
                @CookiesMap map : Map<String , Cookie>

        ) : String{
                println(str)
                println(num)
                println(boolean)
                println(json)
                println(array)
                println(entity)
                println(map)
                return "ok"
        }

        @Route("/returnTest")
        suspend fun returnTest() : String{
                return "ok"
        }
}
