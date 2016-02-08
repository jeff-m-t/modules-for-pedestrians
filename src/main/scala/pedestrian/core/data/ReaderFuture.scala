package pedestrian.core.data

import scala.concurrent.{ Future, ExecutionContext }

import scalaz._
import scalaz.Scalaz._

case class ReaderFuture[ENV,A](run: Reader[ENV,Future[A]]) {
  
  def map[B](f: A => B)(implicit ec: ExecutionContext): ReaderFuture[ENV,B] = 
    ReaderFuture( run.map( futa => futa.map(f) ) )
  
  def flatMap[B](f: A => ReaderFuture[ENV,B])(implicit ec: ExecutionContext): ReaderFuture[ENV,B] =
    ReaderFuture( Reader { env =>
      run.run(env).flatMap { a => f(a).run.run(env) }
    })
    
  def local[ENV2](f: ENV2 => ENV): ReaderFuture[ENV2,A] = ReaderFuture(run.local(f))
  
}

object ReaderFuture {
  def readerFuture[ENV,A](f: ENV => Future[A]): ReaderFuture[ENV,A] = ReaderFuture( Reader(f) )
}