package pedestrian.modules.kv

import pedestrian.core.BlankSlate

import scala.concurrent.stm._

class InMemoryKVStoreSupportTest extends KVStoreSupportCommonTests {
  type ImplType = InMemoryKVStoreSupport
  
  def createModule = new BlankSlate with InMemoryKVStoreSupport
  def createEnvironment: ImplType#KeyValueStoreEnv = TMap.empty
}