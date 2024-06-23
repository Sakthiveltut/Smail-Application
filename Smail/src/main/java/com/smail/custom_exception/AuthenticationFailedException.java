package com.smail.custom_exception;

public class AuthenticationFailedException extends Exception {
	
	public AuthenticationFailedException(String message) {
		super(message);
	}
}
