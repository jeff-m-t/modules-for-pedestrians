package pedestrian.modules.messaging

import scala.concurrent.{ ExecutionContext, Future }

import pedestrian.core.{ Lifecycle, Marshaller, Unmarshaller }

import scalaz.Reader

sealed trait DestinationType
case object Queue extends DestinationType
case object Topic extends DestinationType

trait ProducerConfig {
  val destination: String
  val destinationType: DestinationType
}

trait Producer[PublishedMessageType] {
  def sendMessage(message: PublishedMessageType)(implicit ec: ExecutionContext): Future[Unit]  
  def sendMessages(messages: Seq[PublishedMessageType])(implicit ec: ExecutionContext): Future[Unit]
  def disconnect(implicit ec: ExecutionContext): Future[Unit]
}

trait MessageProductionSupport {
  type MessageProductionEnv
  type ProducerConfigType <: ProducerConfig
  type ProviderPublishedMessageType
  
  def messageProduction: ProductionOps

  trait ProductionOps {
    def getProducer[PublishedMessageType]
          (config: InMemoryProducerConfig)
          (implicit ec: ExecutionContext, marshaller: Marshaller[PublishedMessageType,ProviderPublishedMessageType]): Reader[MessageProductionEnv,Future[Producer[PublishedMessageType]]]
  }
}

trait ConsumerConfig {
  val destination: String
  val destinationType: DestinationType
  val clientId: String
}

trait Consumer {
  def unsubscribe(implicit ec: ExecutionContext): Future[Unit]  
}

trait MessageConsumptionSupport {  
  type MessageConsumptionEnv
  type ConsumerConfigType <: ConsumerConfig
  type ProviderMessageType
  
  def messageConsumption: ConsumptionOps
  
  trait ConsumptionOps {
  	def getConsumer[MessageType, ResultType]
  	      (config: InMemoryConsumerConfig)
  	      (handler: MessageType => Future[ResultType])
          (implicit ec: ExecutionContext, unmarshaller: Unmarshaller[ProviderMessageType,MessageType]): Reader[MessageConsumptionEnv,Future[Consumer]]
  }
}

trait MessagingSupport extends MessageProductionSupport with MessageConsumptionSupport {
  object messaging {
    def getProducer[PublishedMessageType]
          (config: InMemoryProducerConfig)
          (implicit ec: ExecutionContext, marshaller: Marshaller[PublishedMessageType,ProviderPublishedMessageType]): Reader[MessageProductionEnv,Future[Producer[PublishedMessageType]]]
      = Reader(env => messageProduction.getProducer(config)(ec,marshaller).run(env))

  	def getConsumer[MessageType, ResultType]
  	      (config: InMemoryConsumerConfig)
  	      (handler: MessageType => Future[ResultType])
          (implicit ec: ExecutionContext, unmarshaller: Unmarshaller[ProviderMessageType,MessageType]): Reader[MessageConsumptionEnv,Future[Consumer]]
      = Reader(env => messageConsumption.getConsumer(config)(handler)(ec,unmarshaller).run(env)) 
  }
}

