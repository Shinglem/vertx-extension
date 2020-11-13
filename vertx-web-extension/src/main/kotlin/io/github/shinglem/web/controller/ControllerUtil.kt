package io.github.shinglem.web.controller

import io.github.shinglem.core.main.VertxProducer
import io.github.shinglem.web.response.ResponseUtil
import io.vertx.ext.web.Router


interface ControllerUtil {
    fun regist(pack: String = "", router: Router,
               vertxProducer: VertxProducer, respUtil: ResponseUtil)
}






