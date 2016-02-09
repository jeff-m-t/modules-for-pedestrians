# modules-for-pedestrians
Source code to accompany my "Modules for Pedestrians" presentation

There are tags for each of the key points in the presentation:
* ch1 - Initial set up.  KV module interface defined.  Simple im-memory implementation
* ch2 - Prototype web service based on in-memory impl.
* ch3 - a "realistic" implementation of the KV module backed by MongoDb
* ch4 - To avoid issues with initialization order, a simple life cycle is introduced
  * a lifecycle can be avoided by changing the way modules are configured (ch4-no-lifecycle branch) 
* ch5 - Two more modules added so the service has "interesting" business logic
  * ch5-readers-basic - dependency injection through Reader Monad 
  * ch5-implicits - making the Reader based app a bit more readable 

Integration Tests
=================

$ docker run -p 27017:27017 -d mongo
