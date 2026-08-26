package com.event_manager.EventManeger.workforce;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * H2 often creates STRING enums as native ENUM types that won't accept new values later.
 * Normalize workforce status columns to VARCHAR.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class WorkforceStatusColumnInitializer implements ApplicationRunner {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void run(ApplicationArguments args) {
		widen("event_jobs", "status");
		widen("job_applications", "status");
		widen("crew_assignments", "status");
		widen("attendances", "status");
	}

	private void widen(String table, String column) {
		try {
			jdbcTemplate.execute(
					"ALTER TABLE " + table + " ALTER COLUMN " + column + " SET DATA TYPE VARCHAR(32)");
			log.info("Ensured {}.{} is VARCHAR", table, column);
		} catch (Exception ignored) {
			try {
				jdbcTemplate.execute(
						"ALTER TABLE " + table + " MODIFY " + column + " VARCHAR(32) NOT NULL");
				log.info("Ensured {}.{} is VARCHAR", table, column);
			} catch (Exception mysqlStyle) {
				log.debug("Skip {}.{} widen: {}", table, column, mysqlStyle.getMessage());
			}
		}
	}
}
