package pedestrian.modules

import scala.concurrent.{ ExecutionContext, Future, Promise }

import org.json4s._

import pedestrian.core.Lifecycle
import pedestrian.modules.access.AccessControlSupport
import pedestrian.modules.kv.KVStoreSupport
import pedestrian.modules.messaging.{ InMemoryProducerConfig, MessageProductionSupport, Producer }

abstract class KeyValueStoreApp 
  extends Lifecycle 
  with KVStoreSupport
  with MessageProductionSupport 
  with AccessControlSupport
{

  val producer = Promise[Producer[Protocol.KVMessage]]()
  
  def putItem(userId: String, itemId: String, value: JValue)(requesterId: String)(implicit ex: ExecutionContext): Future[Unit] = 
    for {
      ac <- accessControl.write(userId,itemId)
      _ <- ac.enforce(requesterId)
      _ <- kvPut(userId,itemId,value)
      p <- producer.future
      _ <- p.sendMessage(Protocol.ItemUpdated(userId,itemId,value))
    }
    yield ()
  
  def getItem(userId: String, itemId: String)(requesterId: String)(implicit ex: ExecutionContext): Future[Option[JValue]] = 
    for {
      ac <- accessControl.read(userId,itemId)
      _ <- ac.enforce(requesterId)
      value <- kvGet(userId,itemId)
    }
    yield value
      
  def removeItem(userId: String, itemId: String)(requesterId: String)(implicit ex: ExecutionContext): Future[Unit] = 
    for {
      ac <- accessControl.write(userId, itemId)
      _ <- ac.enforce(requesterId)
      _ <- kvDelete(userId,itemId)
      p <- producer.future
      _ <- p.sendMessage(Protocol.ItemDeleted(userId,itemId))
    }
    yield ()

      
  override def startup(implicit ec: ExecutionContext) = {
    import Protocol.byteArrayMarshaller
    val res = messageProduction.getProducer[Protocol.KVMessage](InMemoryProducerConfig("kvmessages"))    

    res.onSuccess{ case p => producer.success(p) }

    res.map(_ => ())
  }
  
  override def shutdown(implicit ec: ExecutionContext) = 
    for {
      p <- producer.future
    }
    yield p.disconnect
    
  
}
