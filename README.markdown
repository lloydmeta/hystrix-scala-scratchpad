# Readme

This is Lloyd's Hystrix scratchpad

## Examples

### Single command returning a Scala Future
```scala
import scala.concurrent.ExecutionContext.Implicits.global
import com.beachape.hystrixscratchpad.hystrixcommands._
import scala.concurrent.Future
import scala.util.Success

val newCommand = CommandScrapeUrl("http://amazon.com")
newCommand.future onComplete {
  case Success(Some(x)) => println(x)
  case _  => println("oops")
}
```

### Collapsed Command
```scala
import scala.concurrent.ExecutionContext.Implicits.global
import com.beachape.hystrixscratchpad.hystrixcommands._
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext
import scala.concurrent.Future
import scala.util.Success

// Initialise a HystrixRequestContext. All Hystrix commands that derive
// from a Collapser class are "collapsed" within this context to run inside
// a single command
val context = HystrixRequestContext.initializeContext()

val c1 = CommandCollapserScrapeUrl("http://beachape.com")
val c2 = CommandCollapserScrapeUrl("http://google.com")
val c3 = CommandCollapserScrapeUrl("http://yahoo.com")

val listOfFutures = List(c1, c2, c3).map(_.future)
val futureList = Future.sequence(listOfFutures)

futureList.foreach(_.foreach {
    case Some(x) => println(x)
    case _ => println("oops")
})

// Sleep for 10 seconds to give the requests to come
// back before shutting down the context
Thread.sleep(10000)

// Shut down the context
context.shutdown()
```