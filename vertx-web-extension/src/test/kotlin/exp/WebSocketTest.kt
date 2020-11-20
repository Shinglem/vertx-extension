package exp

import io.vertx.core.*
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpClientOptions
import io.vertx.core.http.HttpServer
import io.vertx.core.http.HttpServerOptions
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import io.vertx.ext.web.sstore.LocalSessionStore
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Supplier


class WebSocketTest {
    @Test
    fun test() {
        val begin = CountDownLatch(1)
        val vertx = Vertx.vertx()
        val server = vertx.createHttpServer()
        val router = Router.router(vertx)
        val options = SockJSHandlerOptions()
            .setHeartbeatInterval(2000)
        val sockJSHandler = SockJSHandler.create(vertx, options)

        val subRouter = sockJSHandler.socketHandler { sockJSSocket: SockJSSocket ->

            println("sockJSSocket")
            // Just echo the data back
            sockJSSocket.handler { data: Buffer ->
                println("data: ${data.toString()}")
                sockJSSocket.write(
                    "pong"
                ).onSuccess {
                    println("server resp success")
                }
            }
        }
        router.route("/static/*").handler(StaticHandler.create());
//        router.route("/myapp/*").handler(sockJSHandler)
        router.mountSubRouter("/myapp" , subRouter)
        server
            .requestHandler(router)
            .listen(8080) {
                if (it.succeeded()) {
                    println("server success")

                    client
                        .webSocket("/myapp/websocket") {
                            if (it.succeeded()) {
                                println("ws success")
                                val result = it.result()

                                result.handler {
                                    println("ws resp success")
                                    Assert.assertEquals("pong", it.toString())
                                    begin.countDown()
                                }
                                result.write(Buffer.buffer("ping")) {
                                    if (it.succeeded()) {
                                        println("ws req success")
                                    } else {
                                        println("ws req failed")
                                        it.cause().printStackTrace()
                                    }
                                }


                            } else {
                                println("ws failed")
                                it.cause().printStackTrace()
                            }
                        }


                } else {
                    println("server failed")
                    it.cause().printStackTrace()
                }
            }





        begin.await()

//        router.route()
//            .handler(
//                SessionHandler.create(LocalSessionStore.create(vertx))
//                    .setNagHttps(false)
//                    .setSessionTimeout(60 * 60 * 1000.toLong())
//            )

        /*  router.mountSubRouter("/myapp", sockJSHandler.socketHandler { sockJSSocket: SockJSSocket ->

              println("sockJSSocket")
              // Just echo the data back
              sockJSSocket.handler { data: Buffer ->
                  println("data: ${data.toString()}")
                  sockJSSocket.write(
                      "pong"
                  )
              }
          })*/



    }

