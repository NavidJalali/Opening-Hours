package io.navidjalali.models

import io.circe.Json
import zio.test.*
import zio.test.Assertion.*

import java.time.LocalTime

object TimeSpecs extends ZIOSpecDefault {
  def spec =
    suite("Time Specifications")(
      test("Should be able to be parsed from json number of seconds") {
        val midnight: Json               = Json.fromInt(0)
        val midnightLocalTime: LocalTime = LocalTime.of(0, 0)

        val noon: Json               = Json.fromInt(12 * 60 * 60)
        val noonLocalTime: LocalTime = LocalTime.of(12, 0)

        val tenAm: Json               = Json.fromInt(10 * 60 * 60)
        val tenAmLocalTime: LocalTime = LocalTime.of(10, 0)

        val tenPm: Json               = Json.fromInt(22 * 60 * 60)
        val tenPmLocalTime: LocalTime = LocalTime.of(22, 0)

        val tenThirtyAm: Json               = Json.fromInt(10 * 60 * 60 + 30 * 60)
        val tenThirtyAmLocalTime: LocalTime = LocalTime.of(10, 30)

        val tenThirtyPm: Json               = Json.fromInt(22 * 60 * 60 + 30 * 60)
        val tenThirtyPmLocalTime: LocalTime = LocalTime.of(22, 30)

        assertTrue(midnight.as[Time].map(_.toLocalTime) == Right(midnightLocalTime)) &&
        assertTrue(noon.as[Time].map(_.toLocalTime) == Right(noonLocalTime)) &&
        assertTrue(tenAm.as[Time].map(_.toLocalTime) == Right(tenAmLocalTime)) &&
        assertTrue(tenPm.as[Time].map(_.toLocalTime) == Right(tenPmLocalTime)) &&
        assertTrue(tenThirtyAm.as[Time].map(_.toLocalTime) == Right(tenThirtyAmLocalTime)) &&
        assertTrue(tenThirtyPm.as[Time].map(_.toLocalTime) == Right(tenThirtyPmLocalTime))
      },
      test("Should be correctly encoded into human readable form") {
        assertTrue(Time(LocalTime.of(0, 0)).toHumanReadable == "12 AM") &&
        assertTrue(Time(LocalTime.of(12, 0)).toHumanReadable == "12 PM") &&
        assertTrue(Time(LocalTime.of(10, 0)).toHumanReadable == "10 AM") &&
        assertTrue(Time(LocalTime.of(22, 0)).toHumanReadable == "10 PM") &&
        assertTrue(Time(LocalTime.of(10, 30)).toHumanReadable == "10:30 AM") &&
        assertTrue(Time(LocalTime.of(22, 30)).toHumanReadable == "10:30 PM")
        assertTrue(Time(LocalTime.of(12, 0, 59)).toHumanReadable == "12 PM")
      }
    )
}
