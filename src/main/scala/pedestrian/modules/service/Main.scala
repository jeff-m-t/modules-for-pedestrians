package pedestrian.modules.service

import scala.concurrent.Await
import scala.concurrent.duration._

import org.json4s._

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.ActorMaterializer

import pedestrian.modules.KeyValueStoreApp
import pedestrian.modules.access.{ AccessControlSupport, FixtureDataAccessControlSupport }
import pedestrian.modules.kv.InMemoryKVStoreSupport
import pedestrian.modules.messaging.InMemoryMessageProductionSupport

object Main extends App with Json4sMarshalling {
    
  implicit val system = ActorSystem("KeyValueService")
  implicit val ec = system.dispatcher
  implicit val materializer = ActorMaterializer()
  
  val app = new KeyValueStoreApp 
    with InMemoryKVStoreSupport 
    with InMemoryMessageProductionSupport 
    with FixtureDataAccessControlSupport
  {
    override val publicItemIds = Set("foo") 
  }
  Await.result(app.startup,5.seconds)
  
  val exceptionHandler = ExceptionHandler {
    case _: AccessControlSupport.AccessException => complete(StatusCodes.Forbidden)
  }
  
  val routes = handleExceptions(exceptionHandler) {
    pathPrefix("v1" / "kv") {
      path("users" / Segment / "items" / Segment) { (userId,itemId) =>
        headerValueByName("x-requesting-user-id") { requestingUser =>
          get {
            complete {
              app.getItem(userId, itemId)(requestingUser)
            }
          } ~
          put {
            entity(as[JValue]) { value =>
              complete(app.putItem(userId, itemId, value)(requestingUser).map(_ => StatusCodes.Accepted))
            }
          } ~
          delete {
            complete(app.removeItem(userId, itemId)(requestingUser).map(_ => StatusCodes.OK))
          }
        }
      }
    }
  }
  
  Http().bindAndHandle(routes,"0.0.0.0",8080)
}