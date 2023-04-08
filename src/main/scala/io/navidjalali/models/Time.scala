package io.navidjalali.models

import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema

import java.time.LocalTime
import scala.util.Try

opaque type Time = LocalTime

object Time:
  def apply(hour: Int, minute: Int, second: Int): Option[Time] =
    Try(LocalTime.of(hour, minute, second)).toOption

  def apply(localTime: LocalTime): Time = localTime

  def fromSeconds(seconds: Int): Option[Time] =
    Try(LocalTime.ofSecondOfDay(seconds)).toOption

  extension (t: Time)
    def toLocalTime: LocalTime = t

    def toSeconds: Int = t.toSecondOfDay

    infix def <(other: Time): Boolean  = t.toSecondOfDay < other.toSecondOfDay
    infix def <=(other: Time): Boolean = t.toSecondOfDay <= other.toSecondOfDay
    infix def >(other: Time): Boolean  = t.toSecondOfDay > other.toSecondOfDay
    infix def >=(other: Time): Boolean = t.toSecondOfDay >= other.toSecondOfDay

    def toHumanReadable: String =
      val sb = new StringBuilder
      val h  = t.getHour % 12
      if h == 0 then sb.append(12) else sb.append(h)
      if t.getMinute != 0 then
        sb.append(":")
        sb.append(f"${t.getMinute}%02d")
      sb.append(" ")
      if t.getHour < 12 then sb.append("AM") else sb.append("PM")
      sb.toString

  given Decoder[Time] = Decoder.decodeInt.emap { seconds =>
    fromSeconds(seconds).toRight(s"Invalid time: $seconds")
  }

  given Encoder[Time] = Encoder.encodeInt.contramap(_.toSeconds)

  given Schema[Time] = Schema.schemaForInt.map(Time.fromSeconds)(_.toSeconds)

  given Ordering[Time] = Ordering.by(_.toSecondOfDay)
