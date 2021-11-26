package com.example.helloworld

import akka.NotUsed
import akka.stream.FlowShape
import akka.stream.scaladsl.{Balance, Flow, GraphDSL, Merge, Source}

class LongTaskService {

  def balancer[In, Out](worker: Flow[In, Out, Any], workerCount: Int): Flow[In, Out, NotUsed] = {
    import akka.stream.scaladsl.GraphDSL.Implicits._

    Flow.fromGraph(GraphDSL.create() { implicit b =>
      val balancer = b.add(Balance[In](workerCount, waitForAllDownstreams = true))
      val merge = b.add(Merge[Out](workerCount))

      for (_ <- 1 to workerCount) {
        balancer ~> worker.async ~> merge
      }

      FlowShape(balancer.in, merge.out)
    })
  }

  def takeVeryLongTask(): Source[String, NotUsed] =
    Source.fromIterator(() => List("1", "2" , "3", "4", "5", "6", "7").iterator)
      .via(balancer(longFlow, 7))
      .fold("")((a, b) => a + b)

  def longFlow: Flow[String, String, NotUsed] = Flow.fromFunction[String, String](magicNumber => {
    Thread.sleep(5000)
    magicNumber
  })
}


object LongTaskService {
  def apply(): LongTaskService = new LongTaskService()
}
