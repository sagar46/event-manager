package com.event_manager.EventManeger.attendance;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceAuditRepository extends JpaRepository<AttendanceAudit, Long> {
	List<AttendanceAudit> findByAttendanceOrderByCreatedAtDesc(Attendance attendance);
}
