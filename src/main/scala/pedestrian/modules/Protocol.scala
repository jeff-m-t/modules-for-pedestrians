package pedestrian.modules

import org.json4s._

import pedestrian.core.Marshaller

object Protocol {
  
  sealed trait KVMessage {
    val userId: String
    val itemId: String
  }
  case class ItemUpdated(userId: String, itemId: String, newValue: JValue) extends KVMessage
  case class ItemDeleted(userId: String, itemId: String) extends KVMessage

  implicit val byteArrayMarshaller = new Marshaller[KVMessage,Array[Byte]] {
    def marshal(message: KVMessage): Array[Byte] = message.toString.getBytes("UTF-8")
  }
}