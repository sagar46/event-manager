package com.event_manager.EventManeger.auth;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.otp")
public class OtpProperties {

	private Duration ttl = Duration.ofMinutes(5);
	private int length = 6;
	private Duration resendCooldown = Duration.ofSeconds(45);
	private int maxAttempts = 5;
	private boolean devEcho = false;
}
