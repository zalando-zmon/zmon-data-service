package de.zalando.zmon.dataservice.components;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.annotation.Qualifier;


/**
 * Custom-Qualifier for Beans of same type.
 * 
 * @author jbellmann
 *
 */
@Target({ ElementType.METHOD, ElementType.FIELD , ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier("customObjectMapper")
public @interface CustomObjectMapper {
}
