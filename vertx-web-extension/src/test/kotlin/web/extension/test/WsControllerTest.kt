package web.extension.test

import io.github.shinglem.core.main.VertxMain
import io.github.shinglem.util.safeCastTo
import io.github.shinglem.web.annotions.WebSocketController
import io.github.shinglem.web.annotions.WebSocketPath
import io.github.shinglem.web.verticle.WebVerticle
import io.vertx.core.buffer.Buffer
import io.vertx.core.streams.ReadStream
import io.vertx.core.streams.WriteStream
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.coroutines.toChannel
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test
import java.time.LocalDateTime


class WsControllerTest : BaseWebTest() {

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
    fun testWs() {

        runBlocking(vertx.dispatcher()) {
            client
                .webSocket(8080, "127.0.0.1", "/ws/test1/websocket")
                .await()
                .apply {
                    this.write(Buffer.buffer("ping")).await()
                }
                .safeCastTo<ReadStream<Buffer>>()!!
                .toChannel(vertx)
                .receive()
                .let {
                    println(it.toString())
                    Assert.assertEquals("pong", it.toString())
                }

        }
    }


}

@WebSocketController
class TestWsController1 {
    @WebSocketPath("/ws/test1/")
    fun testSimpleRoute(sockJSSocket: SockJSSocket) {
        sockJSSocket.handler { data: Buffer ->
            sockJSSocket.write(
                "pong"
            )
        }
    }
}



