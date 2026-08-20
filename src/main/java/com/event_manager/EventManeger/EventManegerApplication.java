package com.event_manager.EventManeger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EventManegerApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventManegerApplication.class, args);
	}

}
