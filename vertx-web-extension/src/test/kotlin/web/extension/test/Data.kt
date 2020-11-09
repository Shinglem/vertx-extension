package web.extension.test

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

data class TestData(
    val string : String,
    val number: Number,
    val boolean: Boolean,
    val jsonObject: JsonObject,
    val jsonArray: JsonArray
)

data class Entity (val json : String)

data class TestData2(
    val string : String ,
    val number: Number ,
    val boolean: Boolean ,
    val entity: Entity
)