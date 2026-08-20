package com.event_manager.EventManeger.auth;

import com.event_manager.EventManeger.common.InvalidPhoneException;

public final class PhoneNumbers {

	private PhoneNumbers() {
	}

	public static String normalize(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new InvalidPhoneException("Mobile number is required");
		}
		String digits = raw.replaceAll("\\D", "");
		if (digits.length() == 12 && digits.startsWith("91")) {
			digits = digits.substring(2);
		}
		if (digits.length() == 11 && digits.startsWith("0")) {
			digits = digits.substring(1);
		}
		if (!digits.matches("[6-9]\\d{9}")) {
			throw new InvalidPhoneException("Enter a valid 10-digit Indian mobile number");
		}
		return digits;
	}
}
