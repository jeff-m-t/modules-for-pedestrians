package pedestrian.modules

import scala.concurrent.{ ExecutionContext, Future }

import org.json4s.JValue

import pedestrian.modules.kv.KVStoreSupport

abstract class KeyValueStoreApp extends KVStoreSupport {

  def putItem(userId: String, itemId: String, value: JValue)(implicit ex: ExecutionContext): Future[Unit]
      = kvPut(userId,itemId,value)
      
  def getItem(userId: String, itemId: String)(implicit ex: ExecutionContext): Future[Option[JValue]] 
      = kvGet(userId,itemId)
      
  def removeItem(userId: String, itemId: String)(implicit ex: ExecutionContext): Future[Unit]
      = kvDelete(userId,itemId)
      
}
