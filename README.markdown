# Readme

This is Lloyd's Hystrix scratchpad

## Examples

```scala
import scala.concurrent.ExecutionContext.Implicits.global
import com.beachape.hystrixscratchpad.hystrixcommands._
import scala.util.Success

val newCommand = CommandScrapeUrl("http://amazon.com")
newCommand.future onComplete {
  case Success(Some(x)) => println(x)
  case _  => println("oops")
}
```