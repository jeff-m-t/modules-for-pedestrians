package pedestrian.modules.kv

import scala.concurrent.{Await,Future}
import scala.concurrent.duration._
import scala.concurrent.stm._

import org.json4s._
import org.json4s.JsonDSL._

import org.scalatest.FlatSpec
import org.scalatest.ShouldMatchers

class InMemoryKVStoreSupportTest extends FlatSpec with ShouldMatchers {
  import scala.concurrent.ExecutionContext.Implicits.global

  def createModule: KVStoreSupport = new InMemoryKVStoreSupport() {}

  it should "allow values to be stored and retrieved" in {
    val store = createModule

    val userId = java.util.UUID.randomUUID.toString
    val key = "foo"
    val value: JValue = ("flooze" -> "baz")

    waitFor(store.kvGet(userId, key)) should be (None)

    waitFor(store.kvPut(userId, key, value))

    waitFor(store.kvGet(userId, key)) should be (Some(value))
  }

  it should "support the removal of elements" in {
    val store = createModule

    val userId = java.util.UUID.randomUUID.toString
    val key = "bar"
    val value: JValue = ("flooze" -> "baz")

    waitFor(store.kvPut(userId, key, value))

    waitFor(store.kvGet(userId, key)) should be (Some(value))

    waitFor(store.kvDelete(userId, key))

    waitFor(store.kvGet(userId, key)) should be (None)
  }

  def waitFor[T](f: Future[T], d: Duration = 1.second): T = Await.result(f,d) 
}
