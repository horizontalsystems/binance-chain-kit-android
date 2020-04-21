package io.horizontalsystems.binancechainkit.core

import io.reactivex.Flowable
import io.reactivex.Single
import retrofit2.HttpException
import java.util.concurrent.TimeUnit

fun <T> Single<T>.retryWithDelay(delaySeconds: Int): Single<T> {
    var retryCount = 0
    val maxRetries = 1

    return retryWhen { single ->
        single.flatMap { throwable ->
            // Error code 429 (API Rates limit exceeded) HTTP Code: Too Many Request
            if ((throwable as? HttpException)?.code() == 429 && ++retryCount <= maxRetries) {
                Flowable.timer(delaySeconds.toLong(), TimeUnit.SECONDS)
            } else {
                Flowable.error(throwable)
            }
        }
    }
}