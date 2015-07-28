import org.scalatest._
import scala.collection.convert.wrapAsJava._
import de.zalando.zmon.dataservice._

class KairsDBSpec extends FlatSpec with Matchers {
  "rate" should "match api.rate" in {
    KairosDBStore.extractMetricName("api.rate") should be ("rate")
  }

  "api" should "match api." in {
    KairosDBStore.extractMetricName("api.") should be ("api")
  }

  "api2" should "match api2" in {
    KairosDBStore.extractMetricName("api2") should be ("api2")
  }

  "rate" should "match api" in {
    KairosDBStore.extractMetricName("api.GET.200.rate") should be ("rate")
  }

  "null" should "match ''" in {
    KairosDBStore.extractMetricName("") should be (null)
  }

  "null" should "match null" in {
    KairosDBStore.extractMetricName(null) should be (null)
  }

  "a" should "match a" in {
    KairosDBStore.extractMetricName("a") should be ("a")
  }
}