package com.event_manager.EventManeger.common;

public class EmailAlreadyExistsException extends RuntimeException {

	public EmailAlreadyExistsException(String email) {
		super("An account with email " + email + " already exists");
	}
}
