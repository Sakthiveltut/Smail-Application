import java.util.List;
import java.util.Scanner;
import java.util.Arrays;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.List;
import java.util.ArrayList;

public class Message{
	private static Connection connection = DBConnection.getConnection();
	SpamChecker spamChecker = new SpamChecker();
	Scanner sc = new Scanner(System.in);
	List<Integer> userIds,anonymousUserIds;
	List<String> unregisteredUsers;

	public void sendMessage(int id,String from,String to,String cc,String subject,String description){
		User user = new User();	
		if(!to.isEmpty()){
			String toEmails[] = to.split(",");
			if(isValidEmails(toEmails)){
				String ccEmails[] = cc.split(",");
				if(isValidEmails(ccEmails)){
					Integer messageId = setMessage(id,subject,description);
					
					separateUserIds(toEmails);
					for(Integer userId:userIds){
						setRecipients(messageId,userId,"to");
					}
					
					separateUserIds(ccEmails);
					for(Integer userId:userIds){
						setRecipients(messageId,userId,"cc");
					}
					setMessageFolders(id,messageId);
					System.out.println("\u001B[32m"+"Message sented successfully.\n"+"\u001B[0m");
				}
			}
		}
	}
	
	public boolean isValidEmails(String emails[]){
		for(String email:emails){
			if(!Validator.isValidEmail(email)){
				return false;
			}
		}
		return true;
	}
	
	public boolean separateUserIds(String emails[]){
		userIds = new ArrayList<>();
		anonymousUserIds = new ArrayList<>();
		unregisteredUsers = new ArrayList<>();
		User user = new User();
		for(String email:emails){
			if(Validator.isValidEmail(email)){
				if(Validator.isValidSmail(email)){
					Integer userId = user.userExists(email);
					if(userId==null){
						unregisteredUsers.add(email);
					}else{
						userIds.add(userId);
					}
				}else{
					Integer anonymousUserId = user.anonymousUserExists(email);
					if(anonymousUserId==null){
						anonymousUserId = user.setAnonymousUser(email);
					}
					anonymousUserIds.add(anonymousUserId);
				}
			}else{
				return false;
			}
		}
		return true;
	}
	
	public Integer setMessage(int id,String subject,String description){
		String query = "insert into Messages(sender_id,subject,description) values(?,?,?)";
		try(PreparedStatement preparedStatement = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){
			preparedStatement.setInt(1,id);
			preparedStatement.setString(2,subject);
			preparedStatement.setString(3,description);
			preparedStatement.executeUpdate();
			ResultSet resultSet = preparedStatement.getGeneratedKeys();
			if(resultSet.next()){
				int messageId = resultSet.getInt(1);
				resultSet.close();
				return messageId;
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}
	public void setRecipients(int message_id,int user_id,String type){
		String query = "insert into Recipients(message_id,user_id,type) values(?,?,?)";
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setInt(1,message_id);
			preparedStatement.setInt(2,user_id);
			preparedStatement.setString(3,type);
			preparedStatement.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void setMessageFolders(int user_id,int message_id){
		String query = "insert into MessageFolders(user_id,folder_id,message_id) values(?,(select id from Folders where name = ?),?)";
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setInt(1,user_id);
			preparedStatement.setString(2,"inbox");
			preparedStatement.setInt(3,message_id);
			preparedStatement.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	/*public void viewMessages(int id,String folderName,String searchKeyword){
		String query = "select id,`from`,`to`,cc,subject,description,created_at_datetime from Messages where user_id=? and folder_id=?";
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			User user = new User();
			preparedStatement.setInt(1,id);
			preparedStatement.setInt(2,user.getFolderId(id,folderName));
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()){
				int messsage_id = resultSet.getInt("id");
				String from = resultSet.getString("from");
				String to = resultSet.getString("to");
                String cc = resultSet.getString("cc");
                String subject = resultSet.getString("subject");
                String description = resultSet.getString("description");
                String createdAt = resultSet.getString("created_at_datetime");
				
				if(searchKeyword==null || subject.indexOf(searchKeyword)!=-1 || description.indexOf(searchKeyword)!=-1){
					System.out.println("Id: " + messsage_id);
					System.out.println("From: " + from);
					System.out.println("To: " + to);
					System.out.println("CC: " + cc);
					System.out.println("Subject: " + subject);
					System.out.println("Description: " + description);
					System.out.println("Created At: " + createdAt);
					System.out.println("--------------------------------------------------------");
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}*/
	
	/*public void options(int user_id,String folderName){
		String searchKeyword = null;
		viewMessages(user_id,folderName,searchKeyword);
		System.out.println("1.Search\n2.Delete\n3.Exit");
		String choice = sc.nextLine();
		if("1".equals(choice)){
			System.out.print("Search mail: ");
			searchKeyword = sc.nextLine();
			viewMessages(user_id,folderName,searchKeyword);
		}
		else if("2".equals(choice)){
			System.out.print("Enter message id: ");
			String message_id = sc.nextLine();
			if("bin".equals(folderName)){
				deleteMessage(Integer.parseInt(message_id));
			}
			else if(!"bin".equals(folderName)){
				messageSetBinFolder(user_id,Integer.parseInt(message_id));
			}
		}else if(!"3".equals(choice)){
			System.out.println("\033[31m"+"Invalid choice.Please try again..."+"\033[0m");
		}
	}
	public void messageSetBinFolder(int user_id,int message_id){
		String query = "update Messages set folder_id=? where id=?";
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setInt(1,new User().getFolderId(user_id,"bin"));
			preparedStatement.setInt(2,message_id);
			preparedStatement.executeUpdate();
			System.out.println("The message has been moved to the bin folder.");
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void deleteMessage(int id){
		String query = "delete from Messages where id = ?";
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setInt(1,id);
			preparedStatement.executeUpdate();
			System.out.println("Message deleted successfully.");
		}catch(Exception e){
			e.printStackTrace();
		}
	}*/
}
