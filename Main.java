import java.util.Scanner;

public class Main{
	static Scanner sc = new Scanner(System.in);
	static User user;
	static UserDatabase userDatabase = UserDatabase.getInstance();
	static String name,email,password1,password2;
	static Login auth = new Login();
	
	public static void main(String[] args){
		
			
			System.out.println("      ______                 _ _ ");
			System.out.println("     |  ____|               (_) |");
			System.out.println("     | |__   _ __ ___   __ _ _| |");
			System.out.println("     |  __| | '_ ` _ \\ / _` | | |");
			System.out.println("     | |____| | | | | | (_| | | |");
			System.out.println("     |______|_| |_| |_|\\__,_|_|_|");

	 
		while(true){
			System.out.println("\u001B[33m"+"1.Signup\n2.Login"+"\u001B[0m");
		
			String str = sc.nextLine();
			if(str.equals("1") || str.equals("2")){
				int choice = Integer.parseInt(str);
				switch(choice){
					case 1:
						user = new User();
						if(isName() && isEmail() && isPassword()){
							System.out.println("\u001B[32m"+"Account created successfully."+"\u001B[0m");
						}
						userDatabase.addUser(user);
						break;
					case 2:
						auth.loggedIn();
						break;
					default:
						System.out.println("\033[31m"+"Invalid choice.Please try again..."+"\033[0m");
				}
				System.out.println(userDatabase.getUser());
			}else
				System.out.println("Invalid input.");
		}
	}
	
	static boolean isName(){
		System.out.println("\u001B[33m"+"Enter a name: "+"\u001B[0m");
		name = sc.nextLine();
		if(!user.setName(name)){
			isName();
		}
		name="";
		return true;
	}
	static boolean isEmail(){
		System.out.println("\u001B[33m"+"\nEnter a email id: "+"\u001B[0m");	
		email = sc.nextLine();
		if(!user.setEmail(email)){
			isEmail();
		}
		email="";
		return true;
	}
	static boolean isPassword(){
		System.out.println("\u001B[33m"+"The password must be at least 8 characters long and contain at least one uppercase letter, one number, and one symbol.");
		System.out.println("\nEnter a new password(ex Sakthi@123): "+"\u001B[0m");
		password1 = sc.nextLine();
		
		System.out.println("\u001B[33m"+"\nRe enter password: "+"\u001B[0m");
		password2 = sc.nextLine();
		
		if(!user.setPassword(password1,password2)){
			isPassword();
		}
		password1="";password2="";
		return true;
	}
}