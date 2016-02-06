package pedestrian.modules.kv.mongodb

import org.json4s._
import org.bson.BsonValue

// TODO:  This is a simplistic, incomplete mapping between json4s AST and BSON
object JValueBsonMarshalling {
  def jValueToBson(json: JValue): BsonValue = {
    import org.mongodb.scala.bson._
    json match {
      case JObject(fields) => BsonDocument(fields.map{case(k,v) => (k,jValueToBson(v))})
      case JArray(elements) => BsonArray(elements.map(jValueToBson))
      case JString(s) => BsonString(s)
      case JBool(b) => BsonBoolean(b)
      case JDouble(d) => BsonDouble(d)
      case JLong(l) => BsonInt64(l)
      case JInt(i) => if(i > Int.MaxValue) BsonInt64(i.toLong) else BsonInt32(i.toInt)
      case JDecimal(d) => BsonDouble(d.toDouble)
      case JNull => BsonNull()
      case JNothing => BsonUndefined()
    }
  }  
  def bsonToJValue(bson: BsonValue): JValue = {
    import org.bson._
    import scala.collection.JavaConverters._
    
    bson match {
      case doc: BsonDocument => 
        val fields = doc.entrySet().asScala.map( e => e.getKey -> bsonToJValue(e.getValue))
        JObject( fields.toList.map{ case(k,v) => JField(k,v)} )
      case array: BsonArray => JArray(array.asScala.toList.map(bsonToJValue))
      case bs: BsonString => JString(bs.getValue)
      case bb: BsonBoolean => JBool(bb.getValue)
      case bd: BsonDouble => JDouble(bd.getValue)
      case bl: BsonInt64 => JLong(bl.getValue)
      case bi: BsonInt32 => JLong(bi.getValue)
      case bn: BsonNull => JNull
      case bu: BsonUndefined => JNothing
    }
  }
}