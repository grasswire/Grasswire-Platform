package utils

import play.api.mvc.{Security, Request}

object SessionsHelper {

  def getUsername(implicit req: Request[Any]): Option[String] = req.session.get(Security.username)

}
