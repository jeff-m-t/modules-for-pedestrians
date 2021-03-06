package pedestrian.modules.kv

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.stm.{ TMap, _ }

import org.json4s._

import pedestrian.core.Lifecycle

trait InMemoryKVStoreSupport extends KVStoreSupport with Lifecycle{
  val store: TMap[String,Map[String,JValue]] = TMap.empty
  
  def kvPut(userId: String, key: String, value: JValue)(implicit ec: ExecutionContext): Future[Unit] = Future {
    atomic { implicit txn =>
      val userData = store.get(userId).getOrElse(Map.empty)
      store.put(userId, userData + (key -> value))
    }
  }

  def kvDelete(userId: String, key: String)(implicit ec: ExecutionContext): Future[Unit] = Future {
    atomic { implicit txn =>
      val userData = store.get(userId).map(_ - key).getOrElse(Map.empty)
      store.put(userId, userData)
    }
  }

  def kvGet(userId: String, key: String)(implicit ec: ExecutionContext): Future[Option[JValue]] = Future {
    for {
      userData <- store.snapshot.get(userId)
      value <- userData.get(key)
    } yield value
  }
  
  abstract override def startup(implicit ec: ExecutionContext) = super.startup
  abstract override def shutdown(implicit ec: ExecutionContext) = super.shutdown
}