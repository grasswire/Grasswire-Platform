package com.grasswire.common.email

import com.grasswire.common.logging.Logging
import play.api.libs.ws.ning.NingWSClient
import play.api.libs.ws.{ WS, WSAuthScheme }
import com.grasswire.common.CommonConfig

import scalaz.concurrent.Task

object Mailchimp extends Logging {
  import com.grasswire.common.Implicits.TaskPimps

  val mailchimpDC = "us2"
  val mailchimpAPIKey = CommonConfig.mailchimpAPIKey
  import scala.concurrent.ExecutionContext.Implicits.global

  def subscribeToList(listId: String, email: String)(implicit client: NingWSClient): Task[Unit] = {
    val body = s"""{
          "email_address": "$email",
          "status": "subscribed"
        }"""
    Task.fromScalaDeferred(WS.clientUrl(s"https://$mailchimpDC.api.mailchimp.com/3.0/lists/$listId/members/")
      .withAuth("austenallred", mailchimpAPIKey, WSAuthScheme.BASIC)
      .post(body).map(response => logger.info(response.body)))
  }
}
