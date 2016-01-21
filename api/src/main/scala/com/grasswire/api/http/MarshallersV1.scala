package com.grasswire.api.http

import java.util.UUID

import spray.http._
import spray.httpx.marshalling._
import spray.httpx.unmarshalling.{ Deserialized, Deserializer, MalformedContent }

import scala.language.implicitConversions
import scalaz.concurrent.Task
import scalaz.{ -\/, \/- }

object MarshallersV1 {

  implicit def scalazFutureMarshaller[T](implicit m: Marshaller[T]): Marshaller[scalaz.concurrent.Future[T]] =
    Marshaller[scalaz.concurrent.Future[T]] { (value, ctx) =>
      value.runAsync(t => m(t, ctx))
    }

  implicit def scalazTaskWithStatusMarshaller[A](implicit m: ToResponseMarshaller[(StatusCode, A)]): ToResponseMarshaller[scalaz.concurrent.Task[(StatusCode, A)]] =
    ToResponseMarshaller[scalaz.concurrent.Task[(StatusCode, A)]] { (task, ctx) =>
      task.runAsync(_.fold(l => throw l, r => m(r, ctx)))
    }

  implicit def scalazTaskMarshaller[A](implicit m: Marshaller[A]): Marshaller[scalaz.concurrent.Task[A]] =
    Marshaller[scalaz.concurrent.Task[A]] { (task, ctx) =>
      task.runAsync(_.fold(l => throw l, r => m(r, ctx)))
    }

  implicit def scalazEitherMarshaller[A](implicit m: Marshaller[A]): Marshaller[scalaz.\/[Throwable, A]] =
    Marshaller[scalaz.\/[Throwable, A]] { (value, ctx) =>
      value match {
        case -\/(e) => throw e
        case \/-(s) => m(s, ctx)
      }
    }

  implicit val taskMarshaller: MarshallerM[Task] =
    new MarshallerM[Task] {
      def marshaller[T](implicit tm: Marshaller[T]) = {
        new Marshaller[Task[T]] {
          def apply(value: Task[T], ctx: MarshallingContext): Unit = {
            value runAsync { _.fold(ctx.handleError, tm(_, ctx)) }
          }
        }
      }
    }

  implicit val UUIDDeserializer = new Deserializer[String, java.util.UUID] {
    override def apply(v1: String): Deserialized[UUID] =
      try scala.util.Right(java.util.UUID.fromString(v1))
      catch {
        case e: Exception =>
          scala.util.Left(MalformedContent(e.getMessage, e))
      }
  }
}
