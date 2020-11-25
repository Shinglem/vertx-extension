package io.github.shinglem.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.github.shinglem.log.LoggerFactory
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import kotlinx.coroutines.runBlocking
import java.util.*


private lateinit var config : ConfigInterface
open class ConfigFactory : BaseFactory<ConfigInterface>{
    private val logger = LoggerFactory.getLogger(this::class.java)


    override fun getInstance(): ConfigInterface {

        if (::config.isInitialized) {
            return config
        }

        val service = ServiceLoader.load(ConfigInterface::class.java)
        logger.debug("find ConfigInterface ......")
        service.forEach {
            logger.debug("${it.javaClass}")
        }

        if (service.count() == 0) {
            logger.debug("none ConfigInterface load , use DefaultVertxConfig ......")
            config = DefaultVertxConfig()
        }else{
            config = service.first()
            logger.debug("use ${config::class.simpleName}......")
        }

        return config

    }

    override fun setInstance(ins: ConfigInterface) {
        config = ins
    }
}

class DefaultVertxConfig : ConfigInterface{
    private val USER_DIR = System.getProperty("user.dir")
    private val PROFILE = System.getProperty("spring.profiles.active") ?: (System.getProperty("profiles.active") ?: "")
    private val MAIN_FILE_NAME = (System.getProperty("config.name") ?: "")


    private final val logger = LoggerFactory.getLogger(this::class.java.name)

    private var vertxConfig: JsonObject? = null


    private val mapper = ObjectMapper(YAMLFactory())

    init {
        loadConfig()
    }


    override fun config(): JsonObject {
        return vertxConfig!!.copy()
    }

    override fun mergeConfig(config: JsonObject) {
        vertxConfig!!.mergeIn(config, true)
    }
    @Suppress("UNCHECKED_CAST")
    private fun loadConfig() {
        runBlocking {
            logger.debug("----- load config -----")
            val tempVertx = Vertx.vertx()

            val fileName =
                "${if (MAIN_FILE_NAME.isNullOrEmpty()) "application" else MAIN_FILE_NAME}${if (PROFILE.isNullOrEmpty()) "" else "-$PROFILE"}.yml"
            logger.debug("----- load inner config $fileName -----")
            val configInner = try {
                val file = tempVertx.fileSystem().readFile(fileName).await()
                val map = mapper.readValue(file.toString(), Map::class.java) as Map<String, *>
                val json = JsonObject(map)

                json
            } catch (e: Throwable) {
                logger.warn("error in find inner config")
                logger.debug("", e)
                JsonObject()
            }
            logger.debug("----- load outer config $USER_DIR/$fileName -----")

            val configOuter = try {
                val file = tempVertx.fileSystem().readFile("$USER_DIR/$fileName").await()
                val map = mapper.readValue(file.toString(), Map::class.java) as Map<String, *>
                val json = JsonObject(map)

                json
            } catch (e: Throwable) {
                logger.warn("error in find outer config")
                logger.debug("", e)
                JsonObject()
            }

            vertxConfig = configInner.mergeIn(configOuter)
            logger.debug("load config : ${vertxConfig!!.encodePrettily()}")
        }
    }


}

interface ConfigInterface {

    fun config(): JsonObject

    fun mergeConfig(config: JsonObject)

}