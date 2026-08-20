package com.event_manager.EventManeger.common;

public class PhoneAlreadyExistsException extends RuntimeException {

	public PhoneAlreadyExistsException(String phone) {
		super("An account with this mobile number already exists");
	}
}
