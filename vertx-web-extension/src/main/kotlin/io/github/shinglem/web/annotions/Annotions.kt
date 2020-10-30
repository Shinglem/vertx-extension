package io.github.shinglem.web.annotions

import kotlin.reflect.KClass

annotation class Controller

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


annotation  class  Route(val path: String, vararg val method: HttpMethod)

@Route("" , HttpMethod.GET)
annotation class GET(val path: String)


@Route("" , HttpMethod.POST)
annotation class POST(val path: String)


@Route("" , HttpMethod.OPTIONS)
annotation class OPTIONS(val path: String)


@Route("" , HttpMethod.HEAD)
annotation class HEAD(val path: String)


@Route("" , HttpMethod.PUT)
annotation class PUT(val path: String)


@Route("" , HttpMethod.DELETE)
annotation class DELETE(val path: String)


@Route("" , HttpMethod.TRACE)
annotation class TRACE(val path: String)





annotation class Order(val order: Int)


internal annotation class ParamType(val paramType :PARAM_TYPE) {
    enum class PARAM_TYPE {
        RoutingContext ,
        String ,
        Number ,
        Bool ,
        JsonObject ,
        JsonArray ,
        Entity ,
        CookiesMap ,
        Id ,
        IdString
    }
}

@ParamType(ParamType.PARAM_TYPE.RoutingContext)
annotation class Context

@ParamType(ParamType.PARAM_TYPE.CookiesMap)
annotation class CookiesMap

@ParamType(ParamType.PARAM_TYPE.String)
annotation class StringParam

@ParamType(ParamType.PARAM_TYPE.Number)
annotation class NumberParam

@ParamType(ParamType.PARAM_TYPE.Bool)
annotation class BoolParam

@ParamType(ParamType.PARAM_TYPE.JsonObject)
annotation class JsonObjectParam

@ParamType(ParamType.PARAM_TYPE.JsonArray)
annotation class JsonArrayParam

@ParamType(ParamType.PARAM_TYPE.Entity)
annotation class EntityParam

@ParamType(ParamType.PARAM_TYPE.Id)
annotation class Id

@ParamType(ParamType.PARAM_TYPE.IdString)
annotation class IdString