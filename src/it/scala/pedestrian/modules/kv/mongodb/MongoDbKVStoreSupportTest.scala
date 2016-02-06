package pedestrian.modules.kv.mongodb

import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

import org.json4s._
import org.json4s.JsonDSL._

import org.scalatest.{ FlatSpec, ShouldMatchers }

import pedestrian.modules.kv.KVStoreSupport

class MongoDbKVStoreSupportTest extends FlatSpec with ShouldMatchers {
  import scala.concurrent.ExecutionContext.Implicits.global

  def createModule: KVStoreSupport = new MongoDbKVStoreSupport() {}

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
  
  def waitFor[T](f: Future[T], d: Duration = 10.second): T = Await.result(f,d) 
}