package email;

import java.util.List;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class Login{
	static Scanner sc = new Scanner(System.in);
	static UserDatabase userDatabase = UserDatabase.getInstance();
	static User currentUser;
	boolean exit = false;
	public void loggedIn(){
		if(login()){
			while(!exit){
				System.out.println("1.Send Message\n2.View send messages\n3.View received messages\n4.View draft messages\n5.Log out");
				int choice = sc.nextInt();
				switch(choice){
					case 1:
						sendMessage();
						break;
					case 2:
						viewSentMessages();
						break;
					case 3:
						viewReceivedMessages();
						break;
					case 4:
						viewDraftMessages();
						break;
					case 5:
						logout();
						break;
					default:
						System.out.println("Invalid choice.Please try again...");
				}
			}
		}
	}
	
	public boolean login(){
		//sc.nextLine();
		System.out.println("Enter a email id: ");
		String email = sc.nextLine();
		System.out.println("Enter a password: ");
		String password = sc.nextLine();
		
		if(Validator.isValidEmail(email)){
			User user = UserDatabase.findUser(email);
			if(user!=null && user.getPassword().equals(password)){
				currentUser=user;
				exit=false;
				System.out.println("Login successfully.");
				return true;
			}
			System.out.println("Invalid username or password.");
		}
		return false;
	}
	
	public void sendMessage(){
		String to="",cc="",subject="",body="", multiEmail[] = new String[0];
		boolean emailValid=true;
		System.out.print("From: "+currentUser.getEmail());
		System.out.print("\nTo: ");
		sc.nextLine();
		to = sc.nextLine();
		if(Validator.isValidEmail(to)){
			if(UserDatabase.findUser(to)!=null){
				System.out.print("\nCC(Separated with comma(,)): ");
				cc = sc.nextLine();
				if(!cc.isEmpty()){
					multiEmail = cc.split(",");
					System.out.println(Arrays.toString(multiEmail));
					for(String email:multiEmail){
						if(!Validator.isValidEmail(email)){
							emailValid=false;
							break;
						}
						if(UserDatabase.findUser(email)==null){
							emailValid=false;
							System.out.println(email+" is not exists.");
							break;
						}
					}
				}
				if(emailValid){
					System.out.print("\nSubject: ");
					subject = sc.nextLine();		
					System.out.print("\nBody: ");
					body = sc.nextLine();
					
					StringBuilder sb = new StringBuilder();
					sb.append("Date: ").append(new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(new Date()));
					sb.append("\nFrom: ").append(currentUser.getEmail());
					sb.append("\nTo: ").append(to);
					sb.append("\nCC: ").append(cc);
					sb.append("\nSubject: ").append(subject);
					sb.append("\nBody: ").append(body).append("\n");
					
					System.out.println("1.Send message\n2.Add to draft message");
					int choice=sc.nextInt();
					
					switch(choice){
						case 1:
							currentUser.setSentMessage(sb.toString());
							UserDatabase.findUser(to).setReceivedMessage(sb.toString());
							if(!cc.isEmpty() && emailValid){
								for(String email: multiEmail){
									UserDatabase.findUser(email).setReceivedMessage(sb.toString());
								}
							}
							break;
						case 2:
							currentUser.setDraftMessage(sb.toString());
							break;
						default:
							System.out.println("Invalid choice.Please try again...");
					}
					
					System.out.println("Message sented successfully.\n");
				}
			}else
				System.out.println(to+" is not Exists.");
		}
	}
	
	public void viewSentMessages(){
		System.out.println(currentUser.getSentMessages());
	}
	
	public void viewReceivedMessages(){
		System.out.println(currentUser.getReceivedMessages());
	}
	
	public void viewDraftMessages(){
		List<String> list = currentUser.getDraftMessages();
		
		//System.out.println("1.Add\n2.Replace\n3.Delete\n4.Sent");
		
		
		System.out.println(list);
	}
	
	public void logout(){
		currentUser=null;
		exit=true;
	}
}

