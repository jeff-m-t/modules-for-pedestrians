package pedestrian.modules.messaging

import org.scalatest.FlatSpec
import org.scalatest.ShouldMatchers
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import pedestrian.core.BlankSlate
import pedestrian.core.Marshaller
import pedestrian.core.Unmarshaller
import org.scalatest.concurrent.Eventually

class InMemoryMessagingSupportTest extends FlatSpec with ShouldMatchers with Eventually {
  import ExecutionContext.Implicits.global
  
  "MessageConbsumptionSupport" should "provide consumers that process messages until disconnected" in {
    val module = new BlankSlate with InMemoryMessageConsumptionSupport
    
    waitFor(module.startup)
    
    implicit val marshaller = new Marshaller[String, Array[Byte]] {
  	  def marshal(message: String) = message.getBytes("UTF-8")
    }
    implicit val unmarshaller = new Unmarshaller[Array[Byte],String] {
  	  def unmarshal(message: Array[Byte]) = new String(message,"UTF-8")
    }
  
    module.enqueue("foo", "Message 0")
    module.enqueue("foo", "Message 1")
    module.enqueue("foo", "Message 2")
    module.enqueue("foo", "Message 3")
  
    val config = InMemoryConsumerConfig("foo","foo-consumer")
    
    val consumedMessages = scala.collection.mutable.ListBuffer.empty[String]
    val consumer = waitFor(
        module.messageConsumption.getConsumer(config){ (s: String) => 
          Future{ 
            val res = s"Processing: $s"; 
            println(res);
            consumedMessages += res
             res 
          }
        }
    )
  
    println("Processing...")
    
    eventually {
      consumedMessages.size should be (4)
    }
    
    module.enqueue("foo", marshaller.marshal("Message 4"))
   
    eventually {
      consumedMessages.size should be (5)
    }
  
    waitFor(consumer.unsubscribe)
      
    module.enqueue("foo", marshaller.marshal("Message 5"))

    Thread.sleep(1000)
    
    waitFor(module.shutdown)
    
    consumedMessages.size should be (5)
  } 
  
  def waitFor[T](f: Future[T], d: Duration = 10.seconds): T = Await.result(f,d)
}