package io.github.shinglem.web.response

import io.github.shinglem.web.exceptions.ResponseClassNotSupportException
import io.vertx.core.json.Json
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

data class ResponseEntity(var code : Int? = null,
                                  var message : String? = null,
                                  var data : Any? = null) {


}

interface ResponseUtil {

    fun <T> errorResponse(resp : T?) : String

    fun <T> successResponse(resp : T?) : String
}

const val RESULT_OK  = 0
const val RESULT_NOK = 1

object DefaultResponseUtil : ResponseUtil {
    private fun <T> response(resp: T?): String {
        if (resp == null) {
            throw ResponseClassNotSupportException("null response not support")
        }

        if(resp !is ResponseEntity) {
            throw ResponseClassNotSupportException("${resp!!::class.simpleName} response not support")
        }

        return Json.encodePrettily(resp)

    }

    override fun <T> errorResponse(resp: T?): String {
        if (resp is String){
            return response(ResponseEntity(RESULT_NOK , resp))
        }
        return response(ResponseEntity(RESULT_NOK , "failed" , Json.encodePrettily(resp)))
    }

    override fun <T> successResponse(resp: T?): String {
        if (resp is String){
            return response(ResponseEntity(RESULT_OK , resp))
        }

        if (resp is JsonObject){
            return response(ResponseEntity(RESULT_OK , "success",resp))
        }

        if (resp is JsonArray){
            return response(ResponseEntity(RESULT_OK , "success",resp))
        }

        return response(ResponseEntity(RESULT_OK , "success" , Json.encodePrettily(resp)))
    }

}