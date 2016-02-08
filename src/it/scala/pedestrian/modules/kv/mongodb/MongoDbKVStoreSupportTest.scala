package pedestrian.modules.kv.mongodb

import pedestrian.core.BlankSlate
import pedestrian.modules.kv.KVStoreSupportCommonTests
import org.scalatest.BeforeAndAfterAll
import org.mongodb.scala.MongoClient

class MongoDbKVStoreSupportTest extends KVStoreSupportCommonTests with BeforeAndAfterAll {
  type ImplType = MongoDbKVStoreSupport
  
  val client = MongoClient("mongodb://192.168.99.100")
  val collection = client.getDatabase("test").getCollection("kvstore")
  
  val createEnvironment: ImplType#KeyValueStoreEnv = collection
  
  def createModule = new BlankSlate with MongoDbKVStoreSupport
  
  override def afterAll() = {
    client.close()
  }
}