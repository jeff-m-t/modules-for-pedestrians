package pedestrian.modules.messaging

import scala.concurrent.{ExecutionContext,Future}
import scala.concurrent.stm._

import pedestrian.core.{BlankSlate,Lifecycle,Marshaller,Unmarshaller}

import scalaz.concurrent.Task
import scalaz.stream._
import scalaz.stream.async.mutable.{Queue => szQueue}

case class InMemoryProducerConfig(destination: String) extends ProducerConfig {
  val destinationType = Queue
}

trait InMemoryMessageProductionSupport extends MessageProductionSupport with ScalazStreamMessagingProvider[Array[Byte]] {
  val producedMessages = TMap.empty[String, List[ProviderPublishedMessageType]]
 
  type ProducerConfigType = InMemoryProducerConfig
  type ProviderPublishedMessageType = Array[Byte]
 
  object messageProduction extends ProductionOps {
  	def getProducer[PublishedMessageType](config: InMemoryProducerConfig)(implicit ec: ExecutionContext, marshaller: Marshaller[PublishedMessageType,Array[Byte]]): Future[Producer[PublishedMessageType]] = Future {
    	new InMemoryPrododucer(config.destination, marshaller)
  	}
  }
 
  class InMemoryPrododucer[PublishedMessageType](val destination: String, val marshaller: Marshaller[PublishedMessageType,Array[Byte]]) extends Producer[PublishedMessageType] {
  	def sendMessage(message: PublishedMessageType)(implicit ec: ExecutionContext) = Future {
    	val mm = marshaller.marshal(message)
    	atomic { implicit txn =>   	 
      	val previousMessages = producedMessages.get(destination).getOrElse(List.empty)
      	producedMessages.update(destination, mm :: previousMessages)
      	getOrCreateQueue(destination).enqueueOne(mm).run
    	}
  	}
  
  	def sendMessages(messages: Seq[PublishedMessageType])(implicit ec: ExecutionContext): Future[Unit] = Future {
    	val marshalledMessages = messages.map(m => marshaller.marshal(m)).toList
    	atomic { implicit txn =>
      	val updated = marshalledMessages ++ producedMessages.get(destination).getOrElse(List.empty[ProviderPublishedMessageType])
      	producedMessages.put(destination,updated)
      	getOrCreateQueue(destination).enqueueAll(marshalledMessages).run
    	}
  	}
  
  	def disconnect(implicit ec: ExecutionContext): Future[Unit] = Future.successful(Unit)
  }
}

case class InMemoryConsumerConfig(destination: String, clientId: String) extends ConsumerConfig {
  val destinationType = Queue
}

trait InMemoryMessageConsumptionSupport extends MessageConsumptionSupport with ScalazStreamMessagingProvider[Array[Byte]] with Lifecycle {
  self =>
    
  lazy val subscriptions = TMap.empty[String, List[InMemoryConsumer[_,_]]]

  type ConsumerConfigType = InMemoryConsumerConfig
  type ProviderMessageType = Array[Byte]
  
  // For testing
  def enqueue[T](destination: String, message: T)(implicit ec: ExecutionContext, m: Marshaller[T,Array[Byte]]): Future[Unit] = Future { 	 
	  getOrCreateQueue(destination).enqueueOne(m.marshal(message)).run
  }

  abstract override def shutdown(implicit ec: ExecutionContext): Future[Unit] = {
  	// TODO: Shut down all active subscriptions
  	atomic { implicit txn =>
         	 
  	}    
  	super.shutdown
  }
 
  object messageConsumption extends ConsumptionOps {
  	def getConsumer[T, ResultType]
  	      (config: InMemoryConsumerConfig)
  	      (handler: T => Future[ResultType])
          (implicit ec: ExecutionContext, unmarshaller: Unmarshaller[Array[Byte],T]): Future[Consumer] =
    	Future {
      	atomic { implicit txn =>
        	val queue = getOrCreateQueue(config.destination)
        	val consumer = InMemoryConsumer[T,ResultType](config.destination,config.clientId,queue,handler,unmarshaller)
       	 
        	val existingSubscriptions = subscriptions.get(config.destination).getOrElse(List.empty)
        	subscriptions.update(config.destination, consumer :: existingSubscriptions)
       	 
        	consumer
      	}
    	}    
  }

  case class InMemoryConsumer[M,R](destination:String, clientId: String, queue: szQueue[Array[Byte]], handler: M => Future[R], unmarshaller: Unmarshaller[Array[Byte],M])(implicit ec: ExecutionContext) extends Consumer {
  	import scala.language.postfixOps
  
  	val dataSource = queue.dequeue.map(unmarshaller.unmarshal)
  
  	def process(m: M) = Task {
    	handler(m).onComplete(res => println(s"Processed: $res"))
  	}
  	val sink = (Process constant (process _)).toSource
   
  	val stopper = async.signalOf(false)
      
  	val source = (stopper.discrete).wye(dataSource)(wye.interrupt)
      
  	val foo = (source to sink).run.runAsync(s => println(s"Disconnecting $destination:$clientId"))  
      
  	override def unsubscribe(implicit ec: ExecutionContext): Future[Unit] = Future {
    	atomic { implicit txn =>
      	val existingSubscriptions = self.subscriptions.get(destination).getOrElse(List.empty)
      	self.subscriptions.update(destination, existingSubscriptions.filterNot(_ == this))
    	}
    	stopper.set(true).run
  	}
  }
}

trait ScalazStreamMessagingProvider[T] {
  val queues = TMap.empty[String, szQueue[T]]

  def getOrCreateQueue(destination: String): szQueue[T] =    	 
  	atomic { implicit txn =>
    	queues.get(destination).getOrElse {
      	val newQueue = async.boundedQueue[T](100)
      	queues.update(destination, newQueue)
      	newQueue
    	}
  	}  
}
