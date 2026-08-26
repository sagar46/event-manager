package com.event_manager.EventManeger.event;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Older H2 schemas created {@code events.status} as an ENUM without {@code COMPLETED}.
 * Hibernate {@code ddl-auto=update} does not widen ENUM values, so completing an event
 * fails with a data-integrity error until the column is a plain VARCHAR.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class EventStatusColumnInitializer implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(ApplicationArguments args) {
		try {
			jdbcTemplate.execute("ALTER TABLE events ALTER COLUMN status SET DATA TYPE VARCHAR(32)");
			log.info("Ensured events.status is VARCHAR so COMPLETED is allowed");
		} catch (Exception h2Style) {
			try {
				jdbcTemplate.execute("ALTER TABLE events MODIFY status VARCHAR(32) NOT NULL");
				log.info("Ensured events.status is VARCHAR so COMPLETED is allowed");
			} catch (Exception mysqlStyle) {
				log.debug("events.status column already compatible or alter skipped: {}", mysqlStyle.getMessage());
			}
		}
	}
}
