package services

import com.grasswire.common.json_models.UserJsonModel
import play.api.mvc.{Session, Security}
import com.grasswire.common.models.{Session => GWSession}

object SessionService {

  val TWITTER_SCREEN_NAME = Security.username
  val NAME = "name"
  val CREATED_AT = "created_at"
  val KARMA = "karma"
  val TOKEN = "token"
  val EMAIL = "email"
  val ID = "id"
  val HELL_BANNED = "hell_banned"
  val IS_ADMIN = "is_admin"
  val PROFILE_IMAGE_URL = "profile_image_url"

  def get(session: Session): Option[GWSession] = for {
    screenName <- session.get(TWITTER_SCREEN_NAME)
    name <- session.get(NAME)
    createdAt <- session.get(CREATED_AT).map(_.toLong)
    karma <- session.get(KARMA).map(_.toInt)
    hellBanned <- session.get(HELL_BANNED).map(_.toBoolean)
    token <- session.get(TOKEN)
    id <- session.get(ID).map(_.toLong)
    isAdmin <- session.get(IS_ADMIN).map(_.toBoolean)
    profileImageUrl <- session.get(PROFILE_IMAGE_URL)
  } yield GWSession(token, UserJsonModel(screenName, name, session.get(EMAIL), karma, id, createdAt, hellBanned, isAdmin, profileImageUrl), isFirstLogin = false)

  def toHttpSession(session: GWSession): Session = {
    val sessionMap = Map(TWITTER_SCREEN_NAME -> session.user.twitterScreenName, NAME -> session.user.twitterName, CREATED_AT -> session.user.createdAt.toString,
      KARMA -> session.user.karma.toString, TOKEN -> session.token, ID -> session.user.id.toString, HELL_BANNED -> session.user.hellBanned.toString,
      IS_ADMIN -> session.user.isAdmin.toString, PROFILE_IMAGE_URL -> session.user.profileImageUrl
    )
    session.user.email match {
      case None =>  Session(sessionMap)
      case Some(e) => Session(sessionMap + (EMAIL -> e))
    }
  }

  implicit class SessionOps(session: Session) {
    def toGWSession = get(session)
  }

  implicit class GWSessionOps(session: GWSession) {
    def asHttpSession = toHttpSession(session)
  }
}
