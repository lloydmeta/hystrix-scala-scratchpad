package com.beachape.hystrixscratchpad.hystrixcommands

import com.netflix.hystrix.{HystrixCommandProperties, HystrixCommandGroupKey, HystrixCommand}
import spray.json.DefaultJsonProtocol
import spray.client.pipelining._
import org.apache.commons.validator.routines.UrlValidator
import scala.concurrent.{Future, Await, ExecutionContext}
import akka.actor.ActorSystem
import scala.concurrent.duration._
import akka.util.Timeout
import spray.httpx.SprayJsonSupport._
import rx.lang.scala.JavaConversions._
import scala.language.postfixOps
import ExecutionContext.Implicits.global
import com.netflix.hystrix.HystrixCommand.Setter

/**
 * Companion object that does instantiation
 *
 * Also holds implicit conversions and pipelines
 */
object CommandScrapeUrl extends DefaultJsonProtocol with ObservableToFutureSupport {

  val hystrixConfig: Setter = Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey("ScrapeUrlGroup"))
                                    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter()
                                    .withExecutionIsolationThreadTimeoutInMilliseconds(10000))

  // Validator
  val schemes =  Seq("http", "https")
  val urlValidator = new UrlValidator(schemes.toArray)

  // For our Http calls
  implicit val actorRef = ActorSystem("CommandScrapeUrl") // Hystrix demands a bulkhead approach
  implicit val callTimeout = Timeout(10 seconds)
  implicit val jsonToScrapedData = jsonFormat5(ScrapedData)
  val pipeline = sendReceive ~> unmarshal[ScrapedData]

  /**
   * Instantiates a CommandScrapeUrl
   * @param url String representing the URL to scrape
   * @return [[CommandScrapeUrl]]
   */
  def apply(url: String): CommandScrapeUrl = new CommandScrapeUrl(url)

}

/**
 * Our Command class that extends from HystrixCommand
 *
 * Should be instantiated using companion object
 */
class CommandScrapeUrl(private val url: String) extends HystrixCommand[Option[ScrapedData]](CommandScrapeUrl.hystrixConfig) {
  import CommandScrapeUrl._
  require(urlValidator.isValid(url))

  /**
   * Implements the run method for HystrixCommand
   * @return Option[ScrapedData]
   */
  def run: Option[ScrapedData] = {
    Await.result(
      pipeline(
        Get("http://metascraper.beachape.com/scrape/" + java.net.URLEncoder.encode(url, "UTF8"))).
        map(Some(_)) recover { case _ => None },
      callTimeout.duration)
  }

  /**
   * Returns a Future[ScrapedData] based on the underlying HystrixCommand's #observe method
   * @return Future[Option[ScrapedData]]
   */
  def future: Future[Option[ScrapedData]] = observableToFuture(observe)

  /**
   * The fallback is to return a None
   *
   * This is invoked if the command fails in any way by Hystrix.
   *
   * @return None
   */
  override def getFallback: Option[ScrapedData] = None

  /*
    If multiple instances of the same HystrixCommand return the same cacheKey within the same
    Hystrix Request Context, then the result is retrieved from the cached value of the first
    run
    */
//  override def getCacheKey: String = url
}

case class ScrapedData(
                        url: String,
                        title: String,
                        description: String,
                        mainImageUrl: String,
                        imageUrls: Seq[String])