package com.example.helloworld

import akka.NotUsed
import akka.actor.Cancellable
import akka.stream.scaladsl.Source

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration.DurationInt

final case class Event(id: String, cost: Int, desc: String, time: Long)


class EventService() {
  private val counter = new AtomicInteger(1)
  def getAllEvent: Source[Event, Cancellable] =
    Source
      .tick(1.seconds, 1.seconds, NotUsed)
      .map(_ => System.currentTimeMillis())
      .map(time => Event.apply(counter.incrementAndGet().toString, 15, "desk", time))

  def getEvent(filterRequest: FilterRequest): Unit = {

  }
}
//#user-registry-actor

object EventService {
  def apply(): EventService = new EventService()
}
