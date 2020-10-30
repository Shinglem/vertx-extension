package io.github.shinglem.web.config

import io.github.shinglem.core.main.VertxConfig
import io.github.shinglem.util.ClassUtilFactory
import io.github.shinglem.web.controller.ControllerUtil
import io.github.shinglem.web.controller.DefaultControllerUtil
import io.github.shinglem.web.response.DefaultResponseUtil
import io.github.shinglem.web.response.ResponseUtil
import io.github.shinglem.web.route.DefaultRouteUtil
import io.github.shinglem.web.route.RouteUtil
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get

object WebConfig {

    private val config = VertxConfig.config().getJsonObject("vertx")["web"] ?: JsonObject()

    fun httpServerOptions() : HttpServerOptions {

        val options = config.getJsonObject("httpServerOptions") ?: JsonObject()

        val httpServerOptions = HttpServerOptions(options)

        return httpServerOptions

    }

    fun responseUtil(): ResponseUtil {
        val util = (config.getString("responseUtil") ?: "default")
            .let {
                if("default" == it){
                    return@let DefaultResponseUtil
                }
                return@let ClassUtilFactory().getClassUtil().getInstance(it) as ResponseUtil
            }

        return util
    }

    fun routeUtil(): RouteUtil {
        val util = (config.getString("routeUtil") ?: "default")
            .let {
                if("default" == it){
                    return@let DefaultRouteUtil
                }
                return@let ClassUtilFactory().getClassUtil().getInstance(it) as RouteUtil
            }

        return util
    }

    fun controllerUtil(): ControllerUtil {
        val util = (config.getString("controllerUtil") ?: "default")
            .let {
                if("default" == it){
                    return@let DefaultControllerUtil
                }
                return@let ClassUtilFactory().getClassUtil().getInstance(it) as ControllerUtil
        }

        return util
    }


}