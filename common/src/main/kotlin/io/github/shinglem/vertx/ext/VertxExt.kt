package io.github.shinglem.vertx.ext

import io.github.shinglem.log.LoggerFactory
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.*


suspend fun <T> Vertx.parallelBlocking(block: () -> T): T {
    return this.executeBlocking<T>({ fut ->
            fut.complete(block())
        }, false).await()

}

suspend fun <T> Vertx.blocking(block: () -> T): T {
    return try {
        this.executeBlocking<T>({ fut ->
            fut.complete(block())
        }).await()
    } catch (e: Throwable) {
        throw e
    }

}
//fun <T> Vertx.blockingAsync(block: () -> T ,success: (T) -> Unit = {} , error: (Throwable) ->Unit = {}) {
//    this.executeBlocking<T>({ fut ->
//            fut.complete(block())
//        }, { ar ->
//            if(ar.succeeded()){
//                success(ar.result())
//            }else{
//                error(ar.cause())
//            }
//        })
//
//}
//
//fun <T> Vertx.blockingAsync(block: () -> T ) {
//    this.blockingAsync(block , {} , {})
//
//}

suspend fun <T> Vertx.asyncJob(block: suspend () -> T): Deferred<T> {

    return CoroutineScope(
        this.dispatcher()
    ).async {

        block()

    }


}

suspend fun <T> Vertx.asyncBlockingJob(block: suspend () -> T): Deferred<T> {

    return CoroutineScope(
        this.dispatcher()
    ).async {

        this@asyncBlockingJob.blocking { runBlocking { block() } }

    }


}

suspend fun <T> Vertx.asyncParallelBlockingJob(block: suspend () -> T): Deferred<T> {

    return CoroutineScope(
        this.dispatcher()
    ).async {

        this@asyncParallelBlockingJob.parallelBlocking { runBlocking { block() } }

    }


}


suspend fun <T> Vertx.async(block: suspend () -> T): T {
    return asyncJob(block).await()
}

suspend fun <T> Vertx.asyncBlocking(block: suspend () -> T): T {
    return asyncBlockingJob(block).await()
}

suspend fun <T> Vertx.asyncParallelBlocking(block: suspend () -> T): T {
    return asyncParallelBlockingJob(block).await()
}
private val vertxExtLogger = LoggerFactory.getLogger("vertx-future-launch")

fun <T>  Vertx.futureLaunch(handler : Handler<AsyncResult<T>> , block: suspend () -> T) {
    CoroutineScope(this.dispatcher()).launch {
        try {
            val result = block()
            handler.handle(Future.succeededFuture(result))
        }catch (e: Exception) {
            vertxExtLogger.error("" , e)
            handler.handle(Future.failedFuture(e))
        }
    }
}

