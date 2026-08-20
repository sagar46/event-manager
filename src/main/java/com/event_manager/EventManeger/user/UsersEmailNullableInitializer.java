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
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class UsersEmailNullableInitializer implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(ApplicationArguments args) {
		try {
			jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN email SET NULL");
		} catch (Exception h2Style) {
			try {
				jdbcTemplate.execute("ALTER TABLE users MODIFY email VARCHAR(191) NULL");
			} catch (Exception mysqlStyle) {
				log.warn("Could not make users.email nullable: {}", mysqlStyle.getMessage());
				return;
			}
		}
		log.info("Ensured users.email allows NULL so phone-first signup can run");
	}
}
