package com.beachape.hystrixscratchpad.hystrixcommands

import com.netflix.hystrix._
import java.util.Collection
import scala.collection.JavaConversions._
import com.netflix.hystrix.HystrixCollapser.CollapsedRequest
import com.netflix.hystrix.HystrixCommand.Setter
import spray.client.pipelining._
import spray.httpx.SprayJsonSupport._
import org.apache.commons.validator.routines.UrlValidator
import akka.actor.ActorSystem
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.{Await, Future, ExecutionContext}
import ExecutionContext.Implicits.global
import scala.language.postfixOps
import rx.lang.scala.JavaConversions._


/**
 * Holds configurations and instantiates [[CommandCollapserScrapeUrl]]
 */
object CommandCollapserScrapeUrl extends ScrapedDataMarshallingSupport with ObservableToFutureSupport {

  val hystrixConfig: Setter = Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ScrapeUrlGroup"))
    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
    .withExecutionIsolationThreadTimeoutInMilliseconds(10000))
    .andCommandKey(HystrixCommandKey.Factory.asKey("CommandCollapserScrapeUrl"))

  // Validator
  val schemes =  Seq("http", "https")
  val urlValidator = new UrlValidator(schemes.toArray)

  // For our Http calls
  implicit val actorRef = ActorSystem("CollapserCommandScrapeUrl") // Hystrix demands a bulkhead approach
  implicit val callTimeout = Timeout(10 seconds)
  val pipeline = sendReceive ~> unmarshal[ScrapedData]

  /**
   * Instantiates a new CommandCollapserScrapeUrl
   * @param url String
   * @return CommandCollapserScrapeUrl
   */
  def apply(url: String) = new CommandCollapserScrapeUrl(url)

}

/**
 * Class that does batching of requests when used within a HystrixContext
 *
 * Should be instantiated via companion object
 */
class CommandCollapserScrapeUrl(private val url: String) extends HystrixCollapser[Seq[Option[ScrapedData]], Option[ScrapedData], String] {

  import CommandCollapserScrapeUrl._

  require(urlValidator.isValid(url))

  /**
   * Required method, returns the argument
   * @return
   */
  override def getRequestArgument: String = url

  /**
   * Required method, creates a single batch command
   * @param requests
   * @return
   */
  override protected def createCommand(requests: Collection[CollapsedRequest[Option[ScrapedData], String]]): HystrixCommand[Seq[Option[ScrapedData]]] =
    new BatchCommand(requests)

  /**
   * Required method, maps responses from BatchCommand to the original requests
   * @param batchResponse Seq[Option[ScrapedData]] from running BatchCommand
   * @param requests requests that have been batched
   */
  override protected def mapResponseToRequests(batchResponse: Seq[Option[ScrapedData]], requests: Collection[CollapsedRequest[Option[ScrapedData], String]]) {
    for ((response, request) <- batchResponse zip requests) {
      request setResponse response
    }
  }

  def future: Future[Option[ScrapedData]] = observableToFuture(observe)

  /**
   * Private internal HystrixCommand class that does the actual request processing
   * @param requests
   */
  private class BatchCommand(private val requests: Collection[CollapsedRequest[Option[ScrapedData], String]])
    extends HystrixCommand[Seq[Option[ScrapedData]]](hystrixConfig) {

    override protected def run: Seq[Option[ScrapedData]] = {
      /*
        Take the collection of requests and make Http requests for all of them as Futures.
        For the successful ones, map them to Some(ScrapedData), otherwise recover them as None

        Convert the entire Iterable of Futures and turn it into a Future Iterable, then await on
        the result. Lastly, convert to Seq
       */
      Await.result(Future.sequence(requests map { req =>
        pipeline(
          Get(s"http://metascraper.beachape.com/scrape/${java.net.URLEncoder.encode(req.getArgument, "UTF8")}")).
          map(Some(_)) recover { case _ => None }
      }), callTimeout.duration).toSeq
    }

  }

}
