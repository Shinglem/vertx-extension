package web.extension.test

import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.multipart.MultipartForm
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SomeTest {

    @Test
    fun tset() {
        val vertx = Vertx.vertx()
        runBlocking(vertx.dispatcher()) {

            val router = Router.router(vertx)

            router.route().handler(BodyHandler.create())
            router.route("/some/path/multipart")
//                .handler {
//                    it.request().setExpectMultipart(true)
//                    it.next()
//                }
                .handler{ctx ->

                val uploads = ctx.fileUploads()
                    .map {
                        listOf(
                            it.name() ,
                            it.fileName() ,
                            it.uploadedFileName(),
                            it.charSet() ,
                            it.contentType() ,
                            it.contentTransferEncoding() ,
                            it.size().toString()


                            )
                    }
                println(uploads)


            }.failureHandler {
                    val a = it.failure()
                    a.printStackTrace()
                }

            router.route("/some/path/uploads")
                .handler {

                    it.body.apply {
                        println(this)
                    }

                }.failureHandler {
                    val a = it.failure()
                    a.printStackTrace()
                }


            vertx.createHttpServer().requestHandler(router).listen().await()



            val buff = Buffer.buffer("abcdefg")



            val fileBuff = vertx.fileSystem().readFile(".htdigest").await()

            WebClient.create(vertx)
                .postAbs("http://127.0.0.1/some/path/multipart")
                .sendMultipartForm(
                    MultipartForm.create()
                        .textFileUpload("file" , "filename" , buff , "txt/strings")
                        .binaryFileUpload("file2" , "name2" , fileBuff, "txt/strings")
                )
                .await()
                .bodyAsString()
                .also {
                    println(it)
                }

        }
    }

}