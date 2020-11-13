package io.github.shinglem.web.verticle

import io.github.shinglem.core.main.VERTX
import io.github.shinglem.log.LoggerFactory
import io.github.shinglem.util.consoletable.PrettyTable
import io.github.shinglem.vertx.ext.asyncBlockingJob
import io.github.shinglem.web.config.WebConfig
import io.vertx.ext.web.Router
import io.vertx.ext.web.impl.RouteStateImpl
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.await
import kotlin.reflect.full.functions


class WebVerticle() : CoroutineVerticle() {


    private val logger = LoggerFactory.getLogger(this::class.java.name)

    private val options = WebConfig.httpServerOptions()

    private val respUtil = WebConfig.responseUtil()

    private val routeUtil = WebConfig.routeUtil()

    private val controllerUtil = WebConfig.controllerUtil()

    private val controllerPack = WebConfig.controllerPack()

    override suspend fun start() {
        logger.debug("---------web start---------" + this.deploymentID)

        val router: Router = Router.router(vertx)

        val server = vertx.createHttpServer(options)

        routeUtil.registerFullRoute(router, respUtil)

//        controllerUtil.regist(controllerPack, router, VERTX, respUtil).await()
        routeUtil.registerFullRoute(router, respUtil)
        controllerUtil.regist(controllerPack, router, VERTX, respUtil)
//        vertx.asyncBlockingJob<Unit> {
//
//
//        }.join()


        if (logger.isDebugEnabled) {
            logger.debug(
                "routes => \n${
                    PrettyTable.fieldNames(
                        "path",
                        "name",
                        "order",
                        "enable",
                        "method",
                        "consumes",
                        "produces",
                        "pattern",
                        "groups"
                    )
                        .addRows(router.routes.map {
                            val state = RouteStateImpl(it)
                            arrayOf(state.path, state.name , state.order , state.enabled , state.methods , state.consumes , state.produces , state.pattern , state.groups)
                        }).toString()

                }"
            )
        }


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

