package com.grasswire.common.models

case class FromEmailAddress(address: String)
case class RecipientEmailAddress(address: String)
sealed trait EmailBody
case class PlainTextEmail(body: String) extends EmailBody
case class HtmlEmail(body: String) extends EmailBody
case class GrasswireEmail(toAddress: RecipientEmailAddress, fromAddress: FromEmailAddress, fromName: String, subject: String, body: EmailBody)

