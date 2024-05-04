package email;
import java.util.Arrays;

public class Message{
	
	private int messageId=1;
	private String from="",to="",cc[],subject="",body="",date="";

	public String getDate(){
		return date;
	}
	public int getMessageId(){
		return messageId;
	}
	public String getFrom(){
		return from;
	}
	public String getTo(){
		return to;
	}
	public String[] getCc(){
		return cc;
	}
	public String getSubject(){
		return subject;
	}
	public String getBody(){
		return body;
	}
	public void setMessageId(int messageId){
		this.messageId=messageId;
	}
	public void setFrom(String from){
		this.from=from;
	}
	public void setTo(String to){
		this.to=to;
	}
	public void setCc(String[] cc){
		this.cc=cc;
	}
	public void setSubject(String subject){
		this.subject=subject;
	}
	public void setBody(String body){
		this.body=body;
	}
	public void setDate(String date){
		this.date=date;
	}	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append("Message Id: ").append(messageId);
		sb.append("\nDate: ").append(date);
		sb.append("\nFrom: ").append(from);
		sb.append("\nTo: ").append(to);
		sb.append("\nCC: ").append(Arrays.toString(cc));
		sb.append("\nSubject: ").append(subject);
		sb.append("\nBody: ").append(body).append("\n");
		
		return sb.toString();
	}
}
