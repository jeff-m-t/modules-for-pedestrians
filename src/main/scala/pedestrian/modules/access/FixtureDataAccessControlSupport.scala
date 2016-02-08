package pedestrian.modules.access

import scala.concurrent.{ ExecutionContext, Future }

import scalaz._
import scalaz.Scalaz._

trait FixtureDataAccessControlSupport extends AccessControlSupport {
  import AccessControlSupport._
  
  type AccessControlEnv = Set[String]
  
  object accessControl extends AccessControlOps {
    def read(userId: String, itemId: String)(implicit ec: ExecutionContext): Reader[AccessControlEnv,Future[AccessControl]] = Reader { env =>
      if(env.contains(itemId)) Future.successful(Public)
      else Future.successful(Private(userId))
    }
    
    def write(userId: String, itemId: String)(implicit ec: ExecutionContext): Reader[AccessControlEnv,Future[AccessControl]] = Reader { env =>
      Future.successful(Private(userId))
    }    
  }
}