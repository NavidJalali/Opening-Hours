package io.navidjalali.api

import io.navidjalali.models.*
import org.http4s.{HttpApp, HttpRoutes}
import sttp.capabilities.zio.ZioStreams
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.apispec.openapi.circe.yaml.*
import sttp.tapir.json.circe.*
import sttp.tapir.generic.auto.*
import sttp.tapir.server.http4s.ztapir.ZHttp4sServerInterpreter
import sttp.tapir.ztapir.*
import zio.interop.catz.*
import zio.stream.{ZSink, ZStream}
import zio.{Task, ZLayer}

final class Routes:
  private val openingHours: ZServerEndpoint[Any, ZioStreams] =
    endpoint.post
      .in("opening-hours")
      .in(jsonBody[OpeningHours])
      .out(stringBody)
      .description("Get opening hours in human readable format")
      .serverLogicPure(openingHours => Right(openingHours.toHumanReadable))

  val all = List(openingHours)

  val routes: HttpRoutes[Task] =
    ZHttp4sServerInterpreter[Any]().from(all).toRoutes

  val app: HttpApp[Task] = routes.orNotFound

  lazy val writeSwagger: Task[Unit] = ZStream
    .fromIterable(
      OpenAPIDocsInterpreter()
        .toOpenAPI(
          all.map(_.endpoint),
          "Wolt Home Assignment",
          "0.0.1"
        )
        .toYaml
        .getBytes
    )
    .run(ZSink.fromFileName("api.yaml"))
    .unit

object Routes:
  val live = ZLayer.succeed(new Routes)
