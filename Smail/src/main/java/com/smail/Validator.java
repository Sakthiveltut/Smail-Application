package com.smail;

import java.util.regex.Pattern;

import com.smail.custom_exception.InvalidInputException;

public class Validator{
	static public boolean isValidName(String name) throws InvalidInputException{
		if(Pattern.compile("^[A-Za-z]+( [A-Za-z]+)*$").matcher(name).find()){
			return true;
		}else{
			throw new InvalidInputException("\nPlease enter a valid name");
		}
	}
	
	static public boolean isValidEmail(String email) throws InvalidInputException{
		if(Pattern.compile("^[a-z0-9]+(\\.[a-z0-9]+)*@[a-z]+(\\.[a-z]+)$").matcher(email).find()){
		//if(Pattern.compile("^((?=[a-z0-9]*[a-z])[a-z0-9]+(\\.[a-z0-9]+)*){6,30}@[a-z]+(\\.[a-z]+)+$").matcher(email).find()){
			return true;
		}else{
			throw new InvalidInputException(email+" is invalid.");
		}
	}
	
	public static boolean isValidEmails(String emails[]) throws InvalidInputException{
		for(String email:emails){
			if(!Validator.isValidEmail(email.trim())){
				return false;
			}
		}
		return true;
	}
	
	static public boolean isValidSmail(String email) throws InvalidInputException{
		if(Pattern.compile("^[a-z0-9]+(\\.[a-z0-9]+)*@smail.com").matcher(email).find()){
		//if(Pattern.compile("^((?=[a-z0-9]*[a-z])[a-z0-9]+(\\.[a-z0-9]+)*){6,30}@smail(\\.)com$").matcher(email).find()){
			return true;
		}else{
			throw new InvalidInputException(email+" is invalid.");
		}
	}
	
	static public boolean isValidSmail(String email,boolean showError) throws InvalidInputException{
		if(Pattern.compile("^[a-z0-9]+(\\.[a-z0-9]+)*@smail.com").matcher(email).find()){
		//if(Pattern.compile("^((?=[a-z0-9]*[a-z])[a-z0-9]+(\\.[a-z0-9]+)*){6,30}@smail(\\.)com$").matcher(email).find()){
			return true;
		}else if(showError){
			throw new InvalidInputException(email+" is invalid.");
		}
		return false;
	}

	static public boolean isValidPassword(String password) throws InvalidInputException{
		if(Pattern.compile("^(?!\\s)(?=.*[A-Z])(?=.*[0-9])(?=.*[\\W]).{8,100}(?<!\\s)$").matcher(password).find()){
			return true;
		}else{
			throw new InvalidInputException("\nPlease enter a valid password");
		}
	}
}


