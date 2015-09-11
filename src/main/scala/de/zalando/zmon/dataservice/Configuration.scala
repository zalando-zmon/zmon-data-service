package de.zalando.zmon.dataservice

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

import scala.beans.BeanProperty

@Component
@Configuration
@ConfigurationProperties(prefix = "dataservice")
class DataServiceConfig {
  @BeanProperty var redis_host = "localhost"
  @BeanProperty var redis_port = 6378

  @BeanProperty var kairosdb_host = "localhost"
  @BeanProperty var kairosdb_port = 8083

  @BeanProperty var proxy_controller = false
  @BeanProperty var proxy_controller_url = "http://localhost:8080/api/v1/"

  @BeanProperty var proxy_scheduler = false
  @BeanProperty var proxy_scheduler_url = "http://localhost:8085/api/v1/"

  @BeanProperty var log_check_data = false
  @BeanProperty var log_kairosdb_errors = false

  @BeanProperty var actuator_metric_checks : java.util.List[Integer] = new java.util.ArrayList[Integer]()
}