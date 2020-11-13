package web.extension.test

import io.github.shinglem.core.main.VERTX
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.*
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.kotlin.coroutines.await
import org.junit.Assert
import java.util.function.Consumer


open class BaseWebTest {

    val vertx = VERTX.vertx()
    private val httpClientOptions = HttpClientOptions().setDefaultPort(8080)
    private val client = vertx.createHttpClient()
    val webClient = WebClient.create(vertx, WebClientOptions(httpClientOptions))

}