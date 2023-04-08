package io.navidjalali.models

import io.circe.{Decoder, DecodingFailure, Encoder}

import scala.util.chaining.*
import cats.syntax.*
import cats.implicits.*
import sttp.tapir.Schema

opaque type OpeningHours = Seq[OpeningHourPair]

object OpeningHours:
  def apply(openingHours: Seq[OpeningHourPair]): OpeningHours = openingHours

  def fromMap(map: Map[WeekDay, Seq[TimeEntry]]): Option[OpeningHours] =
    map.toList.sortBy { case (day, _) => day }.flatMap { case (day, entries) =>
      entries.sorted.map(entry => (day, entry.value, entry.`type`))
    }.pipe {
      // if it starts with a closing time that means that it opened last week sunday
      // in that case we push it to the end of the list to pair it with it's opening time
      case (head @ (_, _, entryType)) :: tail
          if entryType == TimeEntryType.Close =>
        tail :+ head
      case other => other
    }
      .grouped(2)
      .toSeq
      .traverse {
        case Seq(
              (openDay, openTime, TimeEntryType.Open),
              (closeDay, closeTime, TimeEntryType.Close)
            ) =>
          Some(
            OpeningHourPair(
              openDay = openDay,
              openTime = openTime,
              closeDay = closeDay,
              closeTime = closeTime
            )
          )
        case _ => None
      }

  extension (openingHours: OpeningHours)
    def asMap: Map[WeekDay, Seq[TimeEntry]] =
      val open = openingHours.flatMap { openingHours =>
        List(
          openingHours.openDay -> TimeEntry(
            TimeEntryType.Open,
            openingHours.openTime
          ),
          openingHours.closeDay -> TimeEntry(
            TimeEntryType.Close,
            openingHours.closeTime
          )
        )
      }.groupBy { case (day, _) => day }.map { case (day, entries) =>
        day -> entries.map { case (_, entry) => entry }
      }

      WeekDay.values.foldLeft(open) { (acc, day) =>
        acc.updated(day, acc.getOrElse(day, Seq.empty))
      }

    def toHumanReadable: String =
      val hours = openingHours.foldLeft(Map.empty[WeekDay, Seq[String]]) {
        case acc -> OpeningHourPair.SpansOverMultipleDays(
              openDay,
              openTime,
              closeDay,
              closeTime
            ) =>
          // The days in the middle are open 24 hours and the first day and the last day
          // have `From X` and `Until X` respectively.
          (WeekDay.values ++ WeekDay.values)
            .dropWhile(_ != openDay)
            .tail
            .takeWhile(_ != closeDay)
            .foldLeft(acc) { (acc, day) =>
              acc.updated(day, Seq("Open 24 hours"))
            }
            .updated(
              openDay,
              acc.getOrElse(openDay, Seq.empty) :+
                s"From ${openTime.toHumanReadable}"
            )
            .updated(
              closeDay,
              acc.getOrElse(closeDay, Seq.empty) :+
                s"Until ${closeTime.toHumanReadable}"
            )
        case acc -> openingHours =>
          acc.updated(
            openingHours.openDay,
            acc.getOrElse(openingHours.openDay, Seq.empty) :+
              s"${openingHours.openTime.toHumanReadable} - ${openingHours.closeTime.toHumanReadable}"
          )
      }
      WeekDay.values
        .map(day =>
          s"${day}: ${hours.get(day).fold("Closed")(_.mkString(", "))}"
        )
        .mkString("\n")

  given Decoder[OpeningHours] =
    Decoder.decodeMap[WeekDay, Seq[TimeEntry]].emap { map =>
      for
        _ <- Either.cond(
               WeekDay.values.forall(map.contains),
               (),
               "Missing days in opening hours"
             )
        openingHours <- fromMap(map).toRight("Invalid opening hours")
      yield openingHours
    }

  given Encoder[OpeningHours] =
    Encoder.encodeMap[WeekDay, Seq[TimeEntry]].contramap(_.asMap)

  given Schema[OpeningHours] =
    Schema
      .schemaForMap[WeekDay, Seq[TimeEntry]](_.toString)
      .map(fromMap)(_.asMap)
