package io.github.shinglem.web.verticle

import io.github.shinglem.core.main.VERTX
import io.github.shinglem.log.LoggerFactory
import io.github.shinglem.util.consoletable.PrettyTable
import io.github.shinglem.vertx.ext.asyncBlockingJob
import io.github.shinglem.web.config.StaticSourceConfig
import io.github.shinglem.web.config.WebConfig
import io.github.shinglem.web.config.WsConfig
import io.github.shinglem.web.exceptions.StaticRouteRegistException
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Http2PushMapping
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandler
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

    private val isWsOpen = WsConfig.isOpen()

    private val sockJSHandlerOptions = WsConfig.sockJSHandlerOptions()

    private val isStaticOpen = StaticSourceConfig.isOpen()

    private val staticHandlerOptions = StaticSourceConfig.staticHandlerOptions()

    override suspend fun start() {
        logger.debug("---------web start---------" + this.deploymentID)

        val router: Router = Router.router(vertx)

        val server = vertx.createHttpServer(options)

        routeUtil.registerFullRoute(router, respUtil)
        controllerUtil.regist(controllerPack, router, VERTX, respUtil)

        if (isWsOpen) {
            logger.info("websocket is open ...... ")
            val sockJSHandler = SockJSHandler.create(vertx, sockJSHandlerOptions)
            controllerUtil.registWebsocket(controllerPack, router, VERTX, sockJSHandler)
        }


        if (isStaticOpen) {
            logger.info("static source is open ...... ")
            val staticHandler = StaticHandler.create()
            if (staticHandlerOptions.getString("path") == null
                || !staticHandlerOptions.getString("path").endsWith("*")
            ) {
                throw StaticRouteRegistException("static path : ${staticHandlerOptions.getString("path")}  =>  static path need a string start with '/' and end with '*' ")
            }
            val path = staticHandlerOptions.getString("path")
            staticHandler.apply {

                staticHandlerOptions.getBoolean("allowRootFileSystemAccess")?.let {
                    setAllowRootFileSystemAccess(it)
                }
                staticHandlerOptions.getString("webRoot")?.let {
                    setWebRoot(it)
                }
                staticHandlerOptions.getBoolean("filesReadOnly")?.let {
                    setFilesReadOnly(it)
                }
                staticHandlerOptions.getLong("maxAgeSeconds")?.let {
                    setMaxAgeSeconds(it)
                }
                staticHandlerOptions.getBoolean("cachingEnabled")?.let {
                    setCachingEnabled(it)
                }
                staticHandlerOptions.getBoolean("directoryListing")?.let {
                    setDirectoryListing(it)
                }
                staticHandlerOptions.getBoolean("includeHidden")?.let {
                    setIncludeHidden(it)
                }
                staticHandlerOptions.getLong("cacheEntryTimeoutn")?.let {
                    setCacheEntryTimeout(it)
                }
                staticHandlerOptions.getString("indexPage")?.let {
                    setIndexPage(it)
                }
                staticHandlerOptions.getInteger("maxCacheSize")?.let {
                    setMaxCacheSize(it)
                }
                staticHandlerOptions.getJsonArray("http2PushMappings")?.let {
                    val list = it.map {
                        (it as JsonObject).mapTo(Http2PushMapping::class.java)
                    }
                    setHttp2PushMapping(list)
                }
                staticHandlerOptions.getJsonArray("skipCompressionForMediaTypes")?.let {
                    val list = it.map {
                        it as String
                    }.toSet()
                    skipCompressionForMediaTypes(list)
                }
                staticHandlerOptions.getJsonArray("skipCompressionForSuffixes")?.let {
                    val list = it.map {
                        it as String
                    }.toSet()
                    skipCompressionForSuffixes(list)
                }
                staticHandlerOptions.getBoolean("alwaysAsyncFS")?.let {
                    setAlwaysAsyncFS(it)
                }
                staticHandlerOptions.getBoolean("enableFSTuning")?.let {
                    setEnableFSTuning(it)
                }
                staticHandlerOptions.getLong("maxAvgServeTimeNs")?.let {
                    setMaxAvgServeTimeNs(it)
                }
                staticHandlerOptions.getString("directoryTemplate")?.let {
                    setDirectoryTemplate(it)
                }
                staticHandlerOptions.getBoolean("rangeSupport")?.let {
                    setEnableRangeSupport(it)
                }
                staticHandlerOptions.getBoolean("sendVaryHeader")?.let {
                    setSendVaryHeader(it)
                }
                staticHandlerOptions.getString("defaultContentEncoding")?.let {
                    setDefaultContentEncoding(it)
                }


            }

            router.route(path).handler(staticHandler);
        }


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
                            arrayOf(
                                state.path,
                                state.name,
                                state.order,
                                state.enabled,
                                state.methods,
                                state.consumes,
                                state.produces,
                                state.pattern,
                                state.groups
                            )
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

