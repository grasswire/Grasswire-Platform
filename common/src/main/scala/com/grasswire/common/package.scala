package com.grasswire

import com.grasswire.common.config.GWEnvironment
import org.joda.time.{ DateTime, DateTimeZone }

import scalaz._
import scalaz.concurrent.Task

package object common {

  def average[T](ts: Iterable[T])(implicit num: Numeric[T]) = {
    num.toDouble(ts.sum) / ts.size
  }

  case class CacheObject(key: String, value: String)

  type ProcessorTask[A] = ReaderT[Task, GWEnvironment, A]

  object ProcessorTask {
    def pure[A](a: A): ProcessorTask[A] = Applicative[ProcessorTask].pure(a)
  }
  type RabbitMQConnection = com.rabbitmq.client.Connection
  type DBDef = com.grasswire.common.db.GWDatabaseDriver.Backend#DatabaseDef
  type PrimaryKey = Long
  type ContentId = java.util.UUID
  type StoryId = Long
  type LinkId = Long
  type CommentId = java.util.UUID
  type FeedId = Long
  type Username = String
  type Tagname = String
  type EventId = Long
  type ContentType = String
  type WikiVersion = Long
  type Reputation = Int
  type RMQMessageHeaders = Map[String, String]

  def nowUtcMillis = DateTime.now.withZone(DateTimeZone.UTC).getMillis
  def nowUtc = DateTime.now.withZone(DateTimeZone.UTC)

  def decode(msg: Array[Byte], charsetName: String): \/[Throwable, String] =
    \/.fromTryCatchNonFatal(new String(msg, charsetName))

  def decodeUtf8(msg: Array[Byte]) = decode(msg, "UTF-8")

  val TweetContentType: ContentType = "tweet"
  val LinkSubmissionContentType: ContentType = "linksubmission"
  val VoteEventType = "vote"

  val voteWeight = 5

}
