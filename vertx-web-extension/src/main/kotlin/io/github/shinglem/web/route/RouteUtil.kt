package io.github.shinglem.web.route

import io.github.shinglem.web.response.ResponseUtil
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.LoggerHandler
import org.slf4j.LoggerFactory

interface RouteUtil {
    fun registerFullRoute(router : Router , responseUtil: ResponseUtil)
}

object DefaultRouteUtil : RouteUtil {
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun registerFullRoute(router : Router , responseUtil: ResponseUtil){

        router.route().order(-10000).handler(BodyHandler.create())
        router.route().order(-10000).handler(LoggerHandler.create())

        router.errorHandler(404) {
            logger.info("[404 handler]");
            it.response().statusCode = 404
            if (it.request().method() != HttpMethod.HEAD) {
                // If it's a 404 let's send a body too
                it.response()
                    .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .end(responseUtil.errorResponse("404 not found"))
            } else {
                it.response().end()
            }
        }

        router.errorHandler(500) {
            logger.info("[500 handler]");
            it.response().statusCode = 500
            if (it.request().method() != HttpMethod.HEAD) {
                // If it's a 404 let's send a body too
                it.response()
                    .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .end(responseUtil.errorResponse("Internal Server Error"))
            } else {
                it.response().end()
            }
        }

        router.errorHandler(403) {
            logger.info("[403 handler]");
            it.response().statusCode = 403
            if (it.request().method() != HttpMethod.HEAD) {
                // If it's a 404 let's send a body too
                it.response()
                    .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                    .end(responseUtil.errorResponse("403 forbiden"))
            } else {
                it.response().end()
            }
        }
        router.route().failureHandler {
            logger.info("[failure Handler]");


            val response = it.response()
            response.putHeader("content-type", "text/xml; charset=\"utf-8\"")



            when (val errorCode = it.statusCode()) {

                else -> {
                    logger.info("[$errorCode fail]");
                    it.response().statusCode = errorCode
                    if (it.request().method() != HttpMethod.HEAD) {
                        // If it's a 404 let's send a body too
                        it.response()
                            .putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
                            .end(responseUtil.errorResponse("Internal Server Error"))
                    } else {
                        it.response().end()
                    }
                }
            }


        }
        router.route().order(-1000).handler { rc ->

            val response = rc.response()
            response.isChunked = true
            response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON)
            rc.next()
        }
    }

}