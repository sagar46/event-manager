package com.event_manager.EventManeger.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(NotFoundException exception, HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ApiError> handleConflict(ConflictException exception, HttpServletRequest request) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), request);
	}

	@ExceptionHandler(ForbiddenActionException.class)
	public ResponseEntity<ApiError> handleForbiddenAction(
			ForbiddenActionException exception,
			HttpServletRequest request) {
		return error(HttpStatus.FORBIDDEN, exception.getMessage(), request);
	}

	@ExceptionHandler(InvalidPhoneException.class)
	public ResponseEntity<ApiError> handleInvalidPhone(InvalidPhoneException exception, HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(PhoneAlreadyExistsException.class)
	public ResponseEntity<ApiError> handlePhoneExists(PhoneAlreadyExistsException exception, HttpServletRequest request) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), request);
	}

	@ExceptionHandler(InvalidOtpException.class)
	public ResponseEntity<ApiError> handleInvalidOtp(InvalidOtpException exception, HttpServletRequest request) {
		return error(HttpStatus.UNAUTHORIZED, exception.getMessage(), request);
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<ApiError> handleEmailAlreadyExists(
			EmailAlreadyExistsException exception,
			HttpServletRequest request) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), request);
	}

	@ExceptionHandler(InvalidPersonaException.class)
	public ResponseEntity<ApiError> handleInvalidPersona(
			InvalidPersonaException exception,
			HttpServletRequest request) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(
			MethodArgumentNotValidException exception,
			HttpServletRequest request) {
		String message = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(FieldError::getDefaultMessage)
				.orElse("Validation failed");
		return error(HttpStatus.BAD_REQUEST, message, request);
	}

	@ExceptionHandler({BadCredentialsException.class, DisabledException.class})
	public ResponseEntity<ApiError> handleBadCredentials(AuthenticationException exception, HttpServletRequest request) {
		return error(HttpStatus.UNAUTHORIZED, "Invalid mobile number or OTP", request);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ApiError> handleAuthentication(AuthenticationException exception, HttpServletRequest request) {
		return error(HttpStatus.UNAUTHORIZED, "Invalid mobile number or OTP", request);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ApiError> handleDataIntegrityViolation(
			DataIntegrityViolationException exception,
			HttpServletRequest request) {
		// Keep the message generic so we don't leak DB details to clients.
		return error(HttpStatus.CONFLICT, "Database constraint violated", request);
	}

	@ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
	public ResponseEntity<ApiError> handleSpringNotFound(
			Exception exception,
			HttpServletRequest request) {
		return error(HttpStatus.NOT_FOUND, "Not found", request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleUnexpected(
			Exception exception,
			HttpServletRequest request) {
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", request);
	}

	private ResponseEntity<ApiError> error(HttpStatus status, String message, HttpServletRequest request) {
		return ResponseEntity.status(status).body(ApiError.of(status, message, request.getRequestURI()));
	}
}
