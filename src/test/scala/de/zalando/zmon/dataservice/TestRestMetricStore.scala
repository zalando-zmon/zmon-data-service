package de.zalando.zmon.dataservice

import de.zalando.zmon.dataservice.restmetrics.ApplicationVersion
import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by jmussler on 9/11/15.
 */
class TestRestMetricStore extends FlatSpec with Matchers {
  "Store data" should "work" in {
    val app = new ApplicationVersion();
    val start = System.currentTimeMillis()

    val N = 250 // time points
    val M = 32 // instances

    for (i <- List.range(1, N)) {

      for (id <- List.range(1, M)) {
        for (ep <- List("/", "/users", "/accounts", "/bookings", "/stock", "/articles", "/inventory", "/payment")) {
          for (sc <- List(200, 404, 500)) {
            app.addDataPoint(""+id, ep, sc, start - (250-i) * 60000, i, i * 1.12345)
          }
        }
      }
      System.out.println("\t" + i + ": " + (System.currentTimeMillis() - start))
    }

    System.out.println("used total: " + (System.currentTimeMillis() - start))

    val startAgg = System.currentTimeMillis()
    val data = app.getData(System.currentTimeMillis());
    val stopAgg = System.currentTimeMillis()

    System.out.println("used compute: " + (stopAgg - startAgg));
  }
}