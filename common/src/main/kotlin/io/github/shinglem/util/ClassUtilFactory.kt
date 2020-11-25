package io.github.shinglem.util

import io.github.shinglem.log.LoggerFactory
import io.github.shinglem.reflect.util.DefaultClassUtil
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

private lateinit var classUtil : ClassUtil
open class ClassUtilFactory :BaseFactory<ClassUtil>{
    private val logger = LoggerFactory.getLogger(this::class.java)


    override fun getInstance(): ClassUtil {

        if (::classUtil.isInitialized) {
            return classUtil
        }

        val service = ServiceLoader.load(ClassUtil::class.java)
        logger.debug("find ClassUtilFactory ......")
        service.forEach {
            logger.debug("${it.javaClass}")
        }

        if (service.count() == 0) {
            logger.debug("none ClassUtilFactory , use DefaultClassUtil ......")
            classUtil = DefaultClassUtil()
        }else{
            classUtil = service.first()
            logger.debug("use ${classUtil::class.simpleName}......")
        }

        return classUtil

    }

    override fun setInstance(ins: ClassUtil) {
        classUtil = ins
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