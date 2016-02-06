package pedestrian.modules.kv

import pedestrian.core.BlankSlate

class InMemoryKVStoreSupportTest extends KVStoreSupportCommonTests {
  def createModule = new BlankSlate with InMemoryKVStoreSupport
}