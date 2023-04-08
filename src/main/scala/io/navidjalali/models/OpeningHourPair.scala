package io.navidjalali.models

import java.time.LocalTime

sealed abstract class OpeningHourPair:
  val openDay: WeekDay
  val openTime: Time
  val closeDay: WeekDay
  val closeTime: Time

object OpeningHourPair:
  def apply(
    openDay: WeekDay,
    openTime: Time,
    closeDay: WeekDay,
    closeTime: Time
  ): OpeningHourPair =
    closeDay - openDay match
      case 0 => WithinSameDay(openDay, openTime, closeTime)
      case 1 =>
        if closeTime < openTime && closeTime < Time(LocalTime.NOON) then
          ClosesOnNextDay(openDay, openTime, closeTime)
        else SpansOverMultipleDays(openDay, openTime, closeDay, closeTime)
      case _ => SpansOverMultipleDays(openDay, openTime, closeDay, closeTime)

  final case class WithinSameDay(
    openDay: WeekDay,
    openTime: Time,
    closeTime: Time
  ) extends OpeningHourPair {
    val closeDay: WeekDay = openDay
  }

  final case class ClosesOnNextDay(
    openDay: WeekDay,
    openTime: Time,
    closeTime: Time
  ) extends OpeningHourPair {
    val closeDay: WeekDay = WeekDay.fromOrdinal((openDay.ordinal + 1) % 7)
  }

  final case class SpansOverMultipleDays(
    openDay: WeekDay,
    openTime: Time,
    closeDay: WeekDay,
    closeTime: Time
  ) extends OpeningHourPair
