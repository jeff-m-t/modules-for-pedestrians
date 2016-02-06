package pedestrian.modules.service

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.unmarshalling._

import org.json4s._
import org.json4s.jackson.JsonMethods._

import akka.http.scaladsl.model._

trait Json4sMarshalling {
  
  implicit val jValueMarshaller: ToResponseMarshaller[JValue] =
    Marshaller.withFixedContentType(ContentTypes.`application/json`){((json: JValue) => {
      HttpResponse(entity = HttpEntity(ContentTypes.`application/json`,pretty(json)))
    })
  }

  implicit val jValueUnmarshaller: FromEntityUnmarshaller[JValue] = 
    Unmarshaller.byteStringUnmarshaller.forContentTypes(ContentTypes.`application/json`).mapWithCharset { (data, charset) ⇒
      val input: String = if (charset == HttpCharsets.`UTF-8`) data.utf8String else data.decodeString(charset.nioCharset.name)
      parse(input)
  }
  
}

object Json4sMarshalling extends Json4sMarshalling