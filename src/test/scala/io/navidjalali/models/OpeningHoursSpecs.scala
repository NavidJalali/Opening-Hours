package io.navidjalali.models

import io.circe.DecodingFailure
import io.circe.parser.decode
import zio.test.*
import zio.test.Assertion.*

import scala.io.Source

object OpeningHoursSpecs extends ZIOSpecDefault {
  def spec =
    suite("OpeningHours Specifications")(
      test("Can be parsed") {
        val rawInput = Source.fromResource("dummy_input.json").mkString
        val expected: OpeningHours =
          OpeningHours(
            List(
              OpeningHourPair(
                openDay = WeekDay.Tuesday,
                openTime = Time.fromSeconds(36000).get,
                closeDay = WeekDay.Tuesday,
                closeTime = Time.fromSeconds(64800).get
              ),
              OpeningHourPair(
                openDay = WeekDay.Thursday,
                openTime = Time.fromSeconds(37800).get,
                closeDay = WeekDay.Thursday,
                closeTime = Time.fromSeconds(64800).get
              ),
              OpeningHourPair(
                openDay = WeekDay.Friday,
                openTime = Time.fromSeconds(36000).get,
                closeDay = WeekDay.Saturday,
                closeTime = Time.fromSeconds(3600).get
              ),
              OpeningHourPair(
                openDay = WeekDay.Saturday,
                openTime = Time.fromSeconds(36000).get,
                closeDay = WeekDay.Sunday,
                closeTime = Time.fromSeconds(3600).get
              ),
              OpeningHourPair(
                openDay = WeekDay.Sunday,
                openTime = Time.fromSeconds(43200).get,
                closeDay = WeekDay.Sunday,
                closeTime = Time.fromSeconds(75600).get
              )
            )
          )
        assertTrue(decode[OpeningHours](rawInput) == Right(expected))
      },
      test("Can be written to human readable format") {
        val rawInput = Source.fromResource("dummy_input.json").mkString
        val expected = Source.fromResource("dummy_output").mkString
        val actual   = decode[OpeningHours](rawInput).map(_.toHumanReadable)
        assertTrue(actual == Right(expected))
      },
      test("Can handle cases where opening hours span multiple days") {
        val openingHours = OpeningHours(
          List(
            OpeningHourPair.SpansOverMultipleDays(
              openDay = WeekDay.Saturday,
              openTime = Time.fromSeconds(36000).get,
              closeDay = WeekDay.Tuesday,
              closeTime = Time.fromSeconds(3600).get
            )
          )
        )
        val expected =
          """Monday: Open 24 hours
            |Tuesday: Until 1 AM
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: Closed
            |Saturday: From 10 AM
            |Sunday: Open 24 hours""".stripMargin
        assertTrue(openingHours.toHumanReadable == expected)
      },
      test("Can handle multiple opening hours per day") {
        val openingHours = OpeningHours(
          List(
            OpeningHourPair(
              openDay = WeekDay.Saturday,
              openTime = Time.fromSeconds(36000).get,
              closeDay = WeekDay.Saturday,
              closeTime = Time.fromSeconds(64800).get
            ),
            OpeningHourPair(
              openDay = WeekDay.Saturday,
              openTime = Time.fromSeconds(72000).get,
              closeDay = WeekDay.Saturday,
              closeTime = Time.fromSeconds(75600).get
            )
          )
        )
        val expected =
          """Monday: Closed
            |Tuesday: Closed
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: Closed
            |Saturday: 10 AM - 6 PM, 8 PM - 9 PM
            |Sunday: Closed""".stripMargin
        assertTrue(openingHours.toHumanReadable == expected)
      },
      test("Can handle the wrap around in the week") {
        val openingHours = OpeningHours(
          List(
            OpeningHourPair(
              openDay = WeekDay.Sunday,
              openTime = Time.fromSeconds(75600).get,
              closeDay = WeekDay.Monday,
              closeTime = Time.fromSeconds(3600).get
            )
          )
        )
        val expected =
          """Monday: Closed
            |Tuesday: Closed
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: Closed
            |Saturday: Closed
            |Sunday: 9 PM - 1 AM""".stripMargin
        assertTrue(openingHours.toHumanReadable == expected)
      },
      test(
        "If closes in less than 24 hours and before noon it is counted as the same day"
      ) {
        val openingHours = OpeningHours(
          List(
            OpeningHourPair(
              openDay = WeekDay.Sunday,
              openTime = Time.fromSeconds(75600).get,
              closeDay = WeekDay.Sunday,
              closeTime = Time.fromSeconds(3600).get
            )
          )
        )
        val expected =
          """Monday: Closed
            |Tuesday: Closed
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: Closed
            |Saturday: Closed
            |Sunday: 9 PM - 1 AM""".stripMargin
        assertTrue(openingHours.toHumanReadable == expected)
      },
      test(
        "If closes in less than 24 hours and after noon it is counted across two days"
      ) {
        val openingHours = OpeningHours(
          List(
            OpeningHourPair(
              openDay = WeekDay.Sunday,
              openTime = Time.fromSeconds(75600).get,
              closeDay = WeekDay.Monday,
              closeTime = Time.fromSeconds(75599).get
            )
          )
        )
        val expected =
          """Monday: Until 8:59 PM
            |Tuesday: Closed
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: Closed
            |Saturday: Closed
            |Sunday: From 9 PM""".stripMargin
        assertTrue(openingHours.toHumanReadable == expected)
      },
      test(
        "If closes the next day but more than 24 it is counted across two days"
      ) {
        val openingHours = OpeningHours(
          List(
            OpeningHourPair(
              openDay = WeekDay.Sunday,
              openTime = Time.fromSeconds(75600).get,
              closeDay = WeekDay.Monday,
              closeTime = Time.fromSeconds(75600 + 3600).get
            )
          )
        )
        val expected =
          """Monday: Until 10 PM
            |Tuesday: Closed
            |Wednesday: Closed
            |Thursday: Closed
            |Friday: Closed
            |Saturday: Closed
            |Sunday: From 9 PM""".stripMargin
        assertTrue(openingHours.toHumanReadable == expected)
      },
      test("Detect unbalanced opening hours") {
        val unbalanced = Source.fromResource("unbalanced.json").mkString
        val actual     = decode[OpeningHours](unbalanced)
        assertTrue(
          actual == Left(DecodingFailure("Invalid opening hours", Nil))
        )
      },
      test("Detect missing days") {
        val json   = """{"monday":  []}"""
        val actual = decode[OpeningHours](json)
        assertTrue(
          actual == Left(DecodingFailure("Missing days in opening hours", Nil))
        )
      }
    )
}
