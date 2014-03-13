package com.beachape.hystrixscratchpad.hystrixcommands

import org.scalatest.{Matchers, FunSpec}
import org.scalatest.concurrent.ScalaFutures
import scala.language.postfixOps
import scala.concurrent.duration._
import scala.collection.JavaConversions._
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext
import scala.concurrent.{ExecutionContext, Future}
import com.netflix.hystrix.{HystrixEventType, HystrixRequestLog}

class CommandCollapserScrapeUrlSpec extends FunSpec with Matchers with ScalaFutures {

  implicit val timeout = PatienceConfig(10 seconds)
  import ExecutionContext.Implicits.global

  val allValidUrls = Map(
    "working1" -> "http://beachape.com",
    "working2" -> "http://www.beachape.com/blog/archives",
    "working3" -> "http://www.beachape.com/about"
  )

  describe("calling #future") {
    it("should still return the correct results") {
      runInContext(allValidUrls){ namesToCommandsMap =>
        val commands = namesToCommandsMap.values
        val commandFutures = commands map (_.future)
        val futureCommandResults = Future.sequence(commandFutures)
        whenReady(futureCommandResults) { results =>
          results.forall(_.isDefined) should be(true)
        }
      }
    }

    it("should only have executed the batch command just once") {
      runInContextAndBlock(allValidUrls){ () =>
        val executedCommands = HystrixRequestLog.getCurrentRequest().getExecutedCommands().size()
        executedCommands should be(1)
      }
    }

    it("should have executed as a COLLAPSED command") {
      runInContextAndBlock(allValidUrls){ () =>
        val command  = HystrixRequestLog.getCurrentRequest().getExecutedCommands().head
        command.getExecutionEvents should contain(HystrixEventType.COLLAPSED)
      }
    }
  }

  private def runInContext(urls: Map[String, String])(p: => (Map[String, CommandCollapserScrapeUrl]) => Unit) {
    val context = HystrixRequestContext.initializeContext()
    p(for ((id, url) <- urls) yield (id -> CommandCollapserScrapeUrl(url)))
    context.shutdown()
  }

  private def runInContextAndBlock(urls: Map[String, String])(p: => () => Unit ) {
    runInContext(allValidUrls) { namesToCommandsMap =>
      val commands = namesToCommandsMap.values
      val commandFutures = commands map (_.future)
      val futureCommandResults = Future.sequence(commandFutures)
      whenReady(futureCommandResults) { results => Unit } // Just block ..
      p()
    }
  }
}
