package io.github.shinglem.web.controller

import io.github.shinglem.core.main.VertxProducer
import io.github.shinglem.web.response.ResponseUtil
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.sockjs.SockJSHandler


interface ControllerUtil {
    fun regist(pack: String = "", router: Router,
               vertxProducer: VertxProducer, respUtil: ResponseUtil)

    fun registWebsocket(pack: String = "", router: Router,
               vertxProducer: VertxProducer, sockJSHandler: SockJSHandler
    )
}






