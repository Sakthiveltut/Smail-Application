package com.smail.custom_exception;

public class RecipientNotFoundException extends Exception{
    public RecipientNotFoundException(String message) {
        super(message);
    }
}