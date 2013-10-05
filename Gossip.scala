/*Distributed implementation of Gossip and Push-Sum algorithms in Scala
  Author: Anupam
*/
import akka.actor._
import scala.math._
import scala.util.Random
import scala.math
object Gossip {

  sealed trait AlgoMessage
  case object Calculate extends AlgoMessage
  case class gossipWork(var i : Int) extends AlgoMessage
  case class pushSumWork(var s : Double, var w : Double, var i: Int) extends AlgoMessage
  case class Initialize( var arActors : List[ActorRef], count: ActorRef, neighbourList : List[Int], s_init : Int)
  case class counter( var  count : Int, var i : Int)
  case class counter_Pushsum( var  count : Int, var i : Int)
  case class Gossip(arActors : List[ActorRef]) extends AlgoMessage
  case class pushsum(arActors : List[ActorRef]) extends AlgoMessage
      
  // The worker class starts here. Here we have included the logic for Gossip and Push-Sum

  class Worker extends Actor
  {
    var nodes: List[ActorRef] = Nil
    var count  = 0
    var counterRef : ActorRef = _
    var randomNumber : Int = 0
    var  status : Boolean = true
    var neighbour:List[Int] = Nil
    var s_prev : Double = 0.0
    var w_prev : Double = 0.0
    var conv : Double = pow(10,-10)
    var s : Double = 0.0
    var w : Double = 1.0

      /*Here all the values are stored in different variables for each of the 
      actors. These variables are used in the subsequent steps for further processing. */

    def receive = 
    {
      case Initialize(arActors : List[ActorRef], count : ActorRef, neighbourList : List[Int], s_init : Int) =>
      nodes = arActors
      counterRef = count
      neighbour = neighbourList
      s = s_init
      w = 1.0

      //The Gossip logic starts here.
      case gossipWork(i) =>
     
             var j : Int = 0
             
             if(count < 10)
              {
                
               count+=1
               //println(count + " " + i)     
              }
              else  if (status)   
              {
                counterRef ! counter(count, i)
                status = false
                self ! PoisonPill
              }
        

              for(j<-1 until 10)
              {
               var randomNumber = Random.nextInt(neighbour.length)
               var next = neighbour(randomNumber)
               nodes(neighbour(randomNumber))! gossipWork(next) 
              }
      //The Push-Sum logic starts here.
      case pushSumWork(s_new,w_new,act_num) =>
              s = s + s_new
              w = w + w_new
              if(count<3)
                {
     
                  if((s_prev!=0) && (w_prev!=0))
                  {
                    var diff : Double = Math.abs((s_prev/w_prev) - (s/w))
                    if(diff <= conv)
                      {
                        count+=1
                      }
                    else
                        count =0
                  }
                }
              else if(status)
                {
                  var n = s/w
                  //println(" s/w value for  actor " + act_num + "is " + n)
                  counterRef ! counter_Pushsum(count, act_num)
                  status = false
                  self ! PoisonPill

                }
      
              if(status)
                {
                  s_prev = s
                  w_prev = w
                  s -= s/2
                  w -= w/2
 
                  for(i<-1 until 2)
                  {
                    var randomNumber = Random.nextInt(neighbour.length)
                    var next = neighbour(randomNumber)
          
                    nodes(next)!pushSumWork(s,w,next) 
                  }
                }
              }    
        } 
        
        /* The count of the actors are stored which is used for convergence in Gossip and Push-Sum
           The processing time is also printed here */

        class counting(nrOfActors : Int, start: Long, baseSystem : ActorSystem) extends Actor 
        {
            val count = new Array[Int](nrOfActors)
            var count2 = 0
            def receive = 
            {
              case counter(count1 : Int, i :Int)=>
                    count(i) = count1
                    count2 = count2 + 1
              if(count2 == ((nrOfActors)))
              { 
                println("Time Taken")
                println(((System.currentTimeMillis - start)/1000.0) + "Seconds")
                System.exit(0)
              }

              case counter_Pushsum(count1 : Int, i:Int) =>
                println("Time Taken")
                println(((System.currentTimeMillis - start)/1000.0) + "Seconds")
                System.exit(0)
            }

        }
    
