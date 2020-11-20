package io.github.shinglem.web.annotions

import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpMethod
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure



enum class HttpMethod {

    /**
     * The RFC 2616 `OPTIONS` method, this instance is interned and uniquely used.
     */
    OPTIONS,

    /**
     * The RFC 2616 `GET` method, this instance is interned and uniquely used.
     */
    GET,

    /**
     * The RFC 2616 `HEAD` method, this instance is interned and uniquely used.
     */
    HEAD,

    /**
     * The {RFC 2616 @code POST} method, this instance is interned and uniquely used.
     */
    POST,

    /**
     * The RFC 2616 `PUT` method, this instance is interned and uniquely used.
     */
    PUT,

    /**
     * The RFC 2616 `DELETE` method, this instance is interned and uniquely used.
     */
    DELETE,

    /**
     * The RFC 2616 `TRACE` method, this instance is interned and uniquely used.
     */
    TRACE,

    /**
     * The RFC 2616 `CONNECT` method, this instance is interned and uniquely used.
     */
    CONNECT,

    /**
     * The RFC 5789 `PATCH` method, this instance is interned and uniquely used.
     */
    PATCH,

    /**
     * The RFC 2518/4918 `PROPFIND` method, this instance is interned and uniquely used.
     */
    PROPFIND,

    /**
     * The RFC 2518/4918 `PROPPATCH` method, this instance is interned and uniquely used.
     */
    PROPPATCH,

    /**
     * The RFC 2518/4918 `MKCOL` method, this instance is interned and uniquely used.
     */
    MKCOL,

    /**
     * The RFC 2518/4918 `COPY` method, this instance is interned and uniquely used.
     */
    COPY,

    /**
     * The RFC 2518/4918 `MOVE` method, this instance is interned and uniquely used.
     */
    MOVE,

    /**
     * The RFC 2518/4918 `LOCK` method, this instance is interned and uniquely used.
     */
    LOCK,

    /**
     * The RFC 2518/4918 `UNLOCK` method, this instance is interned and uniquely used.
     */
    UNLOCK,

    /**
     * The RFC 4791 `MKCALENDAR` method, this instance is interned and uniquely used.
     */
    MKCALENDAR,

    /**
     * The RFC 3253 `VERSION_CONTROL` method, this instance is interned and uniquely used.
     */
    VERSION_CONTROL,

    /**
     * The RFC 3253 `REPORT` method, this instance is interned and uniquely used.
     */
    REPORT,

    /**
     * The RFC 3253 `CHECKOUT` method, this instance is interned and uniquely used.
     */
    CHECKOUT,

    /**
     * The RFC 3253 `CHECKIN` method, this instance is interned and uniquely used.
     */
    CHECKIN,

    /**
     * The RFC 3253 `UNCHECKOUT` method, this instance is interned and uniquely used.
     */
    UNCHECKOUT,

    /**
     * The RFC 3253 `MKWORKSPACE` method, this instance is interned and uniquely used.
     */
    MKWORKSPACE,

    /**
     * The RFC 3253 `UPDATE` method, this instance is interned and uniquely used.
     */
    UPDATE,

    /**
     * The RFC 3253 `LABEL` method, this instance is interned and uniquely used.
     */
    LABEL,

    /**
     * The RFC 3253 `MERGE` method, this instance is interned and uniquely used.
     */
    MERGE,

    /**
     * The RFC 3253 `BASELINE_CONTROL` method, this instance is interned and uniquely used.
     */
    BASELINE_CONTROL,

    /**
     * The RFC 3253 `MKACTIVITY` method, this instance is interned and uniquely used.
     */
    MKACTIVITY,

    /**
     * The RFC 3648 `ORDERPATCH` method, this instance is interned and uniquely used.
     */
    ORDERPATCH,

    /**
     * The RFC 3744 `ACL` method, this instance is interned and uniquely used.
     */
    ACL,

    /**
     * The RFC 5323 `SEARCH` method, this instance is interned and uniquely used.
     */
    SEARCH,
}


internal val ROUTE_ANNOTATION_MAP : Map<KClass<out Annotation> , (Annotation) -> Pair<String , Array<HttpMethod>>> = mapOf(
    ROUTE::class to { route : Annotation  ->
        route as ROUTE
        val list = route.method.map { HttpMethod(it.name) }.toTypedArray()
        Pair(route.path , list)
    } ,
    GET::class to { route : Annotation  ->
        route as GET
        Pair(route.path , arrayOf(HttpMethod.GET))
    } ,
    POST::class to { route : Annotation  ->
        route as POST
        Pair(route.path , arrayOf(HttpMethod.POST))
    } ,
    OPTIONS::class to { route : Annotation  ->
        route as OPTIONS
        Pair(route.path , arrayOf(HttpMethod.OPTIONS))
    } ,
    HEAD::class to { route : Annotation  ->
        route as HEAD
        Pair(route.path , arrayOf(HttpMethod.HEAD))
    } ,
    PUT::class to { route : Annotation  ->
        route as PUT
        Pair(route.path , arrayOf(HttpMethod.PUT))
    } ,
    DELETE::class to { route : Annotation  ->
        route as DELETE
        Pair(route.path , arrayOf(HttpMethod.DELETE))
    } ,
    TRACE::class to { route : Annotation  ->
        route as TRACE
        Pair(route.path , arrayOf(HttpMethod.TRACE))
    } ,
    PATCH::class to { route : Annotation  ->
        route as PATCH
        Pair(route.path , arrayOf(HttpMethod.PATCH))
    } ,

    )


val supportedParamsList = mapOf<KClass<out Annotation> , Set<KClass<out Any>>>(
    Context::class to setOf(RoutingContext::class),
    CookiesMap::class to setOf(Map::class),
    StringParam::class to setOf(String::class),
    NumberParam::class to setOf(Number::class , Long::class, Int::class, Double::class, Float::class),
    BoolParam::class to setOf(Boolean::class),
    JsonObjectParam::class to setOf(JsonObject::class),
    JsonArrayParam::class to setOf(JsonArray::class),
    ParamsMap::class to setOf(Map::class , JsonObject::class),
    BodyString::class to setOf(String::class),
    BodyRaw::class to setOf(Buffer::class),
    FileUpload::class to setOf(Set::class),
    EntityParam::class to setOf(Any::class),
    Id::class to setOf(Long::class),
    IdString::class to setOf(String::class),
)


fun invalidParameter(param: KParameter): Pair<Annotation, KParameter>? {
    val klz = param.type.jvmErasure
    val ann = param.annotations

    val bool = ann.firstOrNull {
        supportedParamsList[it.annotationClass]?.any {
            it.isSuperclassOf(klz)
        } ?:false
    }
    if (bool != null)
        return bool to param
    else
        return null


}



@Target(AnnotationTarget.CLASS)
annotation class Controller

@Target(AnnotationTarget.CLASS)
annotation class WebSocketController

@Target(AnnotationTarget.FUNCTION)
annotation class WebSocketPath(val path: String)



@Target(AnnotationTarget.FUNCTION)
annotation class Regex

@Target(AnnotationTarget.FUNCTION)
annotation class ROUTE(val path: String, vararg val method: io.github.shinglem.web.annotions.HttpMethod)


@Target(AnnotationTarget.FUNCTION)
annotation class GET(val path: String)



@Target(AnnotationTarget.FUNCTION)
annotation class POST(val path: String)



@Target(AnnotationTarget.FUNCTION)
annotation class OPTIONS(val path: String)


@Target(AnnotationTarget.FUNCTION)
annotation class HEAD(val path: String)


@Target(AnnotationTarget.FUNCTION)
annotation class PUT(val path: String)


@Target(AnnotationTarget.FUNCTION)
annotation class DELETE(val path: String)


@Target(AnnotationTarget.FUNCTION)
annotation class TRACE(val path: String)


@Target(AnnotationTarget.FUNCTION)
annotation class PATCH(val path: String)




@Target(AnnotationTarget.FUNCTION)
annotation class Consumes(vararg val contentType: String)

@Target(AnnotationTarget.FUNCTION)
annotation class Produces(vararg val contentType: String)




@Target(AnnotationTarget.FUNCTION)
annotation class Order(val order: Int)



@Target(AnnotationTarget.FUNCTION)
annotation class Blocking(val order: Int)







@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Context

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class CookiesMap

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class StringParam

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class NumberParam

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class BoolParam

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class JsonObjectParam

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class JsonArrayParam

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class ParamsMap

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class BodyString

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class BodyRaw

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class FileUpload

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class EntityParam

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Id

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class IdString