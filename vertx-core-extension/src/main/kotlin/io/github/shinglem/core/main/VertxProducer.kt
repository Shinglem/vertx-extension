package io.github.shinglem.core.main

import io.github.shinglem.log.LoggerFactory
import io.github.shinglem.util.*
import io.github.shinglem.util.id.IdGenerator
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
import java.net.InetAddress
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.reflect.KClass


object VertxConfig : ConfigInterface by ConfigFactory().getInstance()


private lateinit var vertxProducer: VertxProducer

open class VertxProducerFactory : BaseFactory<VertxProducer> {
    private val logger = LoggerFactory.getLogger(this::class.java)


    override fun getInstance(): VertxProducer {

        if (::vertxProducer.isInitialized) {
            return vertxProducer
        }

        val service = ServiceLoader.load(VertxProducer::class.java)
        logger.debug("find VertxProducer ......")
        service.forEach {
            logger.debug("${it.javaClass}")
        }

        if (service.count() == 0) {
            logger.debug("none VertxProducer load , use VertxSingleProducer ......")
            vertxProducer = VertxSingleProducer()
        } else {
            vertxProducer = service.first()
            logger.debug("use ${vertxProducer::class.simpleName}......")
        }

        return vertxProducer

    }

    override fun setInstance(ins: VertxProducer) {
        vertxProducer = ins
    }
}

interface VertxProducer {
    fun vertx(): Vertx
    fun baseOption(): DeploymentOptions

    fun nextId(): Long
    fun nextIdStr(): String
}

class VertxSingleProducer : VertxProducer {
    private final val logger = LoggerFactory.getLogger(this::class.java.name)

    private var vertx: Vertx? = null
    private var baseOption: DeploymentOptions? = null

    private fun localIp(): String {
        val ip = InetAddress.getLocalHost().hostAddress
        return ip
    }

    private val vertxConfig = VertxConfig.config()["vertx"] ?: JsonObject()
    private val classUtil = ClassUtilFactory().getInstance()
    private val baseDeploymentOptions = vertxConfig["baseDeploymentOptions"] ?: JsonObject()
    private val vertxOptions = vertxConfig["vertxOptions"] ?: JsonObject()


    init {
        vertxInit()
    }

    private val idGenerator = IdGeneratorFactory()
        .apply {
            vertxConfig
                .getJsonObject("vertx")
                ?.getString("idGenerator")
                ?.run {
                    try {
                        val idGenerator = classUtil.getInstance<IdGenerator>(this)
                        this@apply.setInstance(idGenerator)
                    } catch (e: Throwable) {
                        logger.error("can not get $this ...", e)
                        throw e
                    }
                }

        }.getInstance()

    private fun vertxInit() {

        logger.debug("----- Start VERTX-----")
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


    override fun nextId() = idGenerator.nextId()

    override fun nextIdStr() = idGenerator.next()


}

object VERTX : VertxProducer by VertxProducerFactory().getInstance()

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