    @Test
    fun test3() {
        val begin = CountDownLatch(1)
        val vertx = Vertx.vertx()
        val server = vertx.createHttpServer()
        val router = Router.router(vertx)
        val options = SockJSHandlerOptions().setHeartbeatInterval(2000)
        val sockJSHandler = SockJSHandler.create(vertx, options)
        sockJSHandler.socketHandler { sockJSSocket: SockJSSocket ->

            println("sockJSSocket")
            // Just echo the data back
            sockJSSocket.handler { data: Buffer ->
                println("data: ${data.toString()}")
                sockJSSocket.write(
                    "pong"
                ).onSuccess {
                    println("server resp success")
                }
            }
        }
//        router.route("/test/*").handler(sockJSHandler)
        router.route("/myapp/*").handler(sockJSHandler)
        server
            .requestHandler(router)
            .listen(8080) {
                if (it.succeeded()) {
                    println("server success")
                    val sv = it.result()
                    println(sv.actualPort())
//                    latch.countDown()

                    client
//                        .webSocket("/test/websocket")
                        .webSocket("/test/websocket") {

                        if (it.succeeded()) {
                            println("ws success")
                            val result = it.result()

                            result.handler {
                                println("ws resp success")
                                Assert.assertEquals("pong", it.toString())
                                begin.countDown()
                            }
                            result.write(Buffer.buffer("ping")) {
                                if (it.succeeded()) {
                                    println("ws req success")
                                } else {
                                    println("ws req failed")
                                    it.cause().printStackTrace()
                                }
                            }


                        } else {
                            println("ws failed")
                            it.cause().printStackTrace()
                        }
                    }


                } else {
                    println("server failed")
                    it.cause().printStackTrace()
                }
            }
        begin.await()
//        val str = "aaa"

//        val latch = CountDownLatch(1)
/*
        vertx.deployVerticle({
            object : AbstractVerticle() {
                @Throws(Exception::class)
                override fun start(startFuture: Promise<Void?>) {
                    val router = Router.router(vertx)
                    router.route()
                        .handler(
                            SessionHandler.create(LocalSessionStore.create(vertx))
                                .setNagHttps(false)
                                .setSessionTimeout(60 * 60 * 1000.toLong())
                        )

                    val options = SockJSHandlerOptions().setHeartbeatInterval(2000)
                    val sockJSHandler = SockJSHandler.create(vertx, options)
                    sockJSHandler.socketHandler(Handler { socket: SockJSSocket ->
                        socket.write(
                            Buffer.buffer(str),
                            onSuccess { v -> complete() })
                    })
                    router.route("/test/a").handler(sockJSHandler)
                    vertx.createHttpServer(HttpServerOptions()
                        .setPort(8080)
                        .setHost("localhost"))
                        .requestHandler(router)
                        .listen { ar: AsyncResult<HttpServer?> ->
                            if (ar.succeeded()) {
                                val sv = ar.result()
                                println(sv?.actualPort())
                                startFuture.complete()
                            } else {
                                startFuture.fail(ar.cause())
                            }
                        }
                }
            }
        },
            DeploymentOptions()
                .setInstances(1),
            onSuccess { id -> latch.countDown() })

*/
//        router.route()
//            .handler(
//                SessionHandler.create(LocalSessionStore.create(vertx))
//                    .setNagHttps(false)
//                    .setSessionTimeout(60 * 60 * 1000.toLong())
//            )

//        val subRouter = sockJSHandler.socketHandler(Handler { socket: SockJSSocket ->
//            socket.write(
//                Buffer.buffer("pong"),
//                onSuccess { v ->
////                    complete()
//                    println("server resp success")
//                })
//        })

//        router.mountSubRouter("/test/websocket" , subRouter)
//        vertx.createHttpServer(
//            HttpServerOptions()
//                .setPort(8080)
////                .setHost("localhost")
//        )

//        awaitLatch(latch)


//        client.webSocket("/test/websocket", onSuccess { ws ->
//            ws.handler { buffer ->
//                if (buffer.toString() == str) {
////                    complete()
//                    latch2.countDown()
//                }
//            }
//        })

        /*client.webSocket("/test/websocket") {
            if (it.succeeded()) {
                println("ws success")
                val result = it.result()

                result.handler {
                    println("ws resp success")
                    Assert.assertEquals("pong", it.toString())
                    latch2.countDown()
                }
                result.write(Buffer.buffer("ping")) {
                    if (it.succeeded()) {
                        println("ws req success")
                    } else {
                        println("ws req failed")
                        it.cause().printStackTrace()
                    }
                }


            } else {
                println("ws failed")
                it.cause().printStackTrace()
            }
        }*/

//        awaitLatch(latch2)


    }


    @Test
    fun test2() {
        waitFor(2)
        val str = "aaa"
        socketHandler =
            Supplier {
                Handler { socket: SockJSSocket ->
                    socket.write(
                        Buffer.buffer(str),
                        onSuccess { v -> complete() })
                }
            }

        startServers();
        client.webSocket("/test/websocket", onSuccess { ws ->
            ws.handler { buffer ->
                if (buffer.toString() == str) {
                    complete()
                }
            }
        })
        await();
    }

    protected fun await() {
        await(2, TimeUnit.MINUTES)
    }

    @Volatile
    private var awaitCalled = false
    fun await(delay: Long, timeUnit: TimeUnit?) {
        check(!awaitCalled) { "await() already called" }
        awaitCalled = true
        try {
            val ok = latch!!.await(delay, timeUnit)
            check(ok) {
                // timed out
                "Timed out in waiting for test complete"
            }
            rethrowError()
        } catch (e: InterruptedException) {
            throw IllegalStateException("Test thread was interrupted!")
        }
    }

