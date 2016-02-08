package pedestrian.modules.kv

import scala.concurrent.{ ExecutionContext, Future }

import org.json4s._

import pedestrian.core.Lifecycle

import scalaz.{Reader => szReader}


trait KVStoreSupport {
  type KeyValueStoreEnv
  
  def kvGet(userId: String, key: String)(implicit ec: ExecutionContext): szReader[KeyValueStoreEnv, Future[Option[JValue]]]
  def kvPut(userId: String, key: String, value: JValue)(implicit ec: ExecutionContext): szReader[KeyValueStoreEnv, Future[Unit]]
  def kvDelete(userId: String, key: String)(implicit ec: ExecutionContext): szReader[KeyValueStoreEnv,Future[Unit]]
}