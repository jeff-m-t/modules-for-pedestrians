package pedestrian.modules.kv

import scala.concurrent.{Future,ExecutionContext,Await}
import scala.concurrent.duration._

import org.json4s._
import org.json4s.JsonDSL._

import org.scalatest.FlatSpec
import org.scalatest.ShouldMatchers

abstract class KVStoreSupportCommonTests extends FlatSpec with ShouldMatchers {
  
  type ImplType <: KVStoreSupport
  
  def createModule: ImplType
  def createEnvironment: ImplType#KeyValueStoreEnv
  
  implicit val ex = ExecutionContext.Implicits.global

  it should "allow values to be stored and retrieved" in {
    val store = createModule
    val env = createEnvironment.asInstanceOf[store.KeyValueStoreEnv]
    
    val userId = java.util.UUID.randomUUID.toString
    val key = "foo"
    val value: JValue = ("flooze" -> "baz")
    
    waitFor(store.kvGet(userId, key).run(env)) should be (None)
    
    waitFor(store.kvPut(userId, key, value).run(env))
    
    waitFor(store.kvGet(userId, key).run(env)) should be (Some(value))
    
  }
  
  
  def waitFor[T](f: Future[T], d: Duration = 1.second): T = Await.result(f,d) 

}