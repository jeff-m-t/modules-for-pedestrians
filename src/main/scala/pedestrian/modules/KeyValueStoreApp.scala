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
    val res: ReaderFuture[Config,Unit] = for {
      ac <- readerFuture(accessControl.write(userId,itemId)).local((c:Config) => c.acc)
      _ <- readerFuture((c: Config) => ac.enforce(requesterId))
      _ <- readerFuture(kvPut(userId,itemId,value)).local((c: Config) => c.kv)
      p <- readerFuture((c: Config) => producer.future)
      _ <- readerFuture((c: Config) => p.sendMessage(Protocol.ItemUpdated(userId,itemId,value)))
    }
    yield ()
    
    res.run.run(env)
  }
  
  def getItem(userId: String, itemId: String)(requesterId: String)(implicit ex: ExecutionContext): Future[Option[JValue]] = {
    val res: ReaderFuture[Config,Option[JValue]] = for {
      ac <- readerFuture(accessControl.read(userId,itemId)).local((c:Config) => c.acc)
      _ <- readerFuture((c: Config) => ac.enforce(requesterId))
      value <- readerFuture(kvGet(userId,itemId)).local((c:Config) => c.kv)
    }
    yield value
    
    res.run.run(env)
  }
      
  def removeItem(userId: String, itemId: String)(requesterId: String)(implicit ex: ExecutionContext): Future[Unit] = {
    val res: ReaderFuture[Config,Unit] = for {
      ac <- readerFuture(accessControl.write(userId,itemId)).local((c:Config) => c.acc)
      _ <- readerFuture((c: Config) => ac.enforce(requesterId))
      _ <- readerFuture(kvDelete(userId,itemId)).local((c: Config) => c.kv)
      p <- readerFuture((c: Config) => producer.future)
      _ <- readerFuture((c: Config) => p.sendMessage(Protocol.ItemDeleted(userId,itemId)))
    }
    yield ()
    
    res.run.run(env)
  }
      
  override def startup(implicit ec: ExecutionContext) = {
    val res = messageProduction.getProducer[Protocol.KVMessage](InMemoryProducerConfig("kvmessages")).local((c: Config) => c.msg).run(env)  

    res.onSuccess{ case p => producer.success(p) }

    res.map(_ => ())
  }
  
  override def shutdown(implicit ec: ExecutionContext) = 
    for {
      p <- producer.future
    }
    yield p.disconnect
     
}
