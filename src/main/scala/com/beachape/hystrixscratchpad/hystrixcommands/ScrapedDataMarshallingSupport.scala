package com.beachape.hystrixscratchpad.hystrixcommands

import spray.json.DefaultJsonProtocol

/**
 * Support for unmarshalling and unmarshalling responses from
 * the remote Metadata scraper service
 */
trait ScrapedDataMarshallingSupport extends DefaultJsonProtocol {

  implicit val jsonToScrapedData = jsonFormat5(ScrapedData)

}
