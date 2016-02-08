package pedestrian.modules

import scala.concurrent.{ ExecutionContext, Future, Promise }
import org.json4s._
import pedestrian.core.Lifecycle
import pedestrian.modules.access.AccessControlSupport
import pedestrian.modules.kv.KVStoreSupport
import pedestrian.modules.messaging.{ InMemoryProducerConfig, MessageProductionSupport, Producer }
import pedestrian.core.Marshaller

import scalaz.Reader
import pedestrian.core.data.ReaderFuture
import pedestrian.core.data.ReaderFuture.readerFuture

abstract class KeyValueStoreApp 
  extends Lifecycle 
  with KVStoreSupport
  with MessageProductionSupport 
  with AccessControlSupport
{
  case class Config(kv: KeyValueStoreEnv, msg: MessageProductionEnv, acc: AccessControlEnv)
  val env: Config

  implicit val messageMarshaller: Marshaller[Protocol.KVMessage, ProviderPublishedMessageType]
  
  val producer = Promise[Producer[Protocol.KVMessage]]()
  
  def putItem(userId: String, itemId: String, value: JValue)(requesterId: String)(implicit ex: ExecutionContext): Future[Unit] = {
    import ReaderFuture._
    val res: ReaderFuture[Config,Unit] = for {
      ac <- accessControl.write(userId,itemId).local((c:Config) => c.acc).readerFuture
      _  <- ac.enforce(requesterId).readerFuture
      _  <- kvPut(userId,itemId,value).local((c: Config) => c.kv).readerFuture
      p  <- producer.future.readerFuture
      _  <- p.sendMessage(Protocol.ItemUpdated(userId,itemId,value)).readerFuture
    }
    yield ()
    
    res.run.run(env)
  }
  
  def getItem(userId: String, itemId: String)(requesterId: String)(implicit ex: ExecutionContext): Future[Option[JValue]] = {
    import ReaderFuture._
    val res: ReaderFuture[Config,Option[JValue]] = for {
      ac    <- accessControl.read(userId,itemId).local((c:Config) => c.acc).readerFuture
      _     <- ac.enforce(requesterId).readerFuture
      value <- kvGet(userId,itemId).local((c:Config) => c.kv).readerFuture
    }
    yield value
    
    res.run.run(env)
  }
      
  def removeItem(userId: String, itemId: String)(requesterId: String)(implicit ex: ExecutionContext): Future[Unit] = {
    import ReaderFuture._
    val res: ReaderFuture[Config,Unit] = for {
      ac <- accessControl.write(userId,itemId).local((c:Config) => c.acc).readerFuture
      _  <- ac.enforce(requesterId).readerFuture
      _  <- kvDelete(userId,itemId).local((c: Config) => c.kv).readerFuture
      p  <- producer.future.readerFuture
      _  <- p.sendMessage(Protocol.ItemDeleted(userId,itemId)).readerFuture
    }
    yield ()
    
    res.run.run(env)
  }
      
  override def startup(implicit ec: ExecutionContext) = {
    val res = 
      messageProduction.getProducer[Protocol.KVMessage](InMemoryProducerConfig("kvmessages"))
        .local((c: Config) => c.msg).run(env)  

    res.onSuccess{ case p => producer.success(p) }

    res.map(_ => ())
  }
  
  override def shutdown(implicit ec: ExecutionContext) = 
    for {
      p <- producer.future
    }
    yield p.disconnect
     
}
