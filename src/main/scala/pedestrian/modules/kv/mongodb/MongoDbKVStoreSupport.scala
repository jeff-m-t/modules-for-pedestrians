package pedestrian.modules.kv.mongodb

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.util.control.NonFatal

import org.json4s._

import org.mongodb.scala.{ MongoClient, MongoCollection, Observer }
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Updates._

import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult

import pedestrian.core.Lifecycle
import pedestrian.modules.kv.KVStoreSupport

trait MongoDbKVStoreSupport extends KVStoreSupport with Lifecycle {
  
  import MongoDbKVStoreSupport._
  
  val collection: MongoCollection[Document]
  
  def kvGet(userId: String, key: String)(implicit ec: ExecutionContext): Future[Option[JValue]] = {
    
    val query = equal("userId",userId)
    val proj = fields(excludeId(),include(key))
    
    for {
      res <- {
        val p = Promise[Option[JValue]]()
        collection.find(query).projection(proj).first.subscribe(getObs(key,p))    
        p.future
      }
    } yield res
  }
  
  def kvPut(userId: String, key: String, value: JValue)(implicit ec: ExecutionContext): Future[Unit] = {
    
    val filter = Document("userId"->userId)
    val update = set(key, JValueBsonMarshalling.jValueToBson(value))
    val options = new UpdateOptions(); options.upsert(true);
    
    for {
      res <- {
        val p = Promise[Unit]()
        collection.updateOne(filter,update,options).subscribe(updateObs(p))    
        p.future
      }
    } yield res
  }
  
  def kvDelete(userId: String, key: String)(implicit ec: ExecutionContext): Future[Unit] = {

    val filter = equal("userId",userId)
    val update = unset(key)
    
    for {
      res <- {
        val p = Promise[Unit]()
        collection.updateOne(filter,update).subscribe(updateObs(p))   
        p.future
      }
    } yield res 
  }  
}

object MongoDbKVStoreSupport {
  def getObs(key: String, p: Promise[Option[JValue]]) = new Observer[Document] {
    override def onNext(result: Document): Unit = p.success(result.get(key).map(JValueBsonMarshalling.bsonToJValue))   
    override def onError(e: Throwable): Unit = p.failure(e)
    override def onComplete(): Unit = if(! p.isCompleted) p.success(None)
  }
  
  def updateObs(p: Promise[Unit]) = new Observer[UpdateResult] {
    override def onNext(result: UpdateResult): Unit = p.success(())    
    override def onError(e: Throwable): Unit = p.failure(e)
    override def onComplete(): Unit = if(! p.isCompleted) p.success(())
  }
}