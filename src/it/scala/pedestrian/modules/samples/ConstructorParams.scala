package pedestrian.modules.samples

object ConstructorParams extends App {
  trait GreetingSupport {
    def greet(name: String, languageTag: String): String
  }
  
  trait InMemoryGreetingSupport extends GreetingSupport {
    def localizedGreetings: Map[String,String]
    def greet(name: String, languageTag: String): String =
      s"${localizedGreetings.get(languageTag).getOrElse("Hello")} $name"
  }
  
  abstract class App extends GreetingSupport {
    def makeGreeting(name: String, tag: String) = greet(name,tag)
  }
  
  val app = new App with InMemoryGreetingSupport {
    override val localizedGreetings = Map("es" -> "Hola","en" -> "Hiya")
  }

  println(app.makeGreeting("José","es"))
   
  println(app.makeGreeting("Joe","en"))
  
  case class InMemoryGreeter(localizedGreetings: Map[String,String]) extends InMemoryGreetingSupport 
  
  val greeter = InMemoryGreeter(Map("es" -> "Hola","en" -> "Hiya"))
  
  val greeter2 = new pedestrian.core.BlankSlate with InMemoryGreetingSupport {
    override val localizedGreetings = Map("es" -> "Hola","en" -> "Hiya")    
  }
      
  class App2(greeter: GreetingSupport) {
    def makeGreeting(name: String, tag: String) = greeter.greet(name,tag)
  }
  
  println(app.makeGreeting("José","es"))
   
  println(app.makeGreeting("Joe","en"))      
}