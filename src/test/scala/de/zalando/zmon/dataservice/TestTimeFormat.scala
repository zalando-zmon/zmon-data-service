package de.zalando.zmon.dataservice

import org.scalatest.{Matchers, FlatSpec}

/**
 * Created by jmussler on 9/11/15.
 */
class TestTimeFormat extends FlatSpec with Matchers {
  "Worker dates" should "be equal" in {
    val s = "2014-08-10 15:59:02.973108+02:00"
    val d = RedisDataStore.extractFromPyString(s)
    val s2 = "2014-08-10 15:59:02.973+02:00"
    val d2 = RedisDataStore.extractFromPyString(s2)
    d2.compareTo(d) should be (0)
  }
}
