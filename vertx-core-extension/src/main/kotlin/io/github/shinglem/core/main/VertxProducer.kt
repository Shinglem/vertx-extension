package io.github.shinglem.core.main

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import io.github.shinglem.log.LoggerFactory
import io.github.shinglem.util.ClassUtilFactory
import io.github.shinglem.util.id.IdFactory
import io.github.shinglem.vertx.json.registerJsonMapper
import io.vertx.core.DeploymentOptions
import io.vertx.core.Verticle
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetAddress
import java.util.concurrent.CountDownLatch
import kotlin.reflect.KClass

object VertxConfig {

    private val USER_DIR = System.getProperty("user.dir")
    private val PROFILE = System.getProperty("spring.profiles.active") ?: (System.getProperty("profiles.active") ?: "")
    private val MAIN_FILE_NAME = (System.getProperty("config.name") ?: "")


    private final val logger = LoggerFactory.getLogger(this::class.java.name)

    private var vertxConfig: JsonObject? = null


    private val mapper = ObjectMapper(YAMLFactory())

    init {
        loadConfig()
    }


    fun config(): JsonObject {
        return vertxConfig!!.copy()
    }

    fun mergeConfig(config: JsonObject) {
        vertxConfig!!.mergeIn(config, true)
    }

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

interface VertxProducer {
    fun vertx(): Vertx
    fun baseOption(): DeploymentOptions

}

object VertxSingleProducer : VertxProducer {
    private final val logger = LoggerFactory.getLogger(this::class.java.name)

    private var vertx: Vertx? = null
    private var baseOption: DeploymentOptions? = null

    private fun localIp(): String {
        val ip = InetAddress.getLocalHost().hostAddress
        return ip
    }

    private val vertxConfig = VertxConfig.config()["vertx"] ?: JsonObject()

    private val baseDeploymentOptions = vertxConfig["baseDeploymentOptions"] ?: JsonObject()
    private val vertxOptions = vertxConfig["vertxOptions"] ?: JsonObject()


    init {
        vertxInit()
    }

    private fun vertxInit() {

        logger.debug("----- Start cluster VERTX-----")
        logger.debug("-----Setproperty: Log4j2LogDelegateFactory-----")

        logger.debug("----- register json mapper -----")
        registerJsonMapper()

        logger.debug("----- get local ip -----")
        val ip = localIp()
        logger.debug("----- get ip : $ip -----")


        vertx = Vertx.vertx(VertxOptions(vertxOptions))
        baseOption = DeploymentOptions(baseDeploymentOptions)





        Runtime.getRuntime().addShutdownHook(Thread {
            logger.info("start stop vertx");

            val countDownLatch = CountDownLatch(1)
            vertx!!.close {
                countDownLatch.countDown()
            }
            try {
                countDownLatch.await()
                logger.info("stop vertx success");
            } catch (e: Exception) {
                logger.error("", e)
            }
        })


    }


    override fun vertx(): Vertx {
        return vertx!!
    }


    override fun baseOption(): DeploymentOptions {
        return baseOption!!
    }


}

object VERTX : VertxProducer by VertxSingleProducer {

    private val classUtil = ClassUtilFactory().getClassUtil()

    private val idFactoryName = VertxConfig.config()
        .get<JsonObject?>("vertx")
        ?.getString("idFactory")
        ?: "default"

    private val idFactory = idFactoryName.let {
        if ("default" == it) {
            return@let IdFactory()
        }

        return@let classUtil.getInstance(it)
    }

    private val idGeneratorName = VertxConfig.config()
        .get<JsonObject?>("vertx")
        ?.getString("idGenerator")
        ?: "SnowFlake"

    private val idGenerator = idFactory.getIdGenerator(idGeneratorName)

    fun nextId() = idGenerator.nextId()

    fun nextIdStr() = idGenerator.next()
}


object VertxMain {

    private final val logger = org.slf4j.LoggerFactory.getLogger(this::class.java.name)
    private val vertx = VERTX.vertx()
    private val baseOption = VERTX.baseOption()

    private var verticleClass: Class<*>? = null

    init {

        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
        System.setProperty("user.timezone", "GMT +08")
        System.setProperty("kotlinx.coroutines.debug", "")
        System.setProperty("java.net.preferIPv4Stack", "true")

        VertxConfig
    }

    private fun start0(): Job {
        return GlobalScope.launch(vertx.dispatcher()) {
            logger.debug("-----deploy verticles-----")


            val options = DeploymentOptions(baseOption).setInstances(1)

            val deploymentOptions =
                VertxConfig.config()
                    .get<JsonObject?>("vertx")
                    ?.get<JsonObject?>("deploymentOption")
                    ?.get<JsonObject?>(verticleClass!!.name)
                    ?.let { DeploymentOptions(options.toJson().mergeIn(it, true)) }
                    ?: options


            val serviceVerticleId = vertx.deployVerticle(verticleClass!!.name, deploymentOptions).await()


            logger.info("Main verticle Start: id = [$serviceVerticleId ]")
        }
    }

    fun <T : Verticle> start(verticleClass: Class<T>): Job {
        VertxMain.verticleClass = verticleClass
        return start0()
    }

    fun <T : Verticle> start(verticleClass: KClass<T>): Job {
        VertxMain.verticleClass = verticleClass.java
        return start0()
    }

    inline fun <reified T : Verticle> start(): Job {
        return start(T::class.java)
    }

}
