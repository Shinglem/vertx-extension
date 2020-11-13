package web.extension.test

import io.github.shinglem.core.main.VERTX
import io.github.shinglem.core.main.VertxMain
import io.github.shinglem.util.STRING_COMPARATOR
import io.github.shinglem.web.annotions.*
import io.github.shinglem.web.config.WebConfig
import io.github.shinglem.web.verticle.WebVerticle
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.multipart.MultipartForm
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import java.time.LocalDateTime
import java.util.*


class ControllerTest : BaseWebTest() {

    companion object {
        @JvmStatic
        @BeforeClass
        fun initVerticle() {
            println("-----start ${LocalDateTime.now()}-----")
            runBlocking {
                VertxMain.start(WebVerticle::class).join()
            }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testSimpleRoute() {

        runBlocking(vertx.dispatcher()) {
            webClient
                .get("/")
                .send()
                .await()
                .let {
                    Assert.assertEquals(200, it.statusCode())
                    Assert.assertEquals("OK", it.statusMessage())

                }
        }
    }

    @Test
    @Throws(Exception::class)
    fun testMethod() {


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

                    webClient
                        .request(method, path + meth.name())
                        .send()
                        .await()
                        .let {
                            if (meth == method) {
                                Assert.assertEquals(HttpResponseStatus.OK.code(), it.statusCode())
                            } else {
                                Assert.assertEquals(HttpResponseStatus.METHOD_NOT_ALLOWED.code(), it.statusCode())
                            }

                        }
                }

            }
        }
    }


    @Test
    @Throws(Exception::class)
    fun testParam() {

        runBlocking(vertx.dispatcher()) {
            val respUtil = WebConfig.responseUtil()

            val test1 = TestData("str", 123, false, JsonObject().put("json", "test"), JsonArray().add("aaa").add(111))

            val json = JsonObject.mapFrom(test1)


            webClient
                .request(HttpMethod.POST, "/foo1")
                .sendJson(test1)
                .await()
                .let {
                    Assert.assertEquals(HttpResponseStatus.OK.code(), it.statusCode())
                    val a = JsonObject(respUtil.successResponse(json).trim())
                    val b = json
                    Assert.assertEquals(a.getJsonObject("data"), b)

                    val buff = it.body()

                    val c = JsonObject(buff)
                    Assert.assertEquals(a, c)
                }
        }

        runBlocking(vertx.dispatcher()) {
            val respUtil = WebConfig.responseUtil()

            val test1 = TestData2("str", 123, false, Entity("test"))

            val json = JsonObject.mapFrom(test1)

            webClient
                .request(HttpMethod.POST, "/foo2")
                .sendJson(test1)
                .await()
                .let {
                    Assert.assertEquals(HttpResponseStatus.OK.code(), it.statusCode())
                    val a = JsonObject(respUtil.successResponse(json).trim())
                    val b = json
                    Assert.assertEquals(a.getJsonObject("data"), b)

                    val buff = it.body()

                    val c = JsonObject(buff)
                    Assert.assertEquals(a, c)
                }

        }

        runBlocking(vertx.dispatcher()) {
            val respUtil = WebConfig.responseUtil()

            val json = JsonObject().put("a", 1)

            val result = JsonObject().apply {
                val param = JsonObject().put("a", 1).put("b", "bbb").put("c", "ccc")
                put("map", param)
                put("json", param)
            }
            webClient
                .request(HttpMethod.POST, "/foo3/bbb?c=ccc")
                .sendJsonObject(json)
                .await()
                .let {
                    Assert.assertEquals(HttpResponseStatus.OK.code(), it.statusCode())
                    val a = JsonObject(respUtil.successResponse(result).trim())
                    val buff = it.body()
                    val c = JsonObject(buff)
                    Assert.assertEquals(a, c)
                }

        }

        runBlocking(vertx.dispatcher()) {
            val respUtil = WebConfig.responseUtil()

            val str = "abcdefg"
            val raw = io.vertx.core.buffer.Buffer.buffer(str)

            val result = JsonObject().apply {
                put("str", str)
                put("raw", raw)
            }

            webClient
                .request(HttpMethod.POST, "/foo4")
                .sendBuffer(raw)
                .await()
                .let {
                    Assert.assertEquals(HttpResponseStatus.OK.code(), it.statusCode())
                    val a = JsonObject(respUtil.successResponse(result).trim())
                    val buff = it.body()
                    val c = JsonObject(buff)
                    Assert.assertEquals(a, c)
                }
        }

        runBlocking(vertx.dispatcher()) {
            webClient
                .request(HttpMethod.POST, "/foo5")
                .send()
                .await()
                .let {
                    Assert.assertEquals(HttpResponseStatus.OK.code(), it.statusCode())
                    val buff = it.body()
                    val c = JsonObject(buff).getJsonObject("data")
                    val id = c.getLong("id")
                    val idStr = c.getString("idStr")
                    Assert.assertSame(id::class.simpleName, Long::class.simpleName)
                    Assert.assertSame(idStr::class.simpleName, String::class.simpleName)
                }

        }

        runBlocking(vertx.dispatcher()) {

            val buff = vertx.fileSystem().readFile(".htdigest").await()
            val respUtil = WebConfig.responseUtil()
            webClient
                .postAbs("http://127.0.0.1:8080/foo6")
                .sendMultipartForm(
                    MultipartForm.create()
                        .textFileUpload("file", "filename", io.vertx.core.buffer.Buffer.buffer("test"), "txt/plain")
                        .binaryFileUpload("file2", "name2", buff, "txt/plain")
                )
                .await()
                .let {
                    Assert.assertEquals(HttpResponseStatus.OK.code(), it.statusCode())
                    val result = it.bodyAsJsonObject().getJsonArray("data").map { it.toString() }.toSortedSet(STRING_COMPARATOR)
                    val expect =  JsonArray().add("test").add(buff.toString()).map { it.toString() }.toSortedSet(STRING_COMPARATOR)
                    Assert.assertEquals(expect, result)
                }


        }
    }

    @Test
    @Throws(Exception::class)
    fun testOrder() {


        runBlocking(vertx.dispatcher()) {

            WebClient.create(vertx)
                .getAbs("http://127.0.0.1:8080/order")
                .send()
                .await()
                .bodyAsString()
                .also {
                    Assert.assertEquals("bananasapplesoranges", it)
                }

        }
    }
}

@Controller
class TestController1 {
    @ROUTE("/")
    fun testSimpleRoute(@Context rc: RoutingContext) {
        rc.response().end()
    }
}

@Controller
class TestController2 {
    @ROUTE("/foo1")
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

    @ROUTE("/foo2")
    fun testParam2(
        @EntityParam entity: TestData2,
    ): TestData2 {
        return entity
    }


    @ROUTE("/foo3/:b")
    fun testParam3(
        @ParamsMap map: Map<String, Any>,
        @ParamsMap json: JsonObject,

        ): JsonObject {
        return JsonObject().put("map", map).put("json", json)
    }


    @ROUTE("/foo4")
    fun testParam4(
        @BodyString str: String,
        @BodyRaw raw: io.vertx.core.buffer.Buffer,

        ): JsonObject {
        return JsonObject().put("str", str).put("raw", raw)
    }

    @ROUTE("/foo5")
    fun testParam5(
        @Id id: Long,
        @IdString idStr: String,

        ): JsonObject {
        return JsonObject().put("id", id).put("idStr", idStr)
    }

    @ROUTE("/foo6")
    suspend fun testParam6(
        @FileUpload files: Set<io.vertx.ext.web.FileUpload>


    ): JsonArray {

        val r = files.map {
            val name = it.uploadedFileName()
            val fileBuff = VERTX.vertx().fileSystem().readFile(name).await()
            fileBuff.toString()
        }

        return JsonArray(r)
    }
}

@Controller
class TestController3 {
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


@Controller
class TestController4 {
    @ROUTE("/order")
    @Order(1)
    fun testSimpleRoute1(@Context rc: RoutingContext) {
        rc.response().write("bananas")
        rc.next()
    }

    @ROUTE("/order")
    @Order(5)
    fun testSimpleRoute3(@Context rc: RoutingContext) {
        rc.response().end("oranges")
    }

    @ROUTE("/order")
    @Order(4)
    fun testSimpleRoute2(@Context rc: RoutingContext) {
        rc.response().write("apples")
        rc.next()
    }
}