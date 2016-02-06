package pedestrian.modules.kv.mongodb

import scala.concurrent.{ ExecutionContext, Future, Promise }

import org.bson.conversions.Bson
import org.json4s._
import org.mongodb.scala.{MongoClient,Observer}
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.Projections._
import org.mongodb.scala.model.Updates._

import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.UpdateResult

import pedestrian.modules.kv.KVStoreSupport

trait MongoDbKVStoreSupport extends KVStoreSupport {

  val client = MongoClient("mongodb://192.168.99.100")
  val database = client.getDatabase("foo")
  val collection = database.getCollection("kvstore")
  
  def kvGet(userId: String, key: String)(implicit ec: ExecutionContext): Future[Option[JValue]] = {
    val p = Promise[Option[JValue]]()
    
    val query = equal("userId",userId)
    val proj = fields(excludeId(),include(key))
    
    collection.find(query).projection(proj).first.subscribe(new Observer[Document] {
      override def onNext(result: Document): Unit = p.success(result.get(key).map(JValueBsonMarshalling.bsonToJValue))   
      override def onError(e: Throwable): Unit = p.failure(e)
      override def onComplete(): Unit = if(! p.isCompleted) p.success(None)
    })
    
    p.future
  }
  
  def kvPut(userId: String, key: String, value: JValue)(implicit ec: ExecutionContext): Future[Unit] = {
    val p = Promise[Unit]()
    
    val filter: Bson = Document("userId"->userId)
    val update: Bson = set(key, JValueBsonMarshalling.jValueToBson(value))
    val options = new UpdateOptions(); options.upsert(true);
    
    collection.updateOne(filter,update,options).subscribe(new Observer[UpdateResult] {
      override def onNext(result: UpdateResult): Unit = p.success(())    
      override def onError(e: Throwable): Unit = p.failure(e)
      override def onComplete(): Unit = if(! p.isCompleted) p.success(())
    })
    
    p.future
  }
  
  def kvDelete(userId: String, key: String)(implicit ec: ExecutionContext): Future[Unit] = {
    val p = Promise[Unit]()

    val filter = equal("userId",userId)
    val update = unset(key)
    
    collection.updateOne(filter,update).subscribe(new Observer[UpdateResult] {
      override def onNext(result: UpdateResult): Unit = p.success(())    
      override def onError(e: Throwable): Unit = p.failure(e)
      override def onComplete(): Unit = if(! p.isCompleted) p.success(())
    })
   
    p.future
  }
 
}