package pedestrian.modules.kv

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.stm.{ TMap, _ }

import org.json4s._

import pedestrian.core.Lifecycle

import scalaz.Reader

trait InMemoryKVStoreSupport extends KVStoreSupport {
  type KeyValueStoreEnv = TMap[String,Map[String,JValue]]
  
  def kvPut(userId: String, key: String, value: JValue)(implicit ec: ExecutionContext): Reader[KeyValueStoreEnv,Future[Unit]] = Reader( env =>
    Future {
      atomic { implicit txn =>
        val userData = env.get(userId).getOrElse(Map.empty)
        env.put(userId, userData + (key -> value))
      }
    }
  )

  def kvDelete(userId: String, key: String)(implicit ec: ExecutionContext): Reader[KeyValueStoreEnv,Future[Unit]] = Reader( env =>
    Future {
      atomic { implicit txn =>
        val userData = env.get(userId).map(_ - key).getOrElse(Map.empty)
        env.put(userId, userData)
      }
    }
  )

  def kvGet(userId: String, key: String)(implicit ec: ExecutionContext): Reader[KeyValueStoreEnv,Future[Option[JValue]]] = Reader( env =>
    Future {
      for {
        userData <- env.snapshot.get(userId)
        value <- userData.get(key)
      } yield value
    }
  )
  
}