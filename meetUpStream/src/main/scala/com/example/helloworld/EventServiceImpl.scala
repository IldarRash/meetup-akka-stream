package com.example.helloworld

//#import
import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}

import scala.concurrent.Future

//#import

//#service-request-reply
//#service-stream
class EventServiceImpl(system: ActorSystem[_], eventService: EventService) extends EventStreamService {
  private implicit val sys: ActorSystem[_] = system

  override def getEvent(in: FilterRequest): Future[EventResponse] =
    eventService.takeVeryLongTask()
      .map(id => EventResponse.apply(id = id))
      .runWith(Sink.last)

  val flow: Flow[EventW, EventResponse, NotUsed] =
    Flow.fromFunction[EventW, EventResponse](eventW =>
      EventResponse.apply(eventW.id,
        Some(UserResponse.apply(cost = eventW.event.cost, desc = eventW.event.desc, time = eventW.event.time, userName = eventW.event.userName, itemName = eventW.event.itemName)),
        Some(WeatherResponse.apply(id = 0, main = eventW.eventWeather.coord.toString, desc = eventW.eventWeather.weather.toString())))
    )

  override def getFilterEvent(in: FilterRequest): Source[EventResponse, NotUsed] =
    eventService.getItemsByFilter(in)
      .take(10)
      .viaMat(flow)(Keep.right)
}
