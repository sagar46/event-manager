package com.event_manager.EventManeger.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.admin")
public class AdminProperties {

	private String phone = "9999999999";
	private String fullName = "System Admin";
	private String email = "admin@eventmanager.local";
}
