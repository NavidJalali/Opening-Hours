package io.navidjalali.models

import io.circe.Decoder
import sttp.tapir.Schema

enum TimeEntryType:
  case Open, Close

object TimeEntryType:
  private def fromString(raw: String): Option[TimeEntryType] =
    values.find(_.toString.toLowerCase == raw)

  given Decoder[TimeEntryType] =
    Decoder.decodeString
      .emap(raw =>
        values
          .find(_.toString.toLowerCase == raw)
          .toRight(s"Invalid TimeEntryType: $raw")
      )

  given Schema[TimeEntryType] =
    Schema.schemaForString.map(fromString)(_.toString.toLowerCase)
