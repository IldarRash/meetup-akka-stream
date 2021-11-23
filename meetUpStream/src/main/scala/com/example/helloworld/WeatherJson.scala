package com.example.helloworld

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, NullOptions, RootJsonFormat}

object WeatherJson extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val coord: RootJsonFormat[Coord] = jsonFormat2(Coord)
  implicit val weather: RootJsonFormat[Weather] = jsonFormat3(Weather)
  implicit val weatherEvent: RootJsonFormat[EventWeather] = jsonFormat2(EventWeather)
}
