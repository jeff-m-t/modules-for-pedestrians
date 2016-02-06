package pedestrian.modules.access

import scala.concurrent.{ ExecutionContext, Future }

trait AccessControlSupport {
  import AccessControlSupport._
  
  def accessControl: AccessControlOps
  
  trait AccessControlOps {
    def read(userId: String, itemId: String)(implicit ec: ExecutionContext): Future[AccessControl]
    def write(userId: String, itemId: String)(implicit ec: ExecutionContext): Future[AccessControl]
  }  
}

object AccessControlSupport {

  case class AccessException(msg: String) extends Exception(msg)
  
  sealed trait AccessControl {
    def enforce(accessingUserId: String): Future[Unit]
  }
  case object Public extends AccessControl {
    def enforce(accessingUserId: String) = Future.successful(())
  }
  case class Private(ownerId: String) extends AccessControl {
    def enforce(accessingUserId: String) = 
      if(accessingUserId == ownerId) Future.successful(())
      else Future.failed(AccessException(s"The requested item is not public"))
  }
}

