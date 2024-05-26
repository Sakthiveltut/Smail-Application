import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main{
	
	public static void main(String[] args){
		
		while(true){
			System.out.println("\u001B[33m"+"1.Sign up\n2.Sign in"+"\u001B[0m");
			
			Scanner sc = new Scanner(System.in);
			String authChoice = sc.nextLine();
			User user = new User();
			switch(authChoice){
				case "1":
					System.out.print("\u001B[33m"+"Enter a name: "+"\u001B[0m");
					String name = sc.nextLine();
					
					System.out.print("\u001B[33m"+"\nEnter a email id(example@smail.com): "+"\u001B[0m");	
					String email = sc.nextLine();
					
					System.out.println("\u001B[33m"+"The password must be at least 8 characters long and contain at least one uppercase letter, one number, and one symbol.");
					System.out.print("\nEnter a new password(ex Sakthi@123): "+"\u001B[0m");
					String password1 = sc.nextLine();
					System.out.print("\u001B[33m"+"\nRe enter password: "+"\u001B[0m");
					String password2 = sc.nextLine();
					
					user.signUp(name,email,password1,password2);					
					break;
				case "2":
					System.out.print("\u001B[33m"+"Enter a email id: "+"\u001B[0m");
					email = sc.nextLine();
					System.out.print("\u001B[33m"+"Enter a password: "+"\u001B[0m");
					password1 = sc.nextLine();
					Integer id = user.signIn(email,password1);
					if(id!=null){
						boolean exit=false;
						while(!exit){
							System.out.println("\u001B[33m"+"1.View Profile\n2.Inbox\n3.Unread\n4.Send Message\n5.Starred\n6.Send\n7.Drafts\n8.Spam\n9.Bin\n10.Log out"+"\u001B[0m");
							String choice = sc.nextLine();
							Message message = new Message();
							String userDetails[] = user.getUserData(id);
							switch(choice){
								/*case "1":	
									System.out.println("Name: "+userDetails[0]);
									System.out.println("Email: "+userDetails[1]);
									System.out.println("Last login date and time: "+userDetails[2]);
									break;*/
								case "2":
									//message.viewMessages(id,"inbox",null);
									break;
								/*case "3":
									message.viewSpecificMessages(id,"is_read");
									break;*/
								case "4":
									String from = userDetails[1];
									System.out.print("\u001B[33m"+"From: "+"\u001B[0m"+from);
									System.out.print("\u001B[33m"+"\nTo(Separated with comma(,)): "+"\u001B[0m");
									String to = sc.nextLine();
									System.out.print("\u001B[33m"+"\nCC(Separated with comma(,)): "+"\u001B[0m");
									String cc = sc.nextLine();
									System.out.print("\u001B[33m"+"\nSubject: "+"\u001B[0m");
									String subject = sc.nextLine();		
									System.out.print("\u001B[33m"+"\nBody: "+"\u001B[0m");
									String description = sc.nextLine();
								
									message.sendMessage(id,from,to,cc,subject,description);							
									break;
								/*case "5":
									message.viewSpecificMessages(id,"is_starred");
									break;
								case "6":
									message.options(id,"send");
									break;
								case "7":
									message.options(id,"draft");
									break;
								case "8":
									message.options(id,"spam");
									break;
								case "9":
									message.options(id,"bin");
									break;*/
								case "10":
									exit=true;
									break;
								default:
									System.out.println("\033[31m"+"Invalid choice.Please try again..."+"\033[0m");
							}
						}
					}
					break;
				default:
					System.out.println("\033[31m"+"Invalid choice.Please try again..."+"\033[0m");
			}
		}
	}
}