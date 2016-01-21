package com.grasswire.common

import com.grasswire.common.Implicits.TaskPimps
import scalaz.concurrent.Task
import com.grasswire.common.json_models._
import com.grasswire.common.config.GWEnvironment
import com.grasswire.common.models.RedisCache
import scala.concurrent.ExecutionContext
import play.api.libs.json.Json
import com.grasswire.common.logging.Logging

sealed trait DatastoreWriteOp
case class CacheShortStories(stories: List[StoryJsonModel]) extends DatastoreWriteOp
case class CacheFullStory(story: StoryJsonModel) extends DatastoreWriteOp
case class RemoveShortStory(id: Long) extends DatastoreWriteOp

object Datastore extends Logging {

  def getActiveStories(onCacheMiss: => Task[List[StoryJsonModel]])(env: GWEnvironment)(implicit ec: ExecutionContext): Task[Iterable[StoryJsonModel]] = {
    Task.fromScalaDeferred(env.redis.hGetAll[StoryJsonModel](RedisCache.activeStoresKey))
      .map(s => s.map(s1 => s1.values))
      .flatMap {
        case None =>
          logger.info("cache MISS for active stories")
          onCacheMiss.flatMap { stories =>
            if (stories.nonEmpty) {
              Task.fromScalaDeferred(
                env.redis.hmSet[StoryJsonModel](RedisCache.activeStoresKey, Map(stories.map(story => (story.id.toString, story)): _*)))
                .map(_ => stories)
            } else {
              Task.now(Nil)
            }

          }
        case Some(stories) =>
          logger.info("cache HIT")
          Task.now(stories)
      }
  }

  def getStoryById(id: StoryId, onCacheMiss: Task[Option[StoryJsonModel]])(env: GWEnvironment)(implicit ec: ExecutionContext): Task[Option[StoryJsonModel]] = {
    Task.fromScalaDeferred(env.redis.get[StoryJsonModel](RedisCache.fullStoryKey(id)))
      .flatMap {
        case Some(story) =>
          logger.info(s"cache HIT for story $id")
          Task.now(Some(story))
        case None =>
          logger.info(s"cache MISS for story $id")

          onCacheMiss.flatMap {
            case Some(story) =>
              Task.fromScalaDeferred(env.redis.set(RedisCache.fullStoryKey(story.id), story)).map(_ => Some(story))
            case None => Task.now(None)
          }

      }
  }

  def flushActiveStories(ids: List[Long])(redis: scredis.Redis)(implicit ec: ExecutionContext): Task[Long] = {
    Task.fromScalaDeferred {
      redis.hDel(RedisCache.activeStoresKey, ids.map(_.toString): _*)
    }
  }

  def cacheStory(story: StoryJsonModel)(redis: scredis.Redis)(implicit ec: ExecutionContext): Task[Unit] = {
    val shortStory = story.copy(links = story.links.sortBy(link => -link.createdAt).take(4))
    if (!story.hidden) {
      Task.fromScalaDeferred {
        redis.inTransaction { transx =>
          transx.set(RedisCache.fullStoryKey(story.id), story)
          transx.hmSet(RedisCache.activeStoresKey, Map(story.id.toString -> shortStory))
        }
      }.map(_ => ())
    } else {
      Task.fromScalaDeferred {
        redis.inTransaction { transx =>
          transx.hmSet(RedisCache.activeStoresKey, Map(story.id.toString -> shortStory))
          transx.hDel(RedisCache.activeStoresKey, story.id.toString)
        }
      }.map(_ => ())
    }
  }

  // def write(op: DatastoreWriteOp)(redis: scredis.Redis)(implicit ec: ExecutionContext): Task[Unit] = {
  //   op match {
  //     case CacheShortStories(stories) =>
  //       Task.fromScalaDeferred(redis.hmSet(RedisCache.activeStoresKey, Map(stories.map(story => (story.id.toString, story)): _*))).map(_ => ())
  //     case CacheFullStory(story) =>
  //       Task.fromScalaDeferred(redis.set(RedisCache.fullStoryKey(story.id), story)).map(_ => ())
  //     case RemoveShortStory(id) =>
  //       Task.fromScalaDeferred(redis.hDel(RedisCache.activeStoresKey, id.toString)).map(_ => ())
  //   }
  // }

  def flushCache(key: String)(redis: scredis.Redis)(implicit ec: ExecutionContext): Task[Long] = Task.fromScalaDeferred(redis.del(key))
}
