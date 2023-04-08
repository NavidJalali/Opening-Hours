package io.navidjalali.models

import io.circe.*

enum WeekDay:
  case Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday

object WeekDay:
  extension (day: WeekDay)
    infix def -(other: WeekDay): Int =
      val diff = day.ordinal - other.ordinal
      if diff < 0 then diff + 7 else diff

  given Ordering[WeekDay] = (x, y) => x.ordinal - y.ordinal

  given KeyDecoder[WeekDay] =
    key => WeekDay.values.find(_.toString.toLowerCase == key)

  given KeyEncoder[WeekDay] = _.toString.toLowerCase
