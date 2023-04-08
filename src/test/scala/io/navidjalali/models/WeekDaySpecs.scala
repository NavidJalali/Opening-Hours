package io.navidjalali.models

import io.circe.KeyDecoder
import zio.test.*
import zio.test.Assertion.*

import scala.util.Random

object WeekDaySpecs extends ZIOSpecDefault {
  def spec =
    suite("WeekDay Specifications")(
      test("Should be able decode as a key") {
        val keyDecoder = summon[KeyDecoder[WeekDay]]
        assertTrue(keyDecoder("monday").contains(WeekDay.Monday)) &&
        assertTrue(keyDecoder("tuesday").contains(WeekDay.Tuesday)) &&
        assertTrue(keyDecoder("wednesday").contains(WeekDay.Wednesday)) &&
        assertTrue(keyDecoder("thursday").contains(WeekDay.Thursday)) &&
        assertTrue(keyDecoder("friday").contains(WeekDay.Friday)) &&
        assertTrue(keyDecoder("saturday").contains(WeekDay.Saturday)) &&
        assertTrue(keyDecoder("sunday").contains(WeekDay.Sunday))
      },
      test("Should be sortable") {
        assertTrue(Random.shuffle(WeekDay.values).sorted.toList == WeekDay.values.toList)
      }
    )
}
