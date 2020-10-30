package io.github.shinglem.vertx.json

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.core.json.DecodeException
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.core.json.jackson.DatabindCodec
import java.io.IOException


fun registerJsonMapper() {

    val module = SimpleModule()

    module.addDeserializer(JsonArray::class.java, JsonArrayDeserializer())
    module.addDeserializer(JsonObject::class.java, JsonObjectDeserializer())

    DatabindCodec.mapper().registerModule(module)
    DatabindCodec.prettyMapper().registerModule(module)

    DatabindCodec.mapper().registerModule(KotlinModule())
    DatabindCodec.prettyMapper().registerModule(KotlinModule())

    DatabindCodec.mapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
    DatabindCodec.prettyMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
    DatabindCodec.mapper().registerModule(Jdk8Module()).registerModule(JavaTimeModule())
    DatabindCodec.prettyMapper().registerModule(Jdk8Module()).registerModule(JavaTimeModule())

}

internal class JsonObjectDeserializer : JsonDeserializer<JsonObject>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): JsonObject {
        val node = p.readValueAsTree<TreeNode>()
        val text = node.toString()
        try {
            return JsonObject(text)
        } catch (e: Exception) {
            if (e is NullPointerException || e is DecodeException) {
                throw e
            }
            throw InvalidFormatException(
                p, "node to json object failed", text,
                JsonObject::class.java
            )
        }
    }
}
internal class JsonArrayDeserializer : JsonDeserializer<JsonArray>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): JsonArray {

        val node = p.readValueAsTree<TreeNode>()
        if (!node.isArray) {
            throw InvalidFormatException(
                p, "node is not array", node,
                JsonArray::class.java
            )
        }
        val text = (node as ArrayNode).toString()
        try {
            return JsonArray(text)
        } catch (e: Exception) {
            if (e is NullPointerException || e is DecodeException) {
                throw e
            }
            throw InvalidFormatException(
                p, "node to array failed", text,
                JsonArray::class.java
            )
        }
    }
}

