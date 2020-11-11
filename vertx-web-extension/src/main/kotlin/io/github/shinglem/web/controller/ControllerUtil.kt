package io.github.shinglem.web.controller

import io.github.shinglem.util.ClassUtilFactory
import io.github.shinglem.util.safeCastTo
import io.github.shinglem.web.annotions.Controller
import io.github.shinglem.web.annotions.ParamType
import io.github.shinglem.web.annotions.Route
import io.github.shinglem.web.exceptions.FunctionNotSupportException
import io.github.shinglem.web.exceptions.ParamRegistException
import io.github.shinglem.web.exceptions.RouteRegistException
import io.github.shinglem.web.response.ResponseUtil
import io.vertx.core.Future
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.*
import kotlin.reflect.jvm.jvmErasure

data class ControllerModel(
    val klazz: KClass<*>,
    val methods: List<KFunction<*>>
)

data class RouteInfo(
    val path: String,
    val order: Int,
    val method: List<io.vertx.core.http.HttpMethod>,
    val func: KFunction<*>

)

data class ParamModel(
    val name: String,
    val klz: KClass<*>,
    val ann: Annotation,
    val kParameter: KParameter
)

data class ControllerRegistInfo(
    val controller: KClass<*>,
    val route: List<Triple<RouteInfo, List<ParamModel>, KClass<*>>>
)

fun getMethod(method: io.github.shinglem.web.annotions.HttpMethod): io.vertx.core.http.HttpMethod {
    return HttpMethod(method.name)
}

interface ControllerUtil {
    fun regist(pack: String = "", router: Router, coroutineScope: CoroutineScope, respUtil: ResponseUtil)
}

object DefaultControllerUtil :ControllerUtil {
    private val logger = LoggerFactory.getLogger(this::class.java)

    private val classUtil = ClassUtilFactory().getClassUtil()

    private lateinit var  responseUtil :ResponseUtil

    override fun regist(pack: String, router: Router, coroutineScope: CoroutineScope, respUtil: ResponseUtil) {
        responseUtil = respUtil
        registControllers(getRegistInfo(pack), router, coroutineScope)
    }

    private fun getControllers(pack: String = ""): List<ControllerModel> {
        val classes = classUtil.getClasses(pack)
        val filted = classes
            .filter {

                val list = it.annotations.toList()
                val b = list.map(Annotation::annotationClass).contains(Controller::class)
                b
            }

        val model = filted.map {
            val kt = it.kotlin

            val funcs =
//                classUtil.getKFuncsWithAnnotion(kt , Route::class)

                kt.functions.filter {
                it.annotations.map(Annotation::annotationClass)
                    .any {
                        val a = it
                            .annotations
                            .map(Annotation::annotationClass)
                            .contains(Route::class)
                        val b = it.isSubclassOf(Route::class)
                        a || b
                    }
            }

            ControllerModel(kt, funcs)

        }

        return model
    }

    private fun getRegistInfo(pack: String = ""): List<ControllerRegistInfo> {
        val info = getControllers(pack)
            .map {
                val (klazz, funcs) = it

                val routeInfo = funcs
//                    .filter {
//                        it.annotations
//                            .any { ann ->
//                                ann::class.isSubclassOf(Route::class) || ann.annotationClass.annotations.map(Annotation::annotationClass)
//                                    .contains(Route::class)
//                            }
//                    }
                    .map m@{
                        val annotation =
//                            classUtil.getKFuncsFirstAnnotion(it , Route::class)
                            it.annotations
                            .filter { ann ->
                                ann::class.isSubclassOf(Route::class) || ann.annotationClass.annotations.map(Annotation::annotationClass)
                                    .contains(Route::class)
                            }
                            .first()

                        val route = annotation.let { ann ->

                            var path = ""
                            var method: List<HttpMethod>? = null
                            var order = 0

                            val routeinfo: RouteInfo

//                            val r = (classUtil.safeCastToSuper(ann , Route::class)?.apply {
//                                    path = this.path.apply {
//                                        if (this.isNullOrBlank())
//                                            throw RouteRegistException("route path is null or blank")
//                                    }
//                                    method = this.method
//                                        .toList()
//                                        .map(::getMethod)
//                                }
//                                ?: classUtil.getAnnotionInstance(ann , Route::class))?.apply {
//                                val klz = classUtil.getAnnotionKClass(annotation)
//                                val p = classUtil.getFirstPropertyValue(klz , String::class)
//
////                                    klz.memberProperties.first {
////                                    it.returnType.isSubtypeOf(String::class.starProjectedType)
////                                }.getter.call(ann) as String
//
//                                path = p
//                                method = this.method.toList().map(::getMethod)
//                            }
//                                ?: throw RouteRegistException("route is null")

                            if (ann::class.isSubclassOf(Route::class)) {
                                val r = ann.safeCastTo<Route>() ?: throw RouteRegistException("route is null")
                                path = r.path.apply {
                                    if (this.isNullOrBlank())
                                        throw RouteRegistException("route path is null or blank")
                                }
                                method = r.method
                                    .toList()
                                    .map(::getMethod)


                            }

                            if (ann.annotationClass.annotations.map(Annotation::annotationClass).contains(Route::class)) {

                                val r = ann.annotationClass.annotations
                                    .first {
                                        val a = it.annotationClass.isSubclassOf(Route::class)
                                        a
                                    }
                                    .safeCastTo<Route>() ?: throw RouteRegistException("route is null")

                                val klz = ann.annotationClass
                                val p = klz.memberProperties.first {
                                    it.returnType.isSubtypeOf(String::class.starProjectedType)
                                }.getter.call(ann) as String

                                path = p
                                method = r.method.toList().map(::getMethod)
//                        return@let RouteInfo(path, order, method)
                            }

                            method ?: throw RouteRegistException("method is null")


                            routeinfo = RouteInfo(path, order, method!!, it)

                            return@let routeinfo
                        }

                        val paramModels = kotlin.run {

                            val func = route.func
                            func.parameters
                                .filter { it.kind == KParameter.Kind.VALUE }
                                .map {
                                    val name = it.name ?: throw ParamRegistException("param name is null")
                                    val klz = it.type.jvmErasure
                                    val kParameter = it
                                    val ann = it.annotations.firstOrNull { a ->
                                        a.annotationClass.annotations.any { pa -> pa.annotationClass.isSubclassOf(ParamType::class) }
                                    } ?: throw ParamRegistException("param has no type annotation")

                                    ParamModel(name, klz, ann, kParameter)
                                }

                        }

                        val returnType = it.returnType.jvmErasure

                        Triple(route, paramModels, returnType)
                    }
                ControllerRegistInfo(klazz, routeInfo)
            }
            .also {
                logger.debug("controller info => $it")
            }
        return info
    }


    private fun registControllers(infos: List<ControllerRegistInfo>, router: Router, coroutineScope: CoroutineScope) {

        infos.forEach {
            val (controllerKlz, routeInfo) = it

            val controller = classUtil.getInstance(controllerKlz)
//                controllerKlz.objectInstance ?: controllerKlz.createInstance()

            routeInfo.forEach { (routeInfo, paramModel, returnKlz) ->

                val route = router.route(routeInfo.path)
                routeInfo.method.forEach { httpMethod ->
                    route.method(httpMethod)
                }

                route.order(routeInfo.order)

                val func = routeInfo.func
                route.handler {

                    coroutineScope.launch {
                        try {

                            logger.debug("Req => path param : [${it.pathParams()}] query param : [${it.queryParams()}] body : [${it.bodyAsString}]")

                            val body = try {it.bodyAsJson} catch (e : Throwable){
                                logger.debug("" , e)
                                logger.debug("body to json error , return null")
                                null
                            }

                            val bodyString = try {it.bodyAsString} catch (e : Throwable){
                                logger.debug("" , e)
                                logger.debug("body to string error , return null")
                                null
                            }

                            val bodyRaw = try {it.body} catch (e : Throwable){
                                logger.debug("" , e)
                                logger.debug("body to string error , return null")
                                null
                            }


                            val param = it.pathParams()
                            val query = it.queryParams().map {
                                it.key to it.value
                            }.toMap()
                            param.putAll(query)
                            val paramJson = JsonObject(param as Map<String, Any>?).mergeIn(body ?: JsonObject())

                            logger.debug("param map : $paramJson")

                            val cookies = it.cookieMap()


                            val params: Map<KParameter, Any?> = paramModel
                                .map { pm ->
                                    val paramAnn = pm.ann.annotationClass.annotations.first {
                                        it.annotationClass.isSubclassOf(ParamType::class)
                                    }.safeCastTo<ParamType>()!!


                                    mapParam(
                                        paramAnn ,
                                        pm,
                                        it,
                                        paramJson,
                                        bodyString,
                                        bodyRaw,
                                        cookies
                                    )

                                }
                                .toMutableList<Pair<KParameter, Any?>>()
                                .apply {
                                    func.instanceParameter
                                        ?: throw FunctionNotSupportException("only member function is supported")
                                    add(func.instanceParameter!! to controller)
                                }
                                .toMap()

                            logger.debug("params map : $params")

                            val result = func.callSuspendBy(params)
                                .let {
                                    if(returnKlz.isSubclassOf(Future::class)) {
                                        (it as Future<*>).await()
                                    }else{
                                        it
                                    }
                                }



                            logger.debug("route ${it.request().path()} ${it.request().method()} ------- ")
                            logger.debug("route ${it.currentRoute()} ------- ")
                            when {
                                returnKlz.isSubclassOf(Unit::class) || returnKlz.isSubclassOf(Void::class) -> {
                                    if (it.response().ended()){
                                        logger.debug("route has no return value and is ended")
                                    }else{
                                        logger.debug("route has no return value and need auto ended")
                                        it.response().end(responseUtil.successResponse(""))
                                    }

                                }

                                else -> {

                                    logger.debug("route auto encode response => $result ")
                                    it.response().end(responseUtil.successResponse(result))
                                }
                            }
                        } catch (e: Throwable) {
                            logger.error("route ${it.request().path()} ------- ")
                            logger.error("route fail => ", e)

                            val response = responseUtil.errorResponse(e.message)
                            it.response()
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

            }


        }


    }
}




