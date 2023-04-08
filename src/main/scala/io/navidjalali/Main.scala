package io.navidjalali

import io.navidjalali.api.Routes
import io.navidjalali.models.*
import org.http4s.blaze.server.BlazeServerBuilder
import zio.interop.catz.*
import zio.*
import zio.Console.printLine
import zio.stream.{ZSink, ZStream}

object Main extends ZIOAppDefault:
  val server = for
    executor <- ZIO.executor
    routes   <- ZIO.service[Routes]
    // _        <- routes.writeSwagger
    _ <- BlazeServerBuilder[Task]
           .withExecutionContext(executor.asExecutionContext)
           .bindHttp(8080, "0.0.0.0")
           .withHttpApp(routes.app)
           .serve
           .compile
           .drain
  yield ()

  def run =
    server
      .provide(
        Routes.live
      )
