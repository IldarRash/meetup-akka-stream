package com.example.helloworld

import akka.NotUsed
import akka.actor.Cancellable
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{FlowShape, SinkShape}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, Source, Zip}
import com.google.protobuf.duration.Duration.defaultInstance.seconds
import spray.json.DefaultJsonProtocol._

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration.DurationInt

case class Event(id: String, userName: String, city: String, desc: String, itemName: String, cost: Int, time: Long)

case class EventW(id: String, event: Event, weather: Weather)

case class Coord(lon: Double = 0.0, lat: Double = 0.0)

case class EventWeather(coord: Coord, weather: List[Weather])

case class Weather(id: Long = 0, main: String = "Empty", description: String = "Empty")

class EventService(fileService: FileService)(implicit val system: ActorSystem[_]) {
  implicit val coord = jsonFormat2(Coord)
  implicit val weather = jsonFormat3(Weather)
  implicit val weatherEvent = jsonFormat2(EventWeather)

  private val key = "c4039d97635076407057b0cb34b46da3"
  private val httpClient = Http().singleRequest(HttpRequest(uri = "http://api.openweathermap.org/data/2.5/"))
  private val counter = new AtomicInteger(1)

/*  def getAllEvent: Source[Event, Cancellable] =
    Source
      .tick(1.seconds, 1.seconds, NotUsed)
      .map(_ => System.currentTimeMillis())
      .map(time => Event.apply(counter.incrementAndGet().toString, 15, "desk", time))*/

  def getItemsByFilter(filterRequest: FilterRequest) =
    Source.fromIterator(() => fileService.getAllFilesFromDir(filterRequest.dir))
      .flatMapConcat(file => fileService.getAllStringFromFile(file))
      .filter(_.nonEmpty)
      .map(strToEvent)
      .filter(event => filterRequest.filterName.equals(event.userName))



 /* def getWeather(city: String) =
    Source.future(Unmarshal(
      Http()
        .singleRequest(
          HttpRequest(
            uri = s"http://api.openweathermap.org/data/2.5/weather?q=${city}&appid=${key}")
        )
    ).to[EventWeather])
    .throttle(1, 5.seconds)*/

  def strToEvent(line: String): Event = {
    val items = line.split(",")
    Event.apply(items(0), items(1), items(2), items(3), items(5), items(4).toInt, System.currentTimeMillis())
  }
}

object EventService {
  def apply(implicit actorSystem: ActorSystem[_]): EventService = new EventService(new FileService)
}
