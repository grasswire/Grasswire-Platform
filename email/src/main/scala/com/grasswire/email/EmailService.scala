package com.grasswire.email

import com.grasswire.common.models.{ HtmlEmail, PlainTextEmail, GrasswireEmail }
import com.sendgrid.SendGrid
import com.sendgrid.SendGrid.Email

case class EmailService(sendgrid: SendGrid) {

  def create(gwEmail: GrasswireEmail) = {
    val email = new Email()
    email.addTo(gwEmail.toAddress.address)
    email.setFrom(gwEmail.fromAddress.address)
    email.setFromName(gwEmail.fromName)
    email.setSubject(gwEmail.subject)
    gwEmail.body match {
      case PlainTextEmail(plaintext) => email.setText(plaintext)
      case HtmlEmail(html) => email.setHtml(html)
    }
    email
  }

  def send(email: Email): SendGrid.Response = sendgrid.send(email)
}
