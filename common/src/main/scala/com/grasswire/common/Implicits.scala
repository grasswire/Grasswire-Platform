package com.grasswire.common

import java.util.concurrent.ExecutorService

import scala.concurrent.{ Promise, ExecutionContext, Future }
import scala.language.postfixOps
import scala.util.{ Try, Failure, Success }
import scalaz.\/
import scalaz.concurrent.{ Strategy, Task }

object Implicits {

  implicit class TaskPimps(t: Task.type) {
    def fromFuture[A](f: => scala.concurrent.Future[A])(implicit pool: ExecutionContext): Task[A] = {
      Task async { cb =>
        f onComplete {
          case Success(v) => cb(\/.right(v))
          case Failure(e) => cb(\/.left(e))
        }
      }
    }

    def fromScala[A](future: scala.concurrent.Future[A])(implicit ec: ExecutionContext): Task[A] =
      Task async (handlerConversion andThen future.onComplete)

    def fromScalaDeferred[A](future: => scala.concurrent.Future[A])(implicit ec: ExecutionContext): Task[A] =
      Task delay fromScala(future)(ec) flatMap identity

    private def handlerConversion[A]: ((Throwable \/ A) => Unit) => Try[A] => Unit =
      callback => { t: Try[A] => \/ fromTryCatch t.get } andThen callback

    def unsafeToScala[A](task: Task[A]): scala.concurrent.Future[A] = {
      val p = Promise[A]()
      task runAsync {
        _ fold (p failure, p success)
      }
      p.future
    }

  }

}
