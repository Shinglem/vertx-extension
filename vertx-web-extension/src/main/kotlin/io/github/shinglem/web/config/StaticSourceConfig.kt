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
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions
import io.vertx.kotlin.core.json.get

object StaticSourceConfig {

    private val config = VertxConfig.config().getJsonObject("vertx").getJsonObject("web")["static"] ?: JsonObject()



    fun staticHandlerOptions() : JsonObject {

        val options = config.getJsonObject("staticHandlerOptions") ?: JsonObject()


        return options

    }


    fun isOpen() : Boolean = config["open"] ?: false


}