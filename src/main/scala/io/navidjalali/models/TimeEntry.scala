package io.navidjalali.models

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import sttp.tapir.Schema

import scala.annotation.unused

@unused
final case class TimeEntry(
  `type`: TimeEntryType,
  value: Time
)

object TimeEntry:
  given Decoder[TimeEntry] = deriveDecoder

  given Encoder[TimeEntry] = deriveEncoder

  given Ordering[TimeEntry] = Ordering.by(_.value)

  given Schema[TimeEntry] = Schema.derived[TimeEntry]
