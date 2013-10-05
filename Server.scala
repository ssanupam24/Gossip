import akka.actor._
import com.typesafe.config.ConfigFactory
//import akka.routing.RoundRobinRouter
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import scala.math._

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



object ServerApp {
  def main(args: Array[String]) {

    val system = ActorSystem("Server",ConfigFactory.load().getConfig("remote"))
    val actor = system.actorOf(Props[Worker], "worker")
    println("waiting for messages")
  }
  def startup() {
  }

  def shutdown() {
  }
}
