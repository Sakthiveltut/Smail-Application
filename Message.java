import java.util.List;
import java.util.Scanner;
import java.util.Arrays;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Connection;

public class Message{
	private static Connection connection = DBConnection.getConnection();
	SpamChecker spamChecker = new SpamChecker();
	Scanner sc = new Scanner(System.in);

	public void sendMessage(int id,String from,String to,String cc,String subject,String description){
		User user = new User();
		Integer toId = user.userExists(to);
		if(toId!=null){
			boolean isValidEmail = true;
			Integer ccIds[] = new Integer[0];
			if(!cc.isEmpty()){
				String ccEmails[] = cc.split(",");
				ccIds = new Integer[ccEmails.length];
				for(int i=0;i<ccEmails.length;i++){
					ccIds[i] = user.userExists(ccEmails[i]);
					if(ccIds[i]==null){
						isValidEmail=false;
						System.out.println("\033[31m"+ccIds[i]+" is not exists."+"\033[0m");	
						break;
					}
				}
			}
			if(isValidEmail){
				
				String query = "insert into Messages(sender_id,subject,description) values(?,?,?)";
				try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
					preparedStatement.setInt(1,id);
					preparedStatement.setString(2,subject);
					preparedStatement.setString(3,description);
					preparedStatement.executeUpdate();
					
					
					
					String folderName = spamChecker.isSpam(subject+" "+description)?"spam":"inbox";

					preparedStatement.setInt(1,toId);
					preparedStatement.setInt(2,user.getFolderId(toId,folderName));
					preparedStatement.setString(3,from);
					preparedStatement.setString(4,to);
					preparedStatement.setString(5,cc);
					preparedStatement.setString(6,subject);
					preparedStatement.setString(7,description);
					preparedStatement.executeUpdate();	
					
					for(int ccId:ccIds){
						preparedStatement.setInt(1,ccId);
						preparedStatement.setInt(2,user.getFolderId(ccId,folderName));
						preparedStatement.setString(3,from);
						preparedStatement.setString(4,to);
						preparedStatement.setString(5,cc);
						preparedStatement.setString(6,subject);
						preparedStatement.setString(7,description);
						preparedStatement.executeUpdate();	
					}
					
					System.out.println("\u001B[32m"+"Message sented successfully.\n"+"\u001B[0m");
				}catch(SQLException e){
					e.printStackTrace();
				}
			}				
		}else
			System.out.println("\033[31m"+to+" is not Exists."+"\033[0m");
	}
	
	public void updateMessage(int id,String subject,String description){
		String query = "insert into Messages(sender_id,subject,description) values(?,?,?)";
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setInt(1,id);
			preparedStatement.setString(2,subject);
			preparedStatement.setString(3,description);
			preparedStatement.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void updateRecipients(int id,String email,String type){
		String query = "insert into Recipients(message_id,email,type) values(?,?,?)";
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setInt(1,id);
			preparedStatement.setString(2,email);
			preparedStatement.setString(3,type);
			preparedStatement.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	
	public void viewMessages(int id,String folderName,String searchKeyword){
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
	}
	
	public void viewSpecificMessages(int id,String folderName){
		String query = "select `to`,cc,subject,description,created_at_datetime from Messages where user_id=? and "+folderName+"=true";
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setInt(1,id);
			ResultSet resultSet = preparedStatement.executeQuery();
			while(resultSet.next()){
				String from = resultSet.getString("from");
				String to = resultSet.getString("to");
                String cc = resultSet.getString("cc");
                String subject = resultSet.getString("subject");
                String description = resultSet.getString("description");
                String createdAt = resultSet.getString("created_at_datetime");
				
                System.out.println("From: " + from);
                System.out.println("To: " + to);
                System.out.println("CC: " + cc);
                System.out.println("Subject: " + subject);
                System.out.println("Description: " + description);
                System.out.println("Created At: " + createdAt);
				System.out.println("--------------------------------------------------------");
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	public void options(int user_id,String folderName){
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
	}
}
