package pedestrian.core

trait Marshaller[-INT,+EXT] {
  def marshal(internal: INT): EXT
}
object Marshaller {
  implicit def identityUnmarshaller[T] = new Marshaller[T,T]() {
    def marshal(t: T) = t
  }
}

trait Unmarshaller[-EXT,+INT] {
  def unmarshal(external: EXT): INT
}
object Unmarshaller {
  implicit def identityUnmarshaller[T] = new Unmarshaller[T,T] () {
    def unmarshal(t: T) = t
  }
}
