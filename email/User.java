package email;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

public class User{
	
	private	String name, email, password;
	private List<String> sentMessages, receivedMessages, draftMessages;
	
	public User(){
		sentMessages=new ArrayList<>();
		receivedMessages=new ArrayList<>();
		draftMessages=new ArrayList<>();
	}
	
	public String getName(){
		return name;
	}
	public String getEmail(){
		return email;
	}
	public String getPassword(){
		return password;
	}
	public List<String> getSentMessages(){
		return sentMessages;
	}
	public List<String> getReceivedMessages(){
		return receivedMessages;
	}
	public List<String> getDraftMessages(){
		return draftMessages;
	}
	
	public boolean setName(String name){
		if(Validator.isValidName(name)){
			this.name=name;
			return true;
		}
		return false;
	}
	public boolean setEmail(String email){
		
		if(Validator.isValidEmail(email)){
			if(UserDatabase.findUser(email)==null){
				this.email=email;
				return true;
			}else
				System.out.println("That email id is taken.Try another.");
		}
		return false;
	}
	public boolean setPassword(String password1,String password2){
		
		if(Validator.isValidPassword(password1) && password1.equals(password2)){
			this.password=password1;
			return true;
		}
		return false;
	}
	public void setSentMessage(String message){
		sentMessages.add(message);
	}
	public void setReceivedMessage(String message){
		receivedMessages.add(message);
	}
	public void setDraftMessage(String message){
		draftMessages.add(message);
	}
	
	@Override
	public String toString(){
		return "Name: "+name+" Email: "+email+" Password: "+password;
	}
}