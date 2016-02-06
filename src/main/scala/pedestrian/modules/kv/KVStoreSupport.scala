package pedestrian.modules.kv

import scala.concurrent.{ ExecutionContext, Future }

import org.json4s._

trait KVStoreSupport {
  def kvGet(userId: String, key: String)(implicit ec: ExecutionContext): Future[Option[JValue]]
  def kvPut(userId: String, key: String, value: JValue)(implicit ec: ExecutionContext): Future[Unit]
  def kvDelete(userId: String, key: String)(implicit ec: ExecutionContext): Future[Unit]
}