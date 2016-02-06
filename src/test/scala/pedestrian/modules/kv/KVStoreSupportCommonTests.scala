package pedestrian.modules.kv

import scala.concurrent.{Future,ExecutionContext,Await}
import scala.concurrent.duration._

import org.json4s._
import org.json4s.JsonDSL._

import org.scalatest.FlatSpec
import org.scalatest.ShouldMatchers

abstract class KVStoreSupportCommonTests extends FlatSpec with ShouldMatchers {
  
  def createModule: KVStoreSupport
  implicit val ex = ExecutionContext.Implicits.global

  it should "allow values to be stored and retrieved" in {
    val store = createModule
    waitFor(store.startup)
    
    val userId = java.util.UUID.randomUUID.toString
    val key = "foo"
    val value: JValue = ("flooze" -> "baz")
    
    waitFor(store.kvGet(userId, key)) should be (None)
    
    waitFor(store.kvPut(userId, key, value))
    
    waitFor(store.kvGet(userId, key)) should be (Some(value))
    
    waitFor(store.shutdown)
  }
  
  it should "support the removal of elements" in {
    val store = createModule
    waitFor(store.startup)
    
    val userId = java.util.UUID.randomUUID.toString
    val key = "bar"
    val value: JValue = ("flooze" -> "baz")

    waitFor(store.kvPut(userId, key, value))
        
    waitFor(store.kvGet(userId, key)) should be (Some(value))
    
    waitFor(store.kvDelete(userId, key))
    
    waitFor(store.kvGet(userId, key)) should be (None)
    
    waitFor(store.shutdown)
  }
  
  def waitFor[T](f: Future[T], d: Duration = 1.second): T = Await.result(f,d) 

}