package com.example.helloworld

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Source

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

case class Event(id: String, userName: String, city: String, desc: String, itemName: String, cost: Int, time: Long)

case class EventW(id: String, event: Event, eventWeather: EventWeather)

case class Coord(lon: Double = 0.0, lat: Double = 0.0)

case class EventWeather(coord: Coord, weather: List[Weather])

case class Weather(id: Long = 0, main: String = "Empty", description: String = "Empty")

class EventService(fileService: FileService)(implicit val system: ActorSystem[_]) {
  import WeatherJson._
  private val key = "c4039d97635076407057b0cb34b46da3"

  def getItemsByFilter(filterRequest: FilterRequest) =
    Source.fromIterator(() => fileService.getAllFilesFromDir(filterRequest.dir))
      .flatMapConcat(file => fileService.getAllStringFromFile(file))
      .filter(_.nonEmpty)
      .map(strToEvent)
      .filter(event => filterRequest.filterName.equals(event.userName))
      .mapAsync(1)(getWeather)
      .throttle(1, 1.seconds)

def getWeather(event: Event) =
      Http()
        .singleRequest(
          HttpRequest(
            uri = s"http://api.openweathermap.org/data/2.5/weather?q=${event.city}&appid=${key}")
        )
        .flatMap(answer =>
          Unmarshal.apply(answer).to[EventWeather]
        )
        .map(weather => EventW.apply(event.id, event, weather)
  )

  def strToEvent(line: String): Event = {
    val items = line.split(",")
    Event.apply(items(0), items(1), items(2), items(3), items(5), items(4).toInt, System.currentTimeMillis())
  }
}

object EventService {
  def apply(implicit actorSystem: ActorSystem[_]): EventService = new EventService(new FileService)
}
