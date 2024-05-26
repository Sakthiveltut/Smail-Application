import java.util.regex.Pattern;

public class Validator{
	static public boolean isValidName(String name){
		if(Pattern.compile("^[A-Za-z]+( [A-Za-z]+)*$").matcher(name).find()){
			return true;
		}else{
			System.out.println("\033[31m"+"\nPlease enter a valid name"+"\033[0m");
			return false;
		}
	}
	
	static public boolean isValidEmail(String email){
		if(Pattern.compile("^[a-z0-9]+(\\.[a-z0-9]+)*@[a-z]+(\\.[a-z]+)$").matcher(email).find()){
			return true;
		}else{
			System.out.println("\033[31m"+"\n"+email+" is invalid."+"\033[0m");
			return false;
		}
	}
	
	static public boolean isValidSmail(String email){
		if(Pattern.compile("^[a-z0-9]+(\\.[a-z0-9]+)*@smail.com").matcher(email).find()){
			return true;
		}else{
			System.out.println("\033[31m"+"\n"+email+" is invalid."+"\033[0m");
			return false;
		}
	}

	static public boolean isValidPassword(String password){
		if(Pattern.compile("^(?!\\s)(?=.*[A-Z])(?=.*[0-9])(?=.*[\\W]).{8,}(?<!\\s)$").matcher(password).find()){
			return true;
		}else{
			System.out.println("\033[31m"+"\nPlease enter a valid password"+"\033[0m");
			return false;
		}
	}
}

