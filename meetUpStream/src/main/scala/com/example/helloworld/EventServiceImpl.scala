package com.example.helloworld

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.KillSwitches
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}

import scala.concurrent.Future

class EventServiceImpl(system: ActorSystem[_], eventService: EventService, longTaskService: LongTaskService) extends EventStreamService {
  private implicit val sys: ActorSystem[_] = system

  override def getEvent(in: FilterRequest): Future[EventResponse] = {
    val runnableGraph= longTaskService.takeVeryLongTask()
      .map(id => EventResponse.apply(id = id))
      .viaMat(KillSwitches.single)(Keep.right)
      .toMat(Sink.last)(Keep.both)
      .run()

    runnableGraph._2
  }

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

  override def messanger(in: Source[EchoRequest, NotUsed]): Source[EchoResponse, NotUsed] = {
    in.runWith(inboundHub)
    outboundHub
  }

  val (inboundHub: Sink[EchoRequest, NotUsed], outboundHub: Source[EchoResponse, NotUsed]) =
    MergeHub.source[EchoRequest]
      .map(request => EchoResponse.apply(request.userId, s"Hello, from ${request.userId} : ${request.message}"))
      .toMat(BroadcastHub.sink[EchoResponse])(Keep.both)
      .run()
}
