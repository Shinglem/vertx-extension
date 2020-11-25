package io.github.shinglem.util

import io.github.shinglem.log.LoggerFactory
import io.github.shinglem.util.id.IdGenerator
import io.github.shinglem.util.id.impl.SnowFlake
import java.util.*

private lateinit var idGenerator: IdGenerator
open class IdGeneratorFactory : BaseFactory<IdGenerator> {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun setInstance(ins: IdGenerator) {
        idGenerator = ins
    }

    override fun getInstance(): IdGenerator {
        if (::idGenerator.isInitialized) {
            return idGenerator
        }

        val service = ServiceLoader.load(IdGenerator::class.java)
        logger.debug("find IdGenerator ......")
        service.forEach {
            logger.debug("${it.javaClass}")
        }

        if (service.count() == 0) {
            logger.debug("none IdGenerator load , use SnowFlake ......")
            idGenerator = SnowFlake()
        }else{
            idGenerator = service.first()
            logger.debug("use ${idGenerator::class.simpleName}......")
        }

        return idGenerator
    }
}