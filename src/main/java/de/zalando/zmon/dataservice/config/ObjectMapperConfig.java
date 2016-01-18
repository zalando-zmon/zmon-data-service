package de.zalando.zmon.dataservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import de.zalando.zmon.dataservice.components.CustomObjectMapper;
import de.zalando.zmon.dataservice.components.DefaultObjectMapper;

@Configuration
public class ObjectMapperConfig {

	@Bean
	@Primary
	@DefaultObjectMapper
	public ObjectMapper defaultObjectMapper() {
		return new ObjectMapper();
	}

	@Bean
	@CustomObjectMapper
	public ObjectMapper customObjectMapper() {
		ObjectMapper custom = new ObjectMapper();
		custom.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		custom.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		return custom;
	}
}
