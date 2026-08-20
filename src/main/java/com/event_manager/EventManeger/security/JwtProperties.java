package com.event_manager.EventManeger.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

	private String secret;
	private String issuer = "event-manager";
	private Duration expiration = Duration.ofHours(1);
}
