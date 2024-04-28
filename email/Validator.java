package email;

import java.util.regex.Pattern;

public class Validator{
	static public boolean isValidName(String name){
		if(Pattern.compile("^[a-z]+$").matcher(name).find()){
			return true;
		}else{
			System.out.println("\nPlease enter a valid name");
			return false;
		}
	}
	
	static public boolean isValidEmail(String email){
		if(Pattern.compile("^[a-z0-9]+(\\.[a-z0-9]+)*@[a-z]+(\\.[a-z]+)$").matcher(email).find()){
			return true;
		}else{
			System.out.println("\nPlease enter a valid email id");
			return false;
		}
	}

	static public boolean isValidPassword(String password){
		//System.out.println("\nPassword mismatch.");
		return true;
	}
	
	static public boolean isValidCC(String cc){
		
		return true;
	}
}

