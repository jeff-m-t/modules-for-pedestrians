package pedestrian.modules.access

import scala.concurrent.{ ExecutionContext, Future }

import AccessControlSupport.Private

trait FixtureDataAccessControlSupport extends AccessControlSupport {
  import AccessControlSupport._
  
  val publicItemIds: Set[String]
  
  object accessControl extends AccessControlOps {
    def read(userId: String, itemId: String)(implicit ec: ExecutionContext): Future[AccessControl] = {
      if(publicItemIds.contains(itemId)) Future.successful(Public)
      else Future.successful(Private(userId))
    }
    
    def write(userId: String, itemId: String)(implicit ec: ExecutionContext): Future[AccessControl] = {
      Future.successful(Private(userId))
    }    
  }
}