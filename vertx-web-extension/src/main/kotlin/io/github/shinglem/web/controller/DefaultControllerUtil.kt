package io.github.shinglem.web.controller

import io.github.shinglem.core.main.VertxProducer
import io.github.shinglem.util.ClassUtilFactory
import io.github.shinglem.util.safeCastTo
import io.github.shinglem.web.annotions.*
import io.github.shinglem.web.annotions.ROUTE_ANNOTATION_MAP
import io.github.shinglem.web.exceptions.*
import io.github.shinglem.web.response.ResponseUtil
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.ext.web.handler.sockjs.SockJSSocket
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure


open class RouteInfo(
    val path: String,
    vararg val method: HttpMethod
)


data class RegistInfo(
    val klazz: KClass<out Any>,
    val route: List<FunctionInfo>
)

data class FunctionInfo(
    val func: KFunction<*>,
    val routeInfos: List<RouteInfo>,
    val order: Int,
    val blocking: Boolean,
    val regex: Boolean,
    val consumes: List<String>,
    val produces: List<String>
)


object DefaultControllerUtil : ControllerUtil {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val classUtil = ClassUtilFactory().getClassUtil()
    private lateinit var responseUtil: ResponseUtil

    override fun regist(
        pack: String, router: Router,
        vertxProducer: VertxProducer, respUtil: ResponseUtil
    ) {
        responseUtil = respUtil

        registControllers(getRegistInfo(getControllers(pack)), router, vertxProducer)
    }

    override fun registWebsocket(
        pack: String, router: Router,
        vertxProducer: VertxProducer, sockJSHandler: SockJSHandler
    ) {
        registWebSocketControllers(
            getWebSocketRegistInfo(
                getWebSocketControllers(pack)
            ), router, vertxProducer, sockJSHandler
        )
    }

    private fun getWebSocketControllers(pack: String = ""): List<KClass<out Any>> {

        val classes = classUtil.getClasses(pack)

        val filted = classes
            .filter {

                val ann = it.getAnnotation(WebSocketController::class.java)
                ann != null
//                val list = it.annotations.toList()
////                val b = list.any {
////                    it.annotationClass.isSubclassOf(WebSocketController::class)
////                }
//
//                val b = list.any { it is WebSocketController }

//                b
            }.also {
                logger.debug("websocket controllers =>   $it")
            }

        val model = filted.map {

            val kt = it.kotlin
            kt
        }

        return model

    }

    private fun getWebSocketRegistInfo(controllers: List<KClass<out Any>>): List<Pair<KClass<out Any>, List<KFunction<*>>>> {
        val info = controllers
            .map { klazz ->
                val routeInfo = klazz.functions
                    .filter { func ->

                        val wsFuncs = func
                            .annotations.any {
                                it is WebSocketPath
                            }

                        wsFuncs

                    }

                klazz to routeInfo
            }
        return info
    }


    private fun registWebSocketControllers(
        infos: List<Pair<KClass<out Any>, List<KFunction<*>>>>,
        router: Router,
        vertxProducer: VertxProducer,
        sockJSHandler: SockJSHandler
    ) {

        val vertx = vertxProducer.vertx()
        infos.forEach { (controllerKlz, funcs) ->
            val controller = classUtil.getInstance(controllerKlz)

            funcs.forEach { func ->
                val path = func.annotations
                    .filterIsInstance<WebSocketPath>()
                    .last()
                    .path
                    .apply {
                        if (this.endsWith("/"))
                            this.removeSuffix("/")
                    }

                val param = func.parameters
                    .filter { it.kind == KParameter.Kind.VALUE }
                    .let {
//                        if (!(it.size == 1
//                                    && it.first().type.jvmErasure.isSuperclassOf(SockJSSocket::class)
//                                    && (func.returnType.jvmErasure.isSubclassOf(Unit::class)
//                                    || func.returnType.jvmErasure.isSubclassOf(Void::class)
//                                    ))
//                        ) {
                        if (!(it.size == 1
                                    && it.first().type.jvmErasure == SockJSSocket::class
                                    && (func.returnType.jvmErasure == Unit::class
                                    || func.returnType.jvmErasure == Void::class
                                    ))
                        ) {
                            throw ParamNotSupportException("${controllerKlz.simpleName}#${func.name} => websocket controller function can have only one parameter with type io.vertx.ext.web.handler.sockjs.SockJSSocket and have no return")
                        }
                        it.first()
                    }

                router.mountSubRouter(path, sockJSHandler.socketHandler { sockJSSocket: SockJSSocket ->
                    CoroutineScope(vertx.dispatcher()).launch {

                        val params = mapOf(
                            param to sockJSSocket,
                            func.instanceParameter!! to controller
                        )
                        val result = func.callSuspendBy(params)
                    }
                })


            }

        }
    }


    private fun getControllers(pack: String = ""): List<KClass<out Any>> {

        val classes = classUtil.getClasses(pack)

        val filted = classes
            .filter {
                val ann = it.getAnnotation(Controller::class.java)
                ann != null
//                val list = it.annotations.toList()
//                val b = list.any {
//                    it is Controller
//                }
//
//                b
            }.also {
                logger.debug("controllers =>   $it")
            }

        val model = filted.map {

            val kt = it.kotlin
            kt
        }

        return model

    }


    private fun getRegistInfo(controllers: List<KClass<out Any>>): List<RegistInfo> {
        val info = controllers
            .map { klazz ->
                val routeInfo = klazz.functions
                    .mapNotNull m@{ func ->

                        val ri = func
                            .annotations
                            .mapNotNull {

                                val routeInfo = ROUTE_ANNOTATION_MAP[it.annotationClass]?.invoke(it)
                                    ?.let { (path, methods) ->
                                        RouteInfo(path, *methods)
                                    }

                                routeInfo
                            }


                        if (ri.isEmpty()) {
                            return@m null
                        }

                        val order = func
                            .annotations
                            .firstOrNull { it is Order }
                            ?.safeCastTo<Order>()
                            ?.order ?: 0

                        val block = func
                            .annotations
                            .firstOrNull { it is Blocking }
                            ?.let { true } ?: false

                        val regex = func
                            .annotations
                            .firstOrNull {
                                it is Regex
                            }
                            ?.let { true } ?: false

                        val consumes = func
                            .annotations
                            .firstOrNull {
                                it is Consumes
                            }
                            ?.safeCastTo<Consumes>()
                            ?.contentType?.toList() ?: listOf<String>()

                        val produces = func
                            .annotations
                            .firstOrNull {
                                it is Produces
                            }
                            ?.safeCastTo<Produces>()
                            ?.contentType?.toList() ?: listOf<String>()

                        return@m FunctionInfo(func, ri, order, block, regex, consumes, produces)

                    }

                RegistInfo(klazz, routeInfo)
            }
        return info
    }


    private fun registControllers(
        infos: List<RegistInfo>,
        router: Router,
        vertxProducer: VertxProducer
    ) {

        val vertx = vertxProducer.vertx()
        infos.forEach { (controllerKlz, routeTriple) ->
            val controller = classUtil.getInstance(controllerKlz)

            routeTriple.forEach { (func, routeInfos, order, block, regex, consumers, produces) ->

                routeInfos.forEach { routeInfo ->
                    val route = if (regex) {
                        router.routeWithRegex(routeInfo.path)
                    } else {
                        router.route(routeInfo.path)
                    }

                    routeInfo.method.forEach { httpMethod ->
                        route.method(httpMethod)
                    }


                    consumers.forEach { contentType ->
                        route.consumes(contentType)
                    }

                    produces.forEach { contentType ->
                        route.produces(contentType)
                    }

                    route.order(order)

                    val handler: Handler<RoutingContext> = Handler { rc: RoutingContext ->
                        CoroutineScope(vertx.dispatcher()).launch {
                            try {

                                logger.debug("Req => path param : [${rc.pathParams()}] query param : [${rc.queryParams()}] body : [${rc.bodyAsString}]")

                                val body = try {
                                    rc.bodyAsJson
                                } catch (e: Throwable) {
                                    logger.debug("", e)
                                    logger.debug("body to json error , return null")
                                    null
                                }

                                val bodyString = try {
                                    rc.bodyAsString
                                } catch (e: Throwable) {
                                    logger.debug("", e)
                                    logger.debug("body to string error , return null")
                                    null
                                }

                                val bodyRaw = try {
                                    rc.body
                                } catch (e: Throwable) {
                                    logger.debug("", e)
                                    logger.debug("body to string error , return null")
                                    null
                                }


                                val param = rc.pathParams()
                                val query = rc.queryParams().map {
                                    it.key to it.value
                                }.toMap()
                                param.putAll(query)
                                val paramJson = JsonObject(param as Map<String, Any>?).mergeIn(body ?: JsonObject())

                                logger.debug("param map : $paramJson")

                                val cookies = rc.cookieMap()

                                val params = func.parameters
                                    .filter { it.kind == KParameter.Kind.VALUE }
                                    .map {

                                        val klz = invalidParameter(it)
                                            ?: throw ParamRegistException("param don't support : param => ${it.name}  ,  type => ${it.type}  ,  func => ${func.name}  ,  class => ${controllerKlz.simpleName}  ")

                                        klz

                                    }
                                    .map { (ann, kp) ->
                                        when (ann) {
                                            is Context -> {
                                                kp to rc
                                            }
                                            is CookiesMap -> {
                                                kp to cookies
                                            }
                                            is StringParam -> {
                                                kp to paramJson.getString(kp.name)
                                            }
                                            is NumberParam -> {
                                                kp to when (kp.type) {
                                                    Number::class.starProjectedType -> paramJson.getNumber(kp.name)
                                                    Int::class.starProjectedType -> paramJson.getInteger(kp.name)
                                                    Long::class.starProjectedType -> paramJson.getLong(kp.name)
                                                    Double::class.starProjectedType -> paramJson.getDouble(kp.name)
                                                    Float::class.starProjectedType -> paramJson.getFloat(kp.name)
                                                    else -> throw ParamNotSupportException("number ${kp.type} is not supported")
                                                }
                                            }
                                            is BoolParam -> {
                                                kp to paramJson.getBoolean(kp.name)
                                            }
                                            is JsonObjectParam -> {
                                                kp to paramJson.getJsonObject(kp.name)
                                            }
                                            is JsonArrayParam -> {
                                                kp to paramJson.getJsonArray(kp.name)
                                            }
                                            is ParamsMap -> {
                                                when (kp.type.jvmErasure) {
                                                    Map::class -> kp to paramJson.map
                                                    JsonObject::class -> kp to paramJson
                                                    else -> throw ParamNotSupportException("${ann.annotationClass} => ${kp} is not supported , just Map and JsonObject")
                                                }
                                            }
                                            is BodyString -> {
                                                kp to bodyString
                                            }
                                            is BodyRaw -> {
                                                kp to bodyRaw
                                            }
                                            is FileUpload -> {
                                                kp to rc.fileUploads()
                                            }
                                            is EntityParam -> {
                                                kp to paramJson.mapTo(
                                                    kp.type.jvmErasure.java
                                                )
                                            }
                                            is Id -> {
                                                kp to vertxProducer.nextId()
                                            }
                                            is IdString -> {
                                                kp to vertxProducer.nextIdStr()
                                            }

                                            else -> throw ParamNotSupportException("${ann.annotationClass} => ${kp} is not supported")
                                        }
                                    }
                                    /*.map { (ann, kp) ->
                                        when (ann.annotationClass) {
                                            Context::class -> {
                                                kp to rc
                                            }
                                            CookiesMap::class -> {
                                                kp to cookies
                                            }
                                            StringParam::class -> {
                                                kp to paramJson.getString(kp.name)
                                            }
                                            NumberParam::class -> {
                                                kp to when (kp.type) {
                                                    Number::class.starProjectedType -> paramJson.getNumber(kp.name)
                                                    Int::class.starProjectedType -> paramJson.getInteger(kp.name)
                                                    Long::class.starProjectedType -> paramJson.getLong(kp.name)
                                                    Double::class.starProjectedType -> paramJson.getDouble(kp.name)
                                                    Float::class.starProjectedType -> paramJson.getFloat(kp.name)
                                                    else -> throw ParamNotSupportException("number ${kp.type} is not supported")
                                                }
                                            }
                                            BoolParam::class -> {
                                                kp to paramJson.getBoolean(kp.name)
                                            }
                                            JsonObjectParam::class -> {
                                                kp to paramJson.getJsonObject(kp.name)
                                            }
                                            JsonArrayParam::class -> {
                                                kp to paramJson.getJsonArray(kp.name)
                                            }
                                            ParamsMap::class -> {
                                                when (kp.type.jvmErasure) {
                                                    Map::class -> kp to paramJson.map
                                                    JsonObject::class -> kp to paramJson
                                                    else -> throw ParamNotSupportException("${ann.annotationClass} => ${kp} is not supported , just Map and JsonObject")
                                                }
                                            }
                                            BodyString::class -> {
                                                kp to bodyString
                                            }
                                            BodyRaw::class -> {
                                                kp to bodyRaw
                                            }
                                            FileUpload::class -> {
                                                kp to rc.fileUploads()
                                            }
                                            EntityParam::class -> {
                                                kp to paramJson.mapTo(
                                                    kp.type.jvmErasure.java
                                                )
                                            }
                                            Id::class -> {
                                                kp to vertxProducer.nextId()
                                            }
                                            IdString::class -> {
                                                kp to vertxProducer.nextIdStr()
                                            }

                                            else -> throw ParamNotSupportException("${ann.annotationClass} => ${kp} is not supported")
                                        }
                                    }*/
                                    .toMutableList()
                                    .apply {
                                        func.instanceParameter
                                            ?: throw FunctionNotSupportException("only member function is supported")
                                        add(func.instanceParameter!! to controller)
                                    }
                                    .toMap()

                                logger.debug("params map : $params")

                                val result = func.callSuspendBy(params)
                                    .let {
                                        if (func.returnType.jvmErasure == Future::class) {
                                            (it as Future<*>).await()
                                        } else {
                                            it
                                        }
                                    }
                                logger.debug("route ${rc.request().path()} ${rc.request().method()} ------- ")
                                logger.debug("route ${rc.currentRoute()} ------- ")


                                when {
                                    func.returnType.jvmErasure == Unit::class || func.returnType.jvmErasure == Void::class
                                    -> {


                                        if (rc.response().ended()) {
                                            logger.debug("route has no return value and is ended")
                                        } else {
                                            if (func.hasAnnotation<Order>()) {
                                                logger.debug("route has custom order , need to resolve the end by user")
                                            }
                                            logger.debug("route has no return value and need auto ended")
                                            rc.response().end(responseUtil.successResponse(""))
                                        }

                                    }

                                    else -> {
                                        if (func.hasAnnotation<Order>()) {
                                            throw ReturnTypeNotSupportException("custom order function can not have a return value")
                                        }

                                        logger.debug("route auto encode response => $result ")
                                        rc.response().end(responseUtil.successResponse(result))
                                    }
                                }

                            } catch (e: Throwable) {
                                logger.error("route ${rc.request().path()} ------- ")
                                logger.error("route fail => ", e)

                                val response = responseUtil.errorResponse(e.message)
                                rc.response()
                                    .end(response)
                                    .onSuccess {
                                        logger.error("return failed response => $response")
                                        logger.error("route end -------")
                                    }
                                    .onFailure {
                                        logger.error("return failed response failed")
                                        logger.error("route end -------")
                                    }

                            }
                        }
                    }

                    if (block) {
                        route.blockingHandler(handler)
                    } else {
                        route.handler(handler)
                    }


                }


            }
        }
    }

}
