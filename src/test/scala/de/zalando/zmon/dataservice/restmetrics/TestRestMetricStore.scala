package de.zalando.zmon.dataservice.restmetrics

import org.scalatest.{FlatSpec, Matchers}

/**
 * Created by jmussler on 9/11/15.
 */
class TestRestMetricStore extends FlatSpec with Matchers {
  "Store data" should "work" in {
    val app = new ApplicationVersion("application","1");
    val start = System.currentTimeMillis()

    val N = 360 // time points
    val M = 64 // instances

    for (i <- List.range(1, N)) {
      for (id <- List.range(1, M)) {
        for (ep <- List("/", "/users", "/accounts", "/bookings", "/stock", "/articles", "/inventory", "/payment","/entities","/hosts")) {
          for (m<-List("GET","POST","DELETE","PUT")) {
            for (sc <- List(200, 404, 500)) {
              app.addDataPoint("11111-22222-33333-" + id, ep, m, sc, start - (250 - i) * 60000, i, i * 1.12345)
            }
          }
        }
      }
    }

    System.out.println("used total: " + (System.currentTimeMillis() - start))

    val startAgg = System.currentTimeMillis()
    var data = app.getData(System.currentTimeMillis())
    data = app.getData(System.currentTimeMillis())
    val stopAgg = System.currentTimeMillis()

    System.out.println("used compute: " + ((stopAgg - startAgg)/2.0));
  }

  "Store data" should "return only two points for every endpoint" in {
    val app = new ApplicationVersion("application","2");
    val start = System.currentTimeMillis()

    val N = 2 // time points
    val M = 2 // instances

    for (i <- List.range(1, N+1)) {
      System.out.println("N " + i)
      for (id <- List.range(1, M+1)) {
        System.out.println("M " + id)
        for (ep <- List("/", "/users", "/accounts", "/bookings", "/stock", "/articles", "/inventory", "/payment","/entities","/hosts")) {
          for (m<-List("GET","POST","DELETE","PUT")) {
            for (sc <- List(200, 404, 500)) {
              val ts = start - (i) * 60000
              app.addDataPoint("11111-22222-33333-" + id, ep, m, sc, ts, i, i * 1.12345)
            }
          }
        }
      }
    }

    System.out.println("used total: " + (System.currentTimeMillis() - start))

    val startAgg = System.currentTimeMillis()
    var data = app.getData(System.currentTimeMillis())
    val stopAgg = System.currentTimeMillis()

    var kairosData =AppMetricsService.convertToKairosDB("application", data);

    System.out.println("used compute: " + ((stopAgg - startAgg)/2.0));
  }
}