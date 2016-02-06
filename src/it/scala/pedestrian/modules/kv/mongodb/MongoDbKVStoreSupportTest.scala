package pedestrian.modules.kv.mongodb

import pedestrian.core.BlankSlate
import pedestrian.modules.kv.KVStoreSupportCommonTests

class MongoDbKVStoreSupportTest extends KVStoreSupportCommonTests {
  def createModule = new BlankSlate with MongoDbKVStoreSupport {
    override val mongoUrl = "mongodb://192.168.99.100"
    override val mongoDatabase = "foo"
    override val mongoCollection = "kvstore"
  }
}