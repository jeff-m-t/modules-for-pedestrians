package pedestrian.modules.kv.mongodb

import pedestrian.core.BlankSlate
import pedestrian.modules.kv.KVStoreSupportCommonTests
import org.scalatest.BeforeAndAfterAll
import org.mongodb.scala.MongoClient

class MongoDbKVStoreSupportTest extends KVStoreSupportCommonTests with BeforeAndAfterAll {
  val client = MongoClient("mongodb://192.168.99.100")
  val col = client.getDatabase("test").getCollection("kvstore")

  def createModule = new BlankSlate with MongoDbKVStoreSupport {
    override val collection = col
  }
  
  override def afterAll() = {
    client.close()
  }
}