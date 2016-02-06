package pedestrian.core

import scala.concurrent.{ ExecutionContext, Future }

trait Lifecycle {
  def startup(implicit ec: ExecutionContext): Future[Unit]
  def shutdown(implicit ec: ExecutionContext): Future[Unit]
}

class BlankSlate extends Lifecycle {
  def startup(implicit ec: ExecutionContext) = Future.successful(())
  def shutdown(implicit ec: ExecutionContext) = Future.successful(())
}