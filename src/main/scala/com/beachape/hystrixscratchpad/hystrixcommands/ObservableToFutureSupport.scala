package com.beachape.hystrixscratchpad.hystrixcommands

import rx.lang.scala.Observable
import scala.concurrent.Promise

/**
 * Trait for easy conversion between RX Observables and
 * Scala Futures
 *
 */
trait ObservableToFutureSupport {

  import rx.lang.scala.JavaConversions._

  /**
   * Converts an Observable[A] to Future[A]
   *
   * Shouldn't really be used when the Observable is going to
   * give more than 1 result over the course of time.
   *
   * @param obs Observable[A]; can be Java or Scala variants
   * @tparam A
   * @return Future[A]
   */
  def observableToFuture[A](obs: Observable[A]) = {
    val promise = Promise[A]
    toScalaObservable(obs).subscribe(
      onNext =  ( x => promise.success(x) ),
      onError = ( e => promise.failure(e))
    )
    promise.future
  }

}
