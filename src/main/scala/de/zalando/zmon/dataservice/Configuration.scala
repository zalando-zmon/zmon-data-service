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
}