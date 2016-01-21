package com.grasswire.common.db.tables

import com.grasswire.common.DBDef
import com.grasswire.common.db.GWDatabaseDriver.api._
import com.grasswire.common.json_models.{ DigestJsonModel, StoryJsonModel }
import com.grasswire.common.logging.Logging
import com.grasswire.common.models.{ Paging, PagedResult, JsonHelper }
import org.joda.time.{ DateTime, DateTimeZone }
import play.api.libs.json.Json
import slick.lifted.Tag

import scalaz.concurrent.Task
import scalaz.{ Kleisli, ReaderT }

class Digests(tag: Tag) extends Table[(Option[Long], String, Long)](tag, "digests_1") {

  def data = column[String]("data", O.SqlType("TEXT"))
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def createdAt = column[Long]("created_at")

  def * = (id.?, data, createdAt)

}

object Digests extends Logging {

  import JsonHelper.formats
  import com.grasswire.common.Implicits.TaskPimps

  import scala.concurrent.ExecutionContext.Implicits.global
  def tableQuery = TableQuery[Digests]

  def insert(data: List[StoryJsonModel]): ReaderT[Task, DBDef, Long] = Kleisli[Task, DBDef, Long] { db =>
    logger.info("in the insertion shit!")
    val q = (Digests.tableQuery returning Digests.tableQuery.map(_.id)) += Tuple3(None, Json.toJson(data).toString, DateTime.now(DateTimeZone.UTC).getMillis)
    Task.fromScalaDeferred(db.run(q))
  }

  def update(id: Long, data: List[StoryJsonModel]): ReaderT[Task, DBDef, Int] = Kleisli[Task, DBDef, Int] { db =>
    val q = Digests.tableQuery.filter(_.id === id).map(_.data).update(JsonHelper.serialize(data))
    Task.fromScalaDeferred(db.run(q))
  }

  def getCurrent: ReaderT[Task, DBDef, Option[DigestJsonModel]] = Kleisli[Task, DBDef, Option[DigestJsonModel]] { db =>

    val q = for {
      d <- Digests.tableQuery.sortBy(_.id.desc)
    } yield d

    val action = q.result
    Task.fromScalaDeferred(db.run(action.headOption)).map(r => r.map { r1 =>
      Json.parse(r1._2).validate[List[StoryJsonModel]].fold(l => logger.error(l.mkString("\n")), r => ())
      DigestJsonModel(r1._1.get, r1._3, Json.parse(r1._2).validate[List[StoryJsonModel]].get)
    })

  }

  def list(offset: Int, limit: Int): ReaderT[Task, DBDef, PagedResult[DigestJsonModel]] = Kleisli[Task, DBDef, PagedResult[DigestJsonModel]] { db =>
    val qTotal = Digests.tableQuery.length
    val qDigests = Digests.tableQuery.sortBy(_.id.desc).drop(offset * limit).take(limit)
    for {
      total <- Task.fromScalaDeferred(db.run(qTotal.result))
      digest <- Task.fromScalaDeferred(db.run(qDigests.result)).map(_.map(res => DigestJsonModel(res._1.get, res._3, Json.parse(res._2).validate[List[StoryJsonModel]].get)))
    } yield PagedResult(digest.toList, Paging(offset, limit, total))

  }

  def getById(id: Long): ReaderT[Task, DBDef, Option[DigestJsonModel]] = Kleisli[Task, DBDef, Option[DigestJsonModel]] { db =>

    val q = for {
      d <- Digests.tableQuery.filter(_.id === id)
    } yield d

    val action = q.result
    Task.fromScalaDeferred(db.run(action.headOption)).map(r => r.map(r1 => DigestJsonModel(r1._1.get, r1._3, JsonHelper.deserialize[List[StoryJsonModel]](r1._2))))
  }
}
