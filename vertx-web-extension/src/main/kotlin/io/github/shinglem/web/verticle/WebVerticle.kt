package io.github.shinglem.web.verticle

import io.github.shinglem.log.LoggerFactory
import io.github.shinglem.vertx.ext.asyncBlockingJob
import io.github.shinglem.web.config.WebConfig
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.CoroutineVerticle


class WebVerticle() : CoroutineVerticle() {


    private val logger = LoggerFactory.getLogger(this::class.java.name)

    private val options = WebConfig.httpServerOptions()

    private val respUtil = WebConfig.responseUtil()

    private val routeUtil = WebConfig.routeUtil()

    private val controllerUtil = WebConfig.controllerUtil()

    override suspend fun start() {
        logger.debug("---------web start---------" + this.deploymentID)

        val router: Router = Router.router(vertx)

        val server = vertx.createHttpServer(options)


        vertx.asyncBlockingJob<Unit> {

            routeUtil.registerFullRoute(router, respUtil)

            controllerUtil.regist("" , router , this , respUtil)


        }.join()


        logger.debug("routes => ${router.routes}")

        server
            .requestHandler(router)
            .listen() {
                if (it.succeeded()) {
                    val s = it.result()
                    logger.info("web start success , listening ${s.actualPort()}")
                } else {
                    val cause = it.cause()
                    logger.error("web start fail =>", cause)
                }
            }

    }


}