        //Here the master actor is created where the messages are sent to worker actors for Gossip and Push-Sum
        class Master(nrOfActors : Int, baseSystem : ActorSystem, topology : String) extends Actor  
        {

          var i:Int = 0
          def receive = 
            {
            case Gossip(arActors : List[ActorRef]) =>
                var startNode = Random.nextInt(nrOfActors)
                arActors(startNode) ! gossipWork(startNode)

            case pushsum(arActors : List[ActorRef]) => 
                var startNode = Random.nextInt(nrOfActors)
                arActors(0) ! pushSumWork(0,0,0)

            }
              
        }

    // Create an Akka system
        def main(args: Array[String]) = 
        {
            if(args.length == 0 || args.length != 3)
            {
              println("Invalid arguments")
            }
            else if(args.length ==3)
            {
                  val gossip:String = "gossip"
                  val pushSum:String = "push-sum"
                  var nNodes:Int = args(0).toInt
                  var topology:String = args(1)
                  var algorithm:String = args(2)
                  val system = ActorSystem("SqSystem1")
                  var arActors:List[ActorRef] = Nil
                  var i : Int = 0
                  var nNodes_2D : Double = 0.0
                  if ((topology.equalsIgnoreCase("2D")) || (topology.equalsIgnoreCase("Imp2D")))
                  {
                      nNodes_2D = math.sqrt(nNodes)
                      while (nNodes_2D != nNodes_2D.toInt) 
                      {
                          nNodes= nNodes + 1
                          nNodes_2D = math.sqrt(nNodes)
                      }
                  }

                  val master = system.actorOf(Props(new Master(nNodes, system, topology)),name = "master")
                  while(i < nNodes)
                  {
                    arActors ::= system.actorOf(Props[Worker]) 
                    i+=1
                  }
                  val start = System.currentTimeMillis;
                  val counter = system.actorOf(Props(new counting(nNodes, start, system)))   
                  i = 0
                  while(i < nNodes)
                  {  
                    var neighbour:List[Int] = Nil
                    var j:Int = 0
                    if (topology.equalsIgnoreCase("line"))
                    {
                      if(i > 0)
                        neighbour ::= i - 1
                      if(i < arActors.length -1)
                        neighbour ::=  i + 1
                    }
                    if (topology.equalsIgnoreCase("full"))
                    {
                        while(j < nNodes)
                        {
                          if(j!=i)
                          {
                            neighbour ::= j
                 // println("actor" + i + "neighbour is " + neighbour(0))
                          }
                          j += 1
                        }
                    }

                    if ((topology.equalsIgnoreCase("2D")) || (topology.equalsIgnoreCase("Imp2D")))
                    {  
            
                        j = math.sqrt(nNodes).toInt
                
                        if (i % j == 0)
                        {
                            neighbour ::= i + 1 
                        }
                        if ((i+1) % j == 0)
                        {
                            neighbour ::= i - 1
                        }
                        if (i - j < 0)
                        {
                            neighbour ::= i + j 
                        }
                        if (i - (nNodes - j) >= 0)
                        {
                            neighbour ::= i - j
                        }
                        if (nNodes > 4) 
                        {
                            if ((i % j != 0) && ((i + 1) % j != 0))
                            {
                              neighbour ::= i - 1
                              neighbour ::= i + 1
                            }
                            if ((i - j > 0) && (i - (nNodes - j) < 0))
                            {
                              neighbour ::= i + j
                              neighbour ::= i - j
                            }
                            if (i == j) 
                            {
                              neighbour ::= i - j 
                              neighbour ::= i + j 
                            }
                        }
                    }
               /*  for(k<-0 until neighbour.length)
               {
                println("Neighbour is " + neighbour(k) + " actor no " + i)
               }*/

                    if (topology.equalsIgnoreCase("Imp2D"))
                    {
                        var random = Random.nextInt(nNodes)
                        neighbour ::= random
                    }
                    arActors(i) ! Initialize(arActors,counter,neighbour,i+1)
                    i+=1
                }
            
                if (gossip.equalsIgnoreCase(algorithm))
                    master ! Gossip(arActors)
                else if(pushSum.equalsIgnoreCase(algorithm))
                    master ! pushsum(arActors)
             }
          }
        }
