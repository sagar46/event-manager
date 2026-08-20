package com.event_manager.EventManeger.common;

public class ForbiddenActionException extends RuntimeException {

	public ForbiddenActionException(String message) {
		super(message);
	}
}
