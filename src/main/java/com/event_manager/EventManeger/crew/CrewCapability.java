package com.event_manager.EventManeger.crew;

/**
 * Event-scoped powers granted via a {@link CrewRole} on an active assignment.
 * There is no separate platform MANAGER role — Manager/Supervisor are jobs/roles
 * that carry these capabilities when assigned to an event.
 */
public enum CrewCapability {
	VIEW_EVENT_CREW,
	VIEW_ASSIGNMENTS,
	MARK_ATTENDANCE,
	CORRECT_ATTENDANCE,
	UPLOAD_MEDIA,
	SUBMIT_REPORT,
	REVIEW_FEEDBACK,
	MONITOR_ACTIVITIES
}
