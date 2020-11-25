package io.github.shinglem.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker

open class LoggerFactory {
    companion object{
        @JvmStatic
        fun getLogger(name: String?): Logger {
            val logger = LoggerExt(LoggerFactory.getLogger(name))
            return logger
        }
        @JvmStatic
        fun getLogger(clazz: Class<*>): Logger {
            val logger = LoggerExt(LoggerFactory.getLogger(clazz))
            return logger
        }
    }

}

open class LoggerExt(val logger: Logger) : Logger by logger {

    override fun trace(var1: String?) {
        if (logger.isTraceEnabled){
            logger.trace(var1)
        }
    }

    override fun trace(var1: String?, var2: Any?){
        if (logger.isTraceEnabled){
            logger.trace(var1)
        }
    }

    override fun trace(var1: String?, var2: Any?, var3: Any?){
        if (logger.isTraceEnabled){
            logger.trace(var1)
        }
    }

    override fun trace(var1: String?, vararg var2: Any?){
        if (logger.isTraceEnabled){
            logger.trace(var1)
        }
    }

    override fun trace(var1: String?, var2: Throwable?){
        if (logger.isTraceEnabled){
            logger.trace(var1, var2)
        }
    }

    override fun trace(var1: Marker?, var2: String?){
        if (logger.isTraceEnabled){
            logger.trace(var1, var2)
        }
    }

    override fun trace(var1: Marker?, var2: String?, var3: Any?){
        if (logger.isTraceEnabled){
            logger.trace(var1, var2, var3)
        }
    }

    override fun trace(var1: Marker?, var2: String?, var3: Any?, var4: Any?){
        if (logger.isTraceEnabled){
            logger.trace(var1, var2, var3, var4)
        }
    }

    override fun trace(var1: Marker?, var2: String?, vararg var3: Any?){
        if (logger.isTraceEnabled){
            logger.trace(var1, var2, var3)
        }
    }

    override fun trace(var1: Marker?, var2: String?, var3: Throwable?){
        if (logger.isTraceEnabled){
            logger.trace(var1, var2, var3)
        }
    }

    override fun debug(var1: String?){
        if (logger.isDebugEnabled){
            logger.trace(var1)
        }
    }

    override fun debug(var1: String?, var2: Any?){
        if (logger.isDebugEnabled){
            logger.trace(var1, var2)
        }
    }

    override fun debug(var1: String?, var2: Any?, var3: Any?){
        if (logger.isDebugEnabled){
            logger.trace(var1, var2, var3)
        }
    }

    override fun debug(var1: String?, vararg var2: Any?){
        if (logger.isDebugEnabled){
            logger.trace(var1, var2)
        }
    }

    override fun debug(var1: String?, var2: Throwable?){
        if (logger.isDebugEnabled){
            logger.trace(var1, var2)
        }
    }

    override fun debug(var1: Marker?, var2: String?){
        if (logger.isDebugEnabled){
            logger.trace(var1, var2)
        }
    }

    override fun debug(var1: Marker?, var2: String?, var3: Any?){
        if (logger.isDebugEnabled){
            logger.trace(var1, var2, var3)
        }
    }

    override fun debug(var1: Marker?, var2: String?, var3: Any?, var4: Any?){
        if (logger.isDebugEnabled){
            logger.trace(var1, var2, var3, var4)
        }
    }

    override fun debug(var1: Marker?, var2: String?, vararg var3: Any?){
        if (logger.isDebugEnabled){
            logger.trace(var1, var2, var3)
        }
    }

    override fun debug(var1: Marker?, var2: String?, var3: Throwable?){
        if (logger.isDebugEnabled){
            logger.trace(var1, var2, var3)
        }
    }

}