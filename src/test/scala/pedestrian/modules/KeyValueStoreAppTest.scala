package pedestrian.modules

import scala.concurrent.{ Await, Future, ExecutionContext }
import scala.concurrent.duration.{ Duration, DurationInt }
import org.json4s._
import org.json4s.JsonDSL._
import org.scalatest.{ FlatSpec, ShouldMatchers }
import pedestrian.modules.access.FixtureDataAccessControlSupport
import pedestrian.modules.kv.InMemoryKVStoreSupport
import pedestrian.modules.messaging.InMemoryMessageProductionSupport
import pedestrian.modules.access.AccessControlSupport

class KeyValueStoreAppTest extends FlatSpec with ShouldMatchers {
  import ExecutionContext.Implicits.global
  
  val userId = "user1"
  val itemId = "item1"
  val otherUserId = "user2"
  val privateItemId = "item2"
  
  val json: JValue = ("foo" -> "bar") ~ ("baz" -> "qux")
  
  it should "allow access to other user's public items" in {
    val app = new KeyValueStoreApp 
      with InMemoryKVStoreSupport 
      with InMemoryMessageProductionSupport
      with FixtureDataAccessControlSupport 
    {
      override val publicItemIds = Set("item1")
    }
    waitFor(app.startup)
    
    waitFor(app.putItem(userId,itemId,json)(userId))
    
    waitFor(app.getItem(userId,itemId)(otherUserId)) should be (Some(json))

    waitFor(app.putItem(userId,privateItemId,json)(userId))

    intercept[AccessControlSupport.AccessException] {
      waitFor(app.getItem(userId,privateItemId)(otherUserId))
    }
  }
  
  it should "publish an ItemUpdated message when a user adds or updates an item" in {
    val app = new KeyValueStoreApp 
      with InMemoryKVStoreSupport 
      with InMemoryMessageProductionSupport
      with FixtureDataAccessControlSupport 
    {
      override val publicItemIds = Set.empty[String]
    }
    waitFor(app.startup)
    
    app.store.snapshot.size should be (0)
    app.producedMessages.snapshot.size should be (0)
    
    waitFor(app.putItem(userId,itemId, ("foo" -> "bar") ~ ("baz" -> "qux"))(userId))
    
    app.store.snapshot.size should be (1)
    app.producedMessages.snapshot("kvmessages").size should be (1)
    
    waitFor(app.putItem(userId,itemId, ("foo" -> "bar1") ~ ("baz" -> "qux1"))(userId))
    
    app.store.snapshot.size should be (1)
    app.producedMessages.snapshot("kvmessages").size should be (2)
    
    waitFor(app.shutdown)
  }
  
  def waitFor[T](f: Future[T], d: Duration = 2.seconds): T = Await.result(f,d)
}