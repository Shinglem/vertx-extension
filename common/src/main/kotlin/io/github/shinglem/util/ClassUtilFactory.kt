package io.github.shinglem.util

import io.github.shinglem.log.LoggerFactory
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

open class ClassUtilFactory {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getClassUtil(): ClassUtil {
        val service = ServiceLoader.load(ClassUtil::class.java)
        logger.debug("find ClassUtilFactory ......")
        service.forEach {
            logger.debug("${it.javaClass}")
        }

        return service.first()

    }
}

inline fun <reified T : Any> Any?.safeCastTo(): T? {

    val klz = T::class

    return klz.safeCast(this)
}

interface ClassUtil {
    fun <T: Any> getInstance(name: String) : T

    fun <T : Any> getInstance(klz: KClass<T>) : T

    fun getClasses(pack: String): Set<Class<*>>

}