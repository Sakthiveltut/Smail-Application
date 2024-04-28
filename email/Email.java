package email;
import java.util.Scanner;
import java.util.List;

public class Email{
	static Scanner sc = new Scanner(System.in);
	static User user;
	static UserDatabase userDatabase = UserDatabase.getInstance();
	static String name,email,password1,password2;
	static Login auth = new Login();
	
	public static void main(String[] args){
		
		while(true){
			
			System.out.println("      ______                 _ _ ");
			System.out.println("     |  ____|               (_) |");
			System.out.println("     | |__   _ __ ___   __ _ _| |");
			System.out.println("     |  __| | '_ ` _ \\ / _` | | |");
			System.out.println("     | |____| | | | | | (_| | | |");
			System.out.println("     |______|_| |_| |_|\\__,_|_|_|");

	 
			System.out.println("1.Signup\n2.Login");
			int choice = sc.nextInt();
			switch(choice){
				case 1:
					user = new User();
					if(isName() && isEmail() && isPassword()){
						System.out.println("Account created successfully.");
					}
					userDatabase.addUser(user);
					break;
				case 2:
					auth.loggedIn();
					break;
				default:
					System.out.println("Invalid choice.Please try again...");
			}
			System.out.println(userDatabase.getUser());
		}
	}
	
	static boolean isName(){
		System.out.println("Enter a name: ");
		sc.nextLine();
		name = sc.nextLine();
		if(!user.setName(name)){
			isName();
		}
		name="";
		return true;
	}
	static boolean isEmail(){
		System.out.println("\nEnter a email id: ");	
		email = sc.nextLine();
		if(!user.setEmail(email)){
			isEmail();
		}
		email="";
		return true;
	}
	static boolean isPassword(){
		System.out.println("\nEnter a new password: ");
		password1 = sc.nextLine();
		
		System.out.println("\nRe enter password: ");
		password2 = sc.nextLine();
		
		if(!user.setPassword(password1,password2)){
			isPassword();
		}
		password1="";password2="";
		return true;
	}
}