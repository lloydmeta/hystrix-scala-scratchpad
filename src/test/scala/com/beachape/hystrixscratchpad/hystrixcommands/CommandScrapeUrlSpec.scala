package com.beachape.hystrixscratchpad.hystrixcommands

import org.scalatest.{Matchers, FunSpec}
import org.scalatest.concurrent.ScalaFutures
import scala.language.postfixOps
import scala.concurrent.duration._

class CommandScrapeUrlSpec extends FunSpec with Matchers with ScalaFutures {

  implicit val timeout = PatienceConfig(10 seconds)

  trait CommandContext {
    def urlToHit: String
    def command = CommandScrapeUrl(urlToHit)
  }

  describe("#future method") {
    it("should return  Some(ScrapedData(_,..)) eventually") {
      new CommandContext {
        val urlToHit: String = "http://beachape.com"
        whenReady(command.future) { x =>
          val Some(data) = x
          data.isInstanceOf[ScrapedData] should be(true) }
      }
    }
    it("should return a None for a random URL that doesn't exist") {
      new CommandContext {
        val urlToHit: String = "http://beachape.com/somethingthatdoesntexist"
        whenReady(command.future) ( x => x should be ('empty))
      }
    }
  }
}

