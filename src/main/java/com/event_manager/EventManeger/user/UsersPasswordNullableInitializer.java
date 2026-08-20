package com.event_manager.EventManeger.user;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
@Slf4j
public class UsersPasswordNullableInitializer implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(ApplicationArguments args) {
		try {
			jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN password SET NULL");
		} catch (Exception h2Style) {
			try {
				jdbcTemplate.execute("ALTER TABLE users MODIFY password VARCHAR(72) NULL");
			} catch (Exception mysqlStyle) {
				log.warn("Could not make users.password nullable: {}", mysqlStyle.getMessage());
				return;
			}
		}
		log.info("Ensured users.password allows NULL for OTP-only accounts");
	}
}