    private fun rethrowError() {
        if (throwable != null) {
            if (throwable is Error) {
                throw (throwable as Error?)!!
            } else if (throwable is RuntimeException) {
                throw (throwable as RuntimeException?)!!
            } else {
                // Unexpected throwable- Should never happen
                throw IllegalStateException(throwable)
            }
        }
    }

    val vertx = Vertx.vertx()
    val client = vertx.createHttpClient(HttpClientOptions().setDefaultPort(8080).setKeepAlive(false))
    var preSockJSHandlerSetup: Consumer<Router>? = null

    @Throws(Exception::class)
    fun startServers() {
        val latch = CountDownLatch(1)
        vertx.deployVerticle({
            object : AbstractVerticle() {
                @Throws(Exception::class)
                override fun start(startFuture: Promise<Void?>) {
                    val router = Router.router(vertx)
                    router.route()
                        .handler(
                            SessionHandler.create(LocalSessionStore.create(vertx))
                                .setNagHttps(false)
                                .setSessionTimeout(60 * 60 * 1000.toLong())
                        )
                    preSockJSHandlerSetup?.accept(router)

                    val options = SockJSHandlerOptions().setHeartbeatInterval(2000)
                    val sockJSHandler = SockJSHandler.create(vertx, options)
                    sockJSHandler.socketHandler(socketHandler!!.get())
                    router.route("/test/*").handler(sockJSHandler)
                    vertx.createHttpServer(HttpServerOptions().setPort(8080).setHost("localhost"))
                        .requestHandler(router)
                        .listen { ar: AsyncResult<HttpServer?> ->
                            if (ar.succeeded()) {
                                val sv = ar.result()
                                println(sv?.actualPort())
                                startFuture.complete()
                            } else {
                                startFuture.fail(ar.cause())
                            }
                        }
                }
            }
        }, DeploymentOptions().setInstances(1), onSuccess { id -> latch.countDown() })




        awaitLatch(latch)
    }

    @Throws(InterruptedException::class)
    protected fun awaitLatch(latch: CountDownLatch) {
        awaitLatch(latch, 10, TimeUnit.SECONDS)
    }

    @Throws(InterruptedException::class)
    protected fun awaitLatch(latch: CountDownLatch, timeout: Long, unit: TimeUnit?) {
        assertTrue(latch.await(timeout, unit))
    }

    protected fun assertTrue(condition: Boolean) {
        checkThread()
        try {
            Assert.assertTrue(condition)
        } catch (e: AssertionError) {
            handleThrowable(e)
        }
    }

    @Volatile
    private var tearingDown = false

    @Synchronized
    protected fun complete() {
        check(!tearingDown) { "testComplete called after test has completed" }
        checkThread()
        check(!testCompleteCalled) { "already complete" }
        latch!!.countDown()
        if (latch!!.count == 0L) {
            testCompleteCalled = true
        }
    }

    var socketHandler: Supplier<Handler<SockJSSocket>>? = null
    private var latch: CountDownLatch? = null

    @Synchronized
    private fun waitFor(count: Int) {
        latch = CountDownLatch(count)
    }

    protected fun <T> onSuccess(consumer: Consumer<T>): Handler<AsyncResult<T>>? {
        return Handler { result: AsyncResult<T> ->
            if (result.failed()) {
                result.cause().printStackTrace()
                fail(result.cause().message)
            } else {
                consumer.accept(result.result())
            }
        }
    }

    protected fun fail(message: String?) {
        checkThread()
        try {
            Assert.fail(message)
        } catch (e: AssertionError) {
            handleThrowable(e)
        }
    }

    protected fun checkThread() {
        threadNames[Thread.currentThread().name] = Exception()
    }

    private val threadNames = ConcurrentHashMap<String, Exception>()

    @Volatile
    private var testCompleteCalled = false

    @Volatile
    private var lateFailure = false

    @Volatile
    private var throwable: Throwable? = null

    @Volatile
    private var thrownThread: Thread? = null
    private fun handleThrowable(t: Throwable) {
        if (testCompleteCalled) {
            lateFailure = true
            throw IllegalStateException("assert or failure occurred after test has completed", t)
        }
        throwable = t
        t.printStackTrace()
        thrownThread = Thread.currentThread()
        latch!!.countDown()
        if (t is AssertionError) {
            throw t
        }
    }
}