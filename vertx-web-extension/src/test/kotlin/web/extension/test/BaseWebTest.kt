package web.extension.test

import io.github.shinglem.core.main.VERTX
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.*
import io.vertx.kotlin.coroutines.await
import org.junit.Assert
import java.util.function.Consumer


open class BaseWebTest {

    val vertx = VERTX.vertx()
    private val httpClientOptions = HttpClientOptions().setDefaultPort(8080)
    private val client = vertx.createHttpClient()

    protected open fun normalizeLineEndingsFor(buff: Buffer): Buffer? {
        val buffLen: Int = buff.length()
        val normalized: Buffer = Buffer.buffer(buffLen)
        for (i in 0 until buffLen) {
            val unsignedByte: Short = buff.getUnsignedByte(i)
            if (unsignedByte != '\r'.toShort() || i + 1 == buffLen || buff.getUnsignedByte(i + 1) != '\n'.toShort()) {
                normalized.appendUnsignedByte(unsignedByte)
            }
        }
        return normalized
    }

    @Throws(Exception::class)
    protected suspend fun testRequest(
        method: HttpMethod?, path: String?, statusCode: HttpResponseStatus,
        requestBody: Any? = null
    ) {
        testRequest(method, path, null, statusCode.code(), statusCode.reasonPhrase(), "", requestBody)
    }


    @Throws(Exception::class)
    protected suspend fun testRequest(
        method: HttpMethod?, path: String?, statusCode: Int, statusMessage: String?,
        requestBody: Any? = null
    ) {
        testRequest(method, path, null, statusCode, statusMessage, "", requestBody)
    }


    @Throws(Exception::class)
    protected suspend fun testRequest(
        method: HttpMethod?, path: String?,
        statusCode: HttpResponseStatus,
        responseBody: String?,
        requestBody: String?
    ) {
        testRequest(method, path, null, null, statusCode.code(), statusCode.reasonPhrase(), responseBody, requestBody)
    }

    protected suspend fun testRequest(
        method: HttpMethod?, path: String?,
        statusCode: HttpResponseStatus,
        responseBody: String? = null,
        requestBody: Any? = null,
        responseAssert :  suspend (resp : HttpClientResponse) -> Unit
    ) {
        testRequest(method, path, null, null, statusCode.code(), statusCode.reasonPhrase(), responseBody, requestBody , responseAssert = responseAssert)
    }


    @Throws(Exception::class)
    protected suspend fun testRequest(
        method: HttpMethod?, path: String?, requestAction: Consumer<HttpClientRequest?>?,
        statusCode: Int, statusMessage: String?,
        responseBody: String?,
        requestBody: Any? = null
    ) {
        testRequest(method, path, requestAction, null, statusCode, statusMessage, responseBody, requestBody)
    }

    @Throws(Exception::class)
    protected suspend fun testRequest(
        method: HttpMethod?,
        path: String?,
        requestAction: Consumer<HttpClientRequest?>?,
        responseAction: Consumer<HttpClientResponse?>?,
        statusCode: Int,
        statusMessage: String?,
        responseBody: String?,
        requestBody: Any? = null
    ) {
        testRequestBuffer(
            method,
            path,
            requestAction,
            responseAction,
            statusCode,
            statusMessage,
            if (responseBody != null) Buffer.buffer(responseBody) else null,
            true, requestBody
        )
    }

    @Throws(Exception::class)
    protected suspend fun testRequest(
        method: HttpMethod?,
        path: String?,
        requestAction: Consumer<HttpClientRequest?>?,
        responseAction: Consumer<HttpClientResponse?>?,
        statusCode: Int,
        statusMessage: String?,
        responseBody: String?,
        requestBody: Any? = null,
        responseAssert : suspend (resp : HttpClientResponse) -> Unit
    ) {
        testRequestBuffer(
            method,
            path,
            requestAction,
            responseAction,
            statusCode,
            statusMessage,
            if (responseBody != null) Buffer.buffer(responseBody) else null,
            true, requestBody,
            responseAssert = responseAssert
        )
    }


    @Throws(Exception::class)
    protected suspend fun testRequestBuffer(
        method: HttpMethod?,
        path: String?,
        requestAction: Consumer<HttpClientRequest?>?,
        responseAction: Consumer<HttpClientResponse?>?,
        statusCode: Int,
        statusMessage: String?,
        responseBodyBuffer: Buffer?,
        normalizeLineEndings: Boolean,
        requestBody: Any? = null
    ) {
        testRequestBuffer(
            client,
            method,
            8080,
            path,
            requestAction,
            responseAction,
            statusCode,
            statusMessage,
            responseBodyBuffer,
            normalizeLineEndings, requestBody
        )
    }

    @Throws(Exception::class)
    protected suspend fun testRequestBuffer(
        method: HttpMethod?,
        path: String?,
        requestAction: Consumer<HttpClientRequest?>?,
        responseAction: Consumer<HttpClientResponse?>?,
        statusCode: Int,
        statusMessage: String?,
        responseBodyBuffer: Buffer?,
        normalizeLineEndings: Boolean,
        requestBody: Any? = null ,
        responseAssert : suspend (resp : HttpClientResponse) -> Unit
    ) {
        testRequestBuffer(
            client,
            method,
            8080,
            path,
            requestAction,
            responseAction,
            statusCode,
            statusMessage,
            responseBodyBuffer,
            normalizeLineEndings, requestBody
        , responseAssert = responseAssert
        )
    }

    @Throws(Exception::class)
    protected suspend fun testRequestBuffer(
        client: HttpClient?,
        method: HttpMethod?,
        port: Int,
        path: String?,
        requestAction: Consumer<HttpClientRequest?>?,
        responseAction: Consumer<HttpClientResponse?>?,
        statusCode: Int,
        statusMessage: String?,
        responseBodyBuffer: Buffer?,
        normalizeLineEndings: Boolean,
        requestBody: Any? = null
    ) {
        testRequestBuffer(
            client!!,
            RequestOptions().setMethod(method).setPort(port).setURI(path).setHost("localhost"),
            requestAction,
            responseAction,
            statusCode,
            statusMessage,
            responseBodyBuffer,
            normalizeLineEndings, requestBody
        )
    }

    @Throws(Exception::class)
    protected suspend fun testRequestBuffer(
        client: HttpClient?,
        method: HttpMethod?,
        port: Int,
        path: String?,
        requestAction: Consumer<HttpClientRequest?>?,
        responseAction: Consumer<HttpClientResponse?>?,
        statusCode: Int,
        statusMessage: String?,
        responseBodyBuffer: Buffer?,
        normalizeLineEndings: Boolean,
        requestBody: Any? = null ,
        responseAssert : suspend (resp : HttpClientResponse) -> Unit
    ) {
        testRequestBuffer(
            client!!,
            RequestOptions().setMethod(method).setPort(port).setURI(path).setHost("localhost"),
            requestAction,
            responseAction,
            statusCode,
            statusMessage,
            responseBodyBuffer,
            normalizeLineEndings, requestBody
        , assertResp = responseAssert
        )
    }


    @Throws(Exception::class)
    protected suspend fun testRequestBuffer(
        client: HttpClient?,
        requestOptions: RequestOptions?,
        requestAction: Consumer<HttpClientRequest?>?,
        responseAction: Consumer<HttpClientResponse?>?,
        statusCode: Int,
        statusMessage: String?,
        responseBodyBuffer: Buffer?,
        requestBody: Any? = null
    ) {
        testRequestBuffer(
            client!!,
            requestOptions,
            requestAction,
            responseAction,
            statusCode,
            statusMessage,
            responseBodyBuffer,
            false, requestBody
        )
    }

    @Throws(Exception::class)
    protected suspend fun testRequestBuffer(
        client: HttpClient,
        requestOptions: RequestOptions?,
        requestAction: Consumer<HttpClientRequest?>?,
        responseAction: Consumer<HttpClientResponse?>?,
        statusCode: Int,
        statusMessage: String?,
        responseBodyBuffer: Buffer?,
        normalizeLineEndings: Boolean,
        requestBody: Any? = null,
        assertStatus: suspend (resp : HttpClientResponse) -> Unit  = {resp ->
            Assert.assertEquals(statusCode, resp.statusCode())
            Assert.assertEquals(statusMessage, resp.statusMessage())
            responseAction?.accept(resp)
    },
        assertResp : suspend (resp : HttpClientResponse) -> Unit = { resp ->
              resp?.let {
                var buff = it.body()
                    .await()
                if (normalizeLineEndings) {
                    buff = normalizeLineEndingsFor(buff)
                }
                Assert.assertEquals(responseBodyBuffer, buff)
            }

        }
    ) {

        val r = client.request(requestOptions)
            .await()

            .apply {
                requestAction?.accept(this)

                if (requestBody != null) {
                    when {
                        (requestBody is String) -> {
                            this.send(requestBody)
                        }
                        (requestBody is Buffer) -> {
                            this.send(requestBody)
                        }

                        else -> {
                            error("requestBody : $requestBody ${requestBody::class} not support")
                        }
                    }
                } else {
                    this.end()
                }
            }
            .await()
            .let { resp ->
                assertStatus(resp)
                assertResp(resp)
                resp
            }


    }

/*
        @Throws(Exception::class)
    protected suspend fun testRequestBuffer(
        client: HttpClient,
        requestOptions: RequestOptions?,
        requestAction: Consumer<HttpClientRequest?>?,
        responseAction: Consumer<HttpClientResponse?>?,
        statusCode: Int,
        statusMessage: String?,
        responseBodyBuffer: Buffer?,
        normalizeLineEndings: Boolean,
        requestBody: Any? = null
    ) {

        val r = client.request(requestOptions)
            .await()

            .apply {
                requestAction?.accept(this)

                if (requestBody != null) {
                    when {
                        (requestBody is String) -> {
                            this.send(requestBody)
                        }
                        (requestBody is Buffer) -> {
                            this.send(requestBody)
                        }

                        else -> {
                            error("requestBody : $requestBody ${requestBody::class} not support")
                        }
                    }
                } else {
                    this.end()
                }
            }
            .await()
            .let { resp ->
                Assert.assertEquals(statusCode, resp.statusCode())
                Assert.assertEquals(statusMessage, resp.statusMessage())
                responseAction?.accept(resp)

                resp?.let {
                    var buff = it.body()
                        .await()
                    if (normalizeLineEndings) {
                        buff = normalizeLineEndingsFor(buff)
                    }
                    Assert.assertEquals(responseBodyBuffer, buff)
                }

                resp
            }

    }*/

}