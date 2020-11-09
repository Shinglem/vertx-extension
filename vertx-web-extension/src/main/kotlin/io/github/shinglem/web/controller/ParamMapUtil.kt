package io.github.shinglem.web.controller

import io.github.shinglem.web.annotions.BodyString
import io.github.shinglem.web.annotions.ParamType
import io.github.shinglem.web.exceptions.ParamNotSupportException
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.Cookie
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.FileUpload
import io.vertx.ext.web.RoutingContext
import kotlin.reflect.KParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSupertypeOf
import kotlin.reflect.full.starProjectedType

fun mapParam(paramAnn : ParamType , pm : ParamModel , it : RoutingContext , paramJson : JsonObject, bodyString: String? , bodyRaw : Buffer? ,  cookies : Map<String, io.vertx.core.http.Cookie>): Pair<KParameter, Any?> {
   return when {
        paramAnn.paramType == ParamType.PARAM_TYPE.RoutingContext && pm.kParameter.type.isSupertypeOf(
            RoutingContext::class.starProjectedType
        ) -> pm.kParameter to it

        paramAnn.paramType == ParamType.PARAM_TYPE.String && pm.kParameter.type.isSupertypeOf(
            String::class.starProjectedType
        ) -> pm.kParameter to paramJson.getString(pm.name)

        paramAnn.paramType == ParamType.PARAM_TYPE.Number -> pm.kParameter to when (pm.kParameter.type) {
//                                        String::class.starProjectedType -> paramJson.getNumber(pm.name)
            Number::class.starProjectedType -> paramJson.getNumber(pm.name)
            Int::class.starProjectedType -> paramJson.getInteger(pm.name)
            Long::class.starProjectedType -> paramJson.getLong(pm.name)
            Double::class.starProjectedType -> paramJson.getDouble(pm.name)
            Float::class.starProjectedType -> paramJson.getFloat(pm.name)
            else -> throw ParamNotSupportException("number ${pm.kParameter.type} is not supported")
        }

        paramAnn.paramType == ParamType.PARAM_TYPE.Bool && pm.kParameter.type.isSupertypeOf(
            Boolean::class.starProjectedType
        ) -> pm.kParameter to paramJson.getBoolean(pm.name)


        paramAnn.paramType == ParamType.PARAM_TYPE.JsonObject && pm.kParameter.type.isSupertypeOf(
            JsonObject::class.starProjectedType
        ) -> pm.kParameter to paramJson.getJsonObject(pm.name)

        paramAnn.paramType == ParamType.PARAM_TYPE.JsonArray && pm.kParameter.type.isSupertypeOf(
            JsonArray::class.starProjectedType
        ) -> pm.kParameter to paramJson.getJsonArray(pm.name)

        paramAnn.paramType == ParamType.PARAM_TYPE.Entity -> pm.kParameter to paramJson.mapTo(
            pm.klz.java
        )
        paramAnn.paramType == ParamType.PARAM_TYPE.CookiesMap && pm.kParameter.type.isSupertypeOf(
            Map::class.createType(listOf(
                KTypeProjection(KVariance.INVARIANT, String::class.starProjectedType),
                KTypeProjection(KVariance.INVARIANT, Cookie::class.starProjectedType),
            ))
        ) -> pm.kParameter to cookies


        paramAnn.paramType == ParamType.PARAM_TYPE.ParamsMap && (pm.kParameter.type.isSupertypeOf(
            Map::class.createType(listOf(
                KTypeProjection(KVariance.INVARIANT, String::class.starProjectedType),
                KTypeProjection(KVariance.INVARIANT, Any::class.starProjectedType),
            ))
        )) -> pm.kParameter to paramJson.map

        paramAnn.paramType == ParamType.PARAM_TYPE.ParamsMap && (pm.kParameter.type.isSupertypeOf(
            JsonObject::class.starProjectedType

        )) -> pm.kParameter to paramJson


        paramAnn.paramType == ParamType.PARAM_TYPE.BodyString && (pm.kParameter.type.isSupertypeOf(
            String::class.starProjectedType

        )) -> pm.kParameter to bodyString


        paramAnn.paramType == ParamType.PARAM_TYPE.BodyRaw && (pm.kParameter.type.isSupertypeOf(
            Buffer::class.starProjectedType

        )) -> pm.kParameter to bodyRaw


        paramAnn.paramType == ParamType.PARAM_TYPE.FileUpload && (pm.kParameter.type.isSupertypeOf(
            Set::class.createType(listOf(
                KTypeProjection(KVariance.INVARIANT, FileUpload::class.starProjectedType)
            )


            ))) -> pm.kParameter to it.fileUploads()




        paramAnn.paramType == ParamType.PARAM_TYPE.Id && pm.kParameter.type.isSupertypeOf(
            Long::class.starProjectedType
        ) -> pm.kParameter to paramJson.getJsonObject(pm.name)

        paramAnn.paramType == ParamType.PARAM_TYPE.IdString && pm.kParameter.type.isSupertypeOf(
            String()::class.starProjectedType
        ) -> pm.kParameter to paramJson.getJsonObject(pm.name)


        else -> throw ParamNotSupportException("${pm.ann} => ${pm.kParameter.type} is not supported")
    }
}