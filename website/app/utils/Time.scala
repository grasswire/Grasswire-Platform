package utils

import java.util.Locale

import org.joda.time.format.PeriodFormatterBuilder
import org.joda.time.{Hours, Period, DateTimeZone, DateTime}

object Time {

  def parseString(time: String) =
     DateTime.parse(time).withZone(DateTimeZone.UTC)

  def dateTimeNowUTC = DateTime.now(DateTimeZone.UTC)

  def toMonthDayString(dt: DateTime) = dt.toString("MMMMMMMM dd", Locale.US)


  def hoursAgo(time: DateTime) =
     Hours.hoursBetween(time, dateTimeNowUTC).getHours()

  def timeAgoString(dateTime: DateTime):String = {
    val period = new Period(dateTime, dateTimeNowUTC)
    buildFormatter(period).print(period) + " " + "ago"
  }

  def timeAgoStringInternal(dateTime: DateTime): String =
    daysHoursMinutes.print(new Period(dateTime, dateTimeNowUTC))

  def buildFormatter(period: Period) = {
    val periodFormatterBuilder = new PeriodFormatterBuilder
    if(period.getDays > 0) {
       periodFormatterBuilder.appendDays()
        .appendSuffix(" day", " days").toFormatter
    } else if(period.getHours > 0) {
      periodFormatterBuilder.appendHours()
        .appendSuffix(" hour", " hours").toFormatter
    } else if(period.getMinutes > 0) {
       periodFormatterBuilder.appendMinutes()
        .appendSuffix(" minute", " minutes").toFormatter
    } else {
       periodFormatterBuilder.appendSeconds()
        .appendSuffix(" second", " seconds").toFormatter
    }
  }

  val daysHoursMinutes = new PeriodFormatterBuilder()
    .appendDays()
    .appendSuffix(" day", " days")
    .appendSeparator(" and ")
    .appendMinutes()
    .appendSuffix(" minute", " minutes")
    .appendSeparator(" and ")
    .appendSeconds()
    .appendSuffix(" second", " seconds")
    .toFormatter

}
