import akka.actor._
import akka.actor.ActorContext
import com.typesafe.config.ConfigFactory
//import akka.routing.RoundRobinRouter
import scala.concurrent.duration.Duration
import scala.concurrent.duration._
import akka.kernel.Bootable

sealed trait SqMessage
case class Work(start: Int, nrOfElements: Int, step: Int) extends SqMessage

class Master(nrOfWorkers: Int, 
    nrOfMessages: Int, 
    nrOfElements: Int) extends Actor {
    var nrOfResults: Int = _
    var TotalTime: Duration = _
    val start: Long = System.currentTimeMillis
    val remotepath =
    "akka://Server@127.0.0.1:2553/user/worker"
    val worker = context.actorFor(remotepath)	
    def receive = {
      case 10 =>
        var k:Int = 10
        for (i <- 1 to nrOfMessages by k) {
          worker ! Work(i, nrOfElements, k)
        }
        println("worker is" + worker)
    }
    TotalTime = (System.currentTimeMillis - start).millis
    println("Calculation time: ",TotalTime)
  }
object Client extends Bootable
{
    // Create an Akka system
   def main(args: Array[String]) = {
      if (args.length < 2) { 
        println("Please provide the input")
        System.exit(0)
      }
    println("master is created")
    var nrOfWorkers: Int = 10
    var nrOfMessages: Int = args(0).toInt
    var nrOfElements: Int = args(1).toInt
    val system = ActorSystem("Client", ConfigFactory.load().getConfig("local"))
    
    val master = system.actorOf(Props(new Master(
      nrOfWorkers, nrOfMessages, nrOfElements)),
      name = "master")
	 
   // val worker = context.actorFor(Props[Master], "worker")
    master! 10
    
    println("End with the calculation")

  }

  def startup() {
  }

  def shutdown() {
    
  }
}
