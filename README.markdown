# Readme

This is Lloyd's Hystrix scratchpad

## Examples

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import com.beachape.hystrixscratchpad.hystrixcommands._
import scala.util.{Failure, Success}

val newCommand = CommandScrapeUrl("http://amazon.com")
newCommand.future onComplete {
  case Success(x) => println(x)
  case Failure(x) => println("oops")
}
```