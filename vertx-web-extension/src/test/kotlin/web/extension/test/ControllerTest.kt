package web.extension.test

import io.github.shinglem.core.main.VertxMain
import io.github.shinglem.web.annotions.*
import io.github.shinglem.web.config.WebConfig
import io.github.shinglem.web.verticle.WebVerticle
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.Request
import org.junit.runner.Request.method
import java.nio.Buffer
import java.util.*


class ControllerTest : BaseWebTest() {

    @Before
    fun initVerticle() {
        runBlocking {
            VertxMain.start(WebVerticle::class)
        }
    }

    fun <T> launch(block: suspend () -> T) {
        CoroutineScope(vertx.dispatcher()).launch {
            block()
        }
    }

    @Test
    @Throws(Exception::class)
    fun testSimpleRoute() {

        @Controller
        class TestController {
            @Route("/")
            fun testSimpleRoute(@Context rc: RoutingContext) {
                rc.response().end()
            }
        }
        runBlocking(vertx.dispatcher()) {

            testRequest(HttpMethod.GET, "/", 200, "OK")
        }
    }

    @Test
    @Throws(Exception::class)
    fun testMethod() {

        @Controller
        class TestController {
            @GET("/foo/GET")
            suspend fun testGet(@Context rc: RoutingContext) {

                rc.response().end()
            }

            @POST("/foo/POST")
            suspend fun testPost(@Context rc: RoutingContext) {

                rc.response().end()
            }

            @OPTIONS("/foo/OPTIONS")
            suspend fun testOptions(@Context rc: RoutingContext) {

                rc.response().end()
            }

            @HEAD("/foo/HEAD")
            suspend fun testHead(@Context rc: RoutingContext) {

                rc.response().end()
            }

            @PUT("/foo/PUT")
            suspend fun testPut(@Context rc: RoutingContext) {


                rc.response().end()
            }

            @DELETE("/foo/DELETE")
            suspend fun testDelete(@Context rc: RoutingContext) {

                rc.response().end()
            }

            @TRACE("/foo/TRACE")
            suspend fun testTrace(@Context rc: RoutingContext) {

                rc.response().end()
            }

            @PATCH("/foo/PATCH")
            suspend fun testPatch(@Context rc: RoutingContext) {

                rc.response().end()
            }
        }
        runBlocking(vertx.dispatcher()) {

            val path = "/foo/"

            val METHODS: Set<HttpMethod> = HashSet(
                Arrays.asList(
                    HttpMethod.GET,
                    HttpMethod.DELETE,
                    HttpMethod.HEAD,
                    HttpMethod.PATCH,
                    HttpMethod.OPTIONS,
                    HttpMethod.TRACE,
                    HttpMethod.POST,
                    HttpMethod.PUT
                )
            )

            for (meth in METHODS) {
                for (method in METHODS) {
                    if (meth != method) {
                        testRequest(method, path + meth.name(), HttpResponseStatus.METHOD_NOT_ALLOWED)
                    } else {
                        testRequest(method, path + meth.name(), HttpResponseStatus.OK)
                    }
                }

            }
        }
    }


    @Test
    @Throws(Exception::class)
    fun testParam() {
        @Controller
        class TestController {
            @Route("/foo")
            fun testParam(
                @StringParam string: String,
                @NumberParam number: Number,
                @BoolParam boolean: Boolean,
                @JsonObjectParam jsonObject: JsonObject,
                @JsonArrayParam jsonArray: JsonArray,

                ): JsonObject {
                return JsonObject()
                    .put("string", string)
                    .put("number", number)
                    .put("boolean", boolean)
                    .put("jsonObject", jsonObject)
                    .put("jsonArray", jsonArray)
            }

            @Route("/foo2")
            fun testParam2(
                @EntityParam entity: TestData2,
            ): TestData2 {
                return entity
            }
        }


        runBlocking(vertx.dispatcher()) {
            val respUtil = WebConfig.responseUtil()

            val test1 = TestData( "str", 123, false, JsonObject().put("json" , "test"), JsonArray().add("aaa").add(111))

//            val json = JsonObject()
//                .put("string" , "str")
//                .put("number" , 123)
//                .put("boolean" , false)
//                .put("jsonObject" , JsonObject().put("json" , "test"))
//                .put("jsonArray" , JsonArray().add("aaa").add(111))

            val json =  JsonObject.mapFrom(test1)

            testRequest(HttpMethod.POST, "/foo", HttpResponseStatus.OK , respUtil.successResponse(json).trim(),json.encode()) {
                val a = JsonObject(respUtil.successResponse(json).trim())
                val b = json
                Assert.assertEquals(a.getJsonObject("data"), b)

                val buff = it.body().await()

                val c =   JsonObject(buff)
                Assert.assertEquals(a, c)

            }
        }

        runBlocking(vertx.dispatcher()) {
            val respUtil = WebConfig.responseUtil()

            val test1 = TestData2("str", 123, false, Entity("test"))

            val json = JsonObject.mapFrom(test1)

            testRequest(
                HttpMethod.POST,
                "/foo2",
                HttpResponseStatus.OK,
                respUtil.successResponse(json).trim(),
                json.encode()
            ) {
                val a = JsonObject(respUtil.successResponse(json).trim())
                val b = json
                Assert.assertEquals(a.getJsonObject("data"), b)

                val buff = it.body().await()

                val c = JsonObject(buff)
                Assert.assertEquals(a, c)

            }
        }
    }

}