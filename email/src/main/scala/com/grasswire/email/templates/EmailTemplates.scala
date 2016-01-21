package com.grasswire.email.templates

import com.grasswire.common.logging.Logging
import com.grasswire.common.models._

object EmailTemplates extends Logging {

  def welcomeEmail(email: String): GrasswireEmail = {
    val stream = getClass.getResourceAsStream("/welcome.html")
    val lines = scala.io.Source.fromInputStream(stream, "UTF-8").mkString
    GrasswireEmail(RecipientEmailAddress(email), FromEmailAddress("notify@grasswire.com"), "grasswire.com", "Welcome to Grasswire", HtmlEmail(lines))
  }
}
