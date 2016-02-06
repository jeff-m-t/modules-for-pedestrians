

package pedestrian.modules.service

import pedestrian.modules.KeyValueStoreApp
import pedestrian.modules.kv.InMemoryKVStoreSupport

object Main extends App {
  
  val app = new KeyValueStoreApp with InMemoryKVStoreSupport {
    
  }
  
  // TODO: Add service bootstrap here
}