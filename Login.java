import java.util.List;
import java.util.Scanner;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;


public class Login{
	static Scanner sc = new Scanner(System.in);
	static UserDatabase userDatabase = UserDatabase.getInstance();
	static User currentUser;
	static int messageId;
	static List<Message> draftMessages;
	boolean exit = false;
	public void loggedIn(){
		if(login()){
			while(!exit){
				System.out.println("\u001B[33m"+"1.Send Message\n2.View send messages\n3.View received messages\n4.View draft messages\n5.Log out"+"\u001B[0m");
				int choice = sc.nextInt();
				switch(choice){
					case 1:
						sendMessage("new message",new Message());
						messageId++;
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
						System.out.println("\033[31m"+"Invalid choice.Please try again..."+"\033[0m");
				}
			}
		}
	}
	
	public boolean login(){
		//sc.nextLine();
		System.out.println("\u001B[33m"+"Enter a email id: "+"\u001B[0m");
		String email = sc.nextLine();
		System.out.println("\u001B[33m"+"Enter a password: "+"\u001B[0m");
		String password = sc.nextLine();
		
		if(Validator.isValidEmail(email)){
			User user = UserDatabase.findUser(email);
			if(user!=null && user.getPassword().equals(password)){
				currentUser=user;
				draftMessages = currentUser.getDraftMessages();
				exit=false;
				System.out.println("\u001B[32m"+"Login successfully."+"\u001B[0m");
				return true;
			}
			System.out.println("\033[31m"+"Invalid username or password."+"\033[0m");
		}
		return false;
	}
	
	public void sendMessage(String msg,Message m){
		int index = draftMessages.indexOf(m);
		String to="",cc="",subject="",body="", multiEmail[] = new String[0];
		boolean emailValid=true;
		System.out.print("\u001B[33m"+"From: "+"\u001B[0m"+currentUser.getEmail());
		System.out.print("\u001B[33m"+"\nTo: "+"\u001B[0m");
		sc.nextLine();
		to = sc.nextLine();
		if(Validator.isValidEmail(to)){
			if(UserDatabase.findUser(to)!=null){
				System.out.print("\u001B[33m"+"\nCC(Separated with comma(,)): "+"\u001B[0m");
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
							System.out.println("\033[31m"+email+" is not exists."+"\033[0m");
							break;
						}
					}
				}
				if(emailValid){
					System.out.print("\u001B[33m"+"\nSubject: "+"\u001B[0m");
					subject = sc.nextLine();		
					System.out.print("\u001B[33m"+"\nBody: "+"\u001B[0m");
					body = sc.nextLine();
					
					if("new message".equals(msg))
						m.setMessageId(messageId);
					m.setFrom(currentUser.getEmail());
					m.setTo(to);
					m.setDate(new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(new Date()));
					m.setCc(multiEmail);
					m.setSubject(subject);
					m.setBody(body);
					
					System.out.println("\u001B[33m"+"1.Send message\n2.Add to draft message"+"\u001B[0m");
					int choice=sc.nextInt();
					
					switch(choice){
						case 1:
							if(msg.equals("new message")){
								send(m);
							}else if(msg.equals("draft message")){
								draftMessages.removeIf(object->object.getMessageId()==m.getMessageId());
								send(m);
							}
							break;
						case 2:
							if(msg.equals("new message")){
								currentUser.setDraftMessage(m);
							}else if(msg.equals("draft message")){
								draftMessages.set(index,m);
							}
							System.out.println("\u001B[32m"+"Message drafted successfully.\n"+"\u001B[0m");
							break;
						default:
							System.out.println("\033[31m"+"Invalid choice.Please try again..."+"\033[0m");
					}
				}
			}else
				System.out.println("\033[31m"+to+" is not Exists."+"\033[0m");
		}
	}
	public void send(Message message){
		currentUser.setSentMessage(message);
		UserDatabase.findUser(message.getTo()).setReceivedMessage(message);
		if(message.getCc().length>0){
			for(String email: message.getCc()){
				UserDatabase.findUser(email).setReceivedMessage(message);
			}
		}
		System.out.println("\u001B[32m"+"Message sented successfully.\n"+"\u001B[0m");
	}
	
	public void viewSentMessages(){
		System.out.println(currentUser.getSentMessages());
	}
	
	public void viewReceivedMessages(){
		System.out.println(currentUser.getReceivedMessages());
	}
	
	public void viewDraftMessages(){
		Message msg;
		System.out.println("\u001B[33m"+"1.Show\n2.Edit\n3.Sent\n4.Delete"+"\u001B[0m");
		int choice = sc.nextInt();
		switch(choice){
			case 1:
				System.out.println(draftMessages);
				break;
			case 2:
				System.out.println(draftMessages);
				msg = findMessage();
				if(msg!=null)
					sendMessage("draft message",msg);
				break;
			case 3:
				msg = findMessage();
				if(msg!=null)
					send(msg);
				break;
			case 4:
				msg = findMessage();
				if(msg!=null)
					draftMessages.remove(msg);
				break;
			default:
				System.out.println("\033[31m"+"Invalid choice.Please try again..."+"\033[0m");
		}
	}
	
	public Message findMessage(){
		System.out.println("\u001B[33m"+"Enter your message id: "+"\u001B[0m");
		int id = sc.nextInt();
		for(Message draftMessage:draftMessages){
			if(draftMessage.getMessageId()==id){
				return draftMessage;
			}						
		}
		System.out.println("\033[31m"+"Message id not exists."+"\033[0m");
		return null;
	}
	
	
	public void logout(){
		currentUser=null;
		exit=true;
	}
}

