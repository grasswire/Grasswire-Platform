package com.grasswire.common.models

/**
 * Levi Notik
 * Date: 2/8/14
 */

import JsonHelper._
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

sealed trait MsgType {
  def name: String
}

trait PublishableMessage[A] {
  def msg(a: A): String
}

object RedisCache {

  val activeStoresKey = "stories.active.all"
  def fullStoryKey(id: Long) = s"story.full.$id"

  val mylogger = LoggerFactory.getLogger(getClass.getSimpleName)

  def publishMessage[A](redis: scredis.Redis, channel: String = RedisCache.allEventsChannel)(a: A)(implicit ev: PublishableMessage[A]): Future[Long] =
    redis.publish(channel, ev.msg(a))

  def setExpiringKey[A: scredis.serialization.Writer](key: String, value: A, expireAfter: FiniteDuration)(redis: scredis.Redis): Future[Boolean] =
    redis.set(key, value, Some(expireAfter))

  def setExpiringKeyWithClient[A: scredis.serialization.Writer](key: String, value: A, expireAfter: FiniteDuration)(redis: scredis.Redis): Future[Boolean] =
    redis.set(key, value, Some(expireAfter))

  // def keyExists(key: String)(redis: scredis.Redis): Boolean =
  //   redis.withClient(_.get(key).isDefined)

  def findKey(key: String)(redis: scredis.Redis): Future[Option[String]] =
    redis.get[String](key)

  val factCheckMsgType = new MsgType {
    override def name: String = "fact-check"
  }
  val tweetMsgType = new MsgType {
    override def name: String = "tweet"
  }

  val voteMsgType = new MsgType {
    override def name: String = "vote"
  }

  val commentMsgType = new MsgType {
    override def name: String = "comment"
  }

  def tagChannel(tag: String) = s"channel.events.$tag"

  def allEventsChannel = "channel.events.all"

  object Hashes {
    def feedRank(feedId: Long) = s"feedrank-$feedId"
  }

  def sessionKey(username: String) = s"session_key:$username"
}
