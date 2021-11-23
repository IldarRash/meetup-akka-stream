package com.example.helloworld

//#import
import scala.concurrent.Future
import akka.NotUsed
import akka.actor.Cancellable
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}

//#import

//#service-request-reply
//#service-stream
class EventServiceImpl(system: ActorSystem[_], eventService: EventService) extends EventStreamService {
  private implicit val sys: ActorSystem[_] = system

  override def getEvent(in: FilterRequest): Future[EventResponse] =
    Future.successful(EventResponse.apply("1", 15, in.filterName, System.currentTimeMillis()))

  val flow: Flow[Event, EventResponse, NotUsed] =
    Flow.fromFunction[Event, EventResponse](event => EventResponse.apply(event.id, event.cost, event.desc, event.time))

  override def getFilterEvent(in: FilterRequest): Source[EventResponse, NotUsed] =
    eventService.getItemsByFilter(in)
      .take(10)
      .viaMat(flow)(Keep.right)
}
