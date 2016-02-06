package pedestrian.modules.service

import scala.concurrent.Await
import scala.concurrent.duration._

import org.json4s._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import pedestrian.modules.KeyValueStoreApp
import pedestrian.modules.kv.InMemoryKVStoreSupport

object Main extends App with Json4sMarshalling {
    
  implicit val system = ActorSystem("KeyValueService")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  
  val app = new KeyValueStoreApp with InMemoryKVStoreSupport {
    
  }
  Await.result(app.startup,5.seconds)
  
  val routes = pathPrefix("v1" / "kv") {
    path("users" / Segment / "items" / Segment) { (userId,itemId) =>
      get {
        complete(app.getItem(userId, itemId))
      } ~
      put {
        entity(as[JValue]) { value =>
          complete(app.putItem(userId, itemId, value).map(_ => StatusCodes.Accepted))
        }
      } ~
      delete {
        complete(app.removeItem(userId, itemId).map(_ => StatusCodes.OK))
      }
    }
  }
  
  Http().bindAndHandle(routes,"0.0.0.0",8080)
}