import akka.actor._
import akka.routing.RoundRobinRouter
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.math._

object project1 {
  sealed trait SqMessage
  case object Calculate extends SqMessage
  case class Work(start: Int, nrOfElements: Int, step: Int) extends SqMessage
  class Worker extends Actor {

    def calculateSqFor(start: Int, nrOfElements: Int, step: Int) = {
      var acc = 0
      var value1 = 0
      var k = 0      
      
      for (j <- start to (start + step - 1))  
      { 
        acc = 0
      for (i <- j to (j + nrOfElements - 1)) {  
      acc +=(i * i)     
      }
      value1 = Math.sqrt(acc).toInt
      if ((value1 * value1) == acc){ 
         println(j)
      }
      }
    }

    def receive = {
      case Work(start, nrOfElements, step) =>
          calculateSqFor(start, nrOfElements, step)   
    }

}
    class Master(nrOfWorkers: Int, 
    nrOfMessages: Int, 
    nrOfElements: Int) extends Actor {
    var nrOfResults: Int = _
    var TotalTime: Duration = _
    val start: Long = System.currentTimeMillis

    val workerRouter = context.actorOf(
      Props[Worker].withRouter(RoundRobinRouter(nrOfWorkers)), name = "workerRouter")

    def receive = {
      case Calculate =>
        var k:Int = 10
        for (i <- 1 to nrOfMessages by k) {
          workerRouter ! Work(i, nrOfElements, k)
        }
    }
    TotalTime = (System.currentTimeMillis - start).millis
    println("Calculation time: ",TotalTime)
    context.system.shutdown()
  }

    // Create an Akka system
    def main(args: Array[String]) = {
      if (args.length < 2) { 
        println("Please provide the input")
        System.exit(0)
      }
    val system = ActorSystem("SqSystem")
    var nrOfWorkers: Int = 10
    var nrOfMessages: Int = args(0).toInt
    var nrOfElements: Int = args(1).toInt
    val master = system.actorOf(Props(new Master(
      nrOfWorkers, nrOfMessages, nrOfElements)),
      name = "master")
    master ! Calculate

  }
}
