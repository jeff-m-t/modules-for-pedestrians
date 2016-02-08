package pedestrian.modules.samples

import scalaz._
import scalaz.Scalaz._

object ReaderMonad extends App {
  
  type Env = String
  
  def greet(name: String): Reader[Env,String] = Reader(env => s"$env $name")
  def sizer(s: String): Reader[Env,Int] = Reader(env => s.length)
  
  val computation: Reader[Env,Int] = greet("Jack").flatMap(s => sizer(s))

  println(computation.run("Hello"))
  
  val computation2: Reader[Env,Int] = 
    for {
      g <- greet("Jack")
      l <- sizer(g)
    }
    yield l
    
  println(computation2.run("Hiya"))
    
  trait GreetingSupport {
    type GreetingEnv
    def greet(name: String, languageTag: String): Reader[GreetingEnv,String]    
  }
    
  trait InMemoryGreetingSupport extends GreetingSupport {
    type GreetingEnv = Map[String,String]
    def greet(name: String, languageTag: String) = Reader { env =>
      s"${env.get(languageTag).getOrElse("Hello")} $name"
    }
  }
  
  abstract class App extends GreetingSupport {
    val env: GreetingEnv
    def makeGreeting(name: String, tag: String) = greet(name,tag).run(env)
  }
  
  val app = new App with InMemoryGreetingSupport {
    val env = Map("es" -> "Hola","en" -> "Hiya")
  }
  
  println(app.makeGreeting("José","es"))   
  println(app.makeGreeting("Joe","en"))

}