package com.smail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import com.smail.custom_exception.InvalidEmailException;
import com.smail.custom_exception.InvalidInputException;
import com.smail.custom_exception.MessageNotFoundException;

public class MessageOperation {
	
	private String to,cc,subject,description,attachments;
	private Set<Long> validToRecipientIds,validCcRecipientIds;
	private Set<User> unregisteredToRecipients,unregisteredCcRecipients;
		
	private static final byte SEARCH_OPTION=1, DELETE_OPTION=2,STARRED_OPTION=4,EXIT_OPTION=3, MOVE_TO_INBOX=5, EDIT=5, SENT=6;
	private static final String FROM = "\u001B[33m"+"From: "+"\u001B[0m";
	private static final String TO = "To(Separated with comma(,)): ";
	private static final String CC = "CC(Separated with comma(,)): ";
	private static final String SUBJECT = "Subject: ";
	private static final String DESCRIPTION = "Description: ";
	private static final String ATTACHMENTS = "Attachments: ";
	
	private static final String SPAM_MESSAGE_OPTIONS = "1.Search\n2.Delete\n3.Exit\n4.Starred\n5.Move to inbox";
	private static final String DRAFT_MESSAGE_OPTIONS = "1.Search\n2.Delete\n3.Exit\n4.Starred\n5.Edit\n6.Send";
	private static final String MESSAGE_OPTIONS = "1.Search\n2.Delete\n3.Exit\n4.Starred";
	private static final String BIN_MESSAGE_OPTIONS = "1.Search\n2.Delete\n3.Exit";
	
	private static final String MESSAGE_ID = "Enter message id: ";

	private static final String SEARCH_MAIL = "Search mail: ";
	private static final String CHOICE = "Enter your choice: ";
	private static final String INVALID_INPUT = "\033[31m"+"Invalid input."+"\033[0m";
	private static final String MESSAGE_NOT_FOUND = "\033[31m"+"Message not found."+"\033[0m";
	
	private final static String BASE_QUERY = """
			SELECT 
			    m.id,
			    su.email AS sender_email,
			    m.subject,
			    m.description,
			    m.is_read,
			 	mf.is_starred,
			    m.created_time,
			    m.has_attachment,
			    GROUP_CONCAT(CASE WHEN rt.type = 'to' THEN u.email END SEPARATOR ', ') AS to_recipients,
			    GROUP_CONCAT(CASE WHEN rt.type = 'cc' THEN u.email END SEPARATOR ', ') AS cc_recipients
			FROM 
			    Messages m
			LEFT JOIN 
			    Recipients r ON m.id = r.message_id
			LEFT JOIN 
			    Users u ON r.user_id = u.id
			LEFT JOIN 
			    RecipientTypes rt ON r.type_id = rt.id
			JOIN 
			    MessageFolders mf ON m.id = mf.message_id
			JOIN 
			    Users su ON m.sender_id = su.id
			JOIN 
			    Folders f ON mf.folder_id = f.id
			WHERE 
			    mf.user_id = ?   
			""";
	
	private final static String GROUP_BY = "GROUP BY m.id;";
		
	/*public void viewMessageOptions(String folderName) throws Exception{  
		InputHandler inputHandler = new InputHandler();
		long userId = currentUser.getUserId();
		displayMessages(getMessages(folderName));
		if(Folder.getSpamName().equals(folderName))
			System.out.println(SPAM_MESSAGE_OPTIONS);
		else if(Folder.getDraftName().equals(folderName))
			System.out.println(DRAFT_MESSAGE_OPTIONS);
		else if(Folder.getBinName().equals(folderName))
			System.out.println(BIN_MESSAGE_OPTIONS);
		else
			System.out.println(MESSAGE_OPTIONS);
		Byte choice = inputHandler.readByte(CHOICE);
		if(choice!=null){																																																								
			if(SEARCH_OPTION==choice){
				String searchedKeyword = inputHandler.readString(SEARCH_MAIL);
				List<Message> message = getSearchedMessages(folderName,searchedKeyword);
				if(message!=null) {
					displayMessages(message);
				}
			}else if(DELETE_OPTION==choice){
				Long message_id = inputHandler.readLong(MESSAGE_ID);
				if(message_id!=null) {					
					if(Folder.getBinName().equals(folderName)){
						deleteMessage(message_id,Folder.getFolderId(folderName));
					}
					else if(!Folder.getBinName().equals(folderName)){
						changeMessageFolderId(userId,message_id,Folder.getFolderId(folderName),Folder.getFolderId(Folder.getBinName()));
					}
				}
			}else if(STARRED_OPTION==choice && folderName!=Folder.getBinName()){
				Long message_id = inputHandler.readLong(MESSAGE_ID);
				if(message_id!=null) {
					setStarredMessage(message_id,Folder.getFolderId(folderName));
				}
			}else if(Folder.getSpamName().equals(folderName) && MOVE_TO_INBOX==choice) {
				Long message_id = inputHandler.readLong(MESSAGE_ID);
				if(message_id!=null) {
					changeMessageFolderId(userId,message_id,Folder.getFolderId(folderName),Folder.getFolderId(Folder.getInboxName()));
				}
			}else if(Folder.getDraftName().equals(folderName) && EDIT==choice) {
				Long message_id = inputHandler.readLong(MESSAGE_ID);
				if(message_id!=null) {
					Message originalMessage = getMessage(folderName,message_id);
					if(originalMessage!=null) {
						System.out.println("From: " + originalMessage.getFrom());
						System.out.println("To: " + originalMessage.getTo());
						if(originalMessage.getCc()!=null) {
							System.out.println("CC: " + originalMessage.getCc());
						} 
						System.out.println("Subject: " + originalMessage.getSubject());
						System.out.println("Description: " + originalMessage.getDescription());
						System.out.println("--------------------------------------------------------");
						
						inputMessageDetails();
						updateDraftMessage(originalMessage);
					}
				}
			}else if(Folder.getDraftName().equals(folderName) && SENT==choice){
				Long messageId = inputHandler.readLong(MESSAGE_ID);
				if(messageId!=null) {
					Message message = getMessage(Folder.getDraftName(),messageId);
					sendMessage(folderName, message);
				}
			}else if(!(EXIT_OPTION==choice)){
				throw new InvalidInputException("Invalid choice.Please try again...");
			}
		}
	}*/
	
	private void updateDraftMessage(Message originalMessage) throws Exception {
		if(isValidMessage()) {
			if(!originalMessage.getTo().equals(to)) {
				deleteRecipient(originalMessage.getMessageId(),RecipientType.getRecipientType("to"));
				setToRecipients(originalMessage.getMessageId());
			}
			if(!(originalMessage.getCc()==null && cc==null) || (originalMessage.getCc()!=null && !originalMessage.getCc().equals(cc))) {
				deleteRecipient(originalMessage.getMessageId(),RecipientType.getRecipientType("cc"));
				setCcRecipients(originalMessage.getMessageId());
			}
			if(!originalMessage.getSubject().equals(subject)) {
				updateSubjectField(originalMessage.getMessageId(),subject);
			}
			if(!originalMessage.getDescription().equals(description)) {
				updateDescriptionField(originalMessage.getMessageId(),description);
			}
			System.out.println("\u001B[32m"+"The draft message has been edited successfully.\n"+"\u001B[0m");
		}
	}

	public void inputMessageDetails(String to,String cc,String subject,String description) {
		this.to = to;
		this.cc = cc;
		this.subject = subject;
		this.description = description;
	}
	
	public boolean isValidMessage() throws InvalidInputException {
		if(!to.isEmpty()){
			String toEmails[] = to.split(",");
			if(toEmails.length!=0){
				if(Validator.isValidEmails(toEmails)){
					boolean isEmptyCC = cc.isEmpty(),isValidCC = true;
					String ccEmails[] = new String[0];
					if(!isEmptyCC) {
						if(cc.matches("^[,]+$")) {
							isValidCC = false;
						}else {
							ccEmails = cc.split(",");
							isValidCC = Validator.isValidEmails(ccEmails);
						}
					}					
					if(isValidCC) {
						return true;
					}else
						throw new InvalidInputException("CC is invalid.");
				}
			}else
				throw new InvalidInputException("To address "+to+" is invalid");
		}else
			throw new InvalidInputException("To address is empty.");
		return false;
	}

	@SuppressWarnings("unchecked")
	public JSONObject createMessage() throws InvalidInputException, Exception {
		if(isValidMessage()) {
			long messageId = setMessage(UserDatabase.getCurrentUser().getUserId(),subject,description);
			setToRecipients(messageId);
			setCcRecipients(messageId);
			JSONObject message = new JSONObject();
			message.put("id", messageId);
			message.put("from", UserDatabase.getCurrentUser().getEmail());
			message.put("to", to);
	        message.put("cc", cc);
	        message.put("subject", subject);
	        message.put("description", description);
            message.put("is_read", false);
            message.put("is_starred", false);
            message.put("has_attachment", false);
			return message;
		}
		return null;
	}
	
	public void setToRecipients(long messageId) throws Exception {
		String toEmails[] = to.split(",");
		validToRecipientIds = new HashSet<>();	
		setRecipientIds(toEmails,validToRecipientIds);
		for(Long UserId:validToRecipientIds){
			setRecipient(messageId,UserId,RecipientType.getRecipientType("to"));
		}
	}
	public void setCcRecipients(long messageId) throws Exception {
		if(!cc.isEmpty()) {
			String ccEmails[] = to.split(",");
			validCcRecipientIds = new HashSet<>();
			setRecipientIds(ccEmails,validCcRecipientIds);
			for(Long UserId:validCcRecipientIds){
				if(!validToRecipientIds.contains(UserId)) {//To reduce duplicate entries in the MessageFolders table, ensure that both the 'To' and 'Cc' fields contain the same email address.
					setRecipient(messageId,UserId,RecipientType.getRecipientType("cc"));
				}
			}
		}
	}
	
	public void displayMessages(List<Message> messages) {
		if(messages!=null) {
			for(Message message:messages) {
				System.out.println("Id: " + message.getMessageId());
				System.out.println("From: " + message.getFrom());
				System.out.println("To: " + message.getTo());
				if(message.getCc()!=null) {
					System.out.println("CC: " + message.getCc());
				}
				System.out.println("Subject: " + message.getSubject());
				System.out.println("Description: " + message.getDescription());
				System.out.println("Unread: " + (message.isRead()?"Yes":"No"));
				System.out.println("Starred: " + (message.isStarred()?"Yes":"No"));
				System.out.println("Created At: " + message.getCreatedTime());
				System.out.println("--------------------------------------------------------");
			}
		}
	}
	
	public void sendMessage(String messageType,JSONObject message) throws InvalidEmailException, Exception{
		if(message!=null) {
			long messageId = (long)message.get("id");
			if("newMessage".equals(messageType)) {
				setMessageFolder(UserDatabase.getCurrentUser().getUserId(),messageId,Folder.getFolderId(Folder.getSentName()));
			}else if("draftMessage".equals(messageType)) {
				updateMessageFolder(UserDatabase.getCurrentUser().getUserId(),messageId,Folder.getFolderId(Folder.getSentName()));
			}
			String recipientFolderName;
			if(SpamChecker.isSpam((String)message.get("subject")+" "+(String)message.get("description"))) 
				recipientFolderName = Folder.getSpamName();
			else
				recipientFolderName = Folder.getInboxName();
			
			validToRecipientIds = new HashSet<>();
			validCcRecipientIds = new HashSet<>();
			unregisteredToRecipients = new HashSet<>();
			unregisteredCcRecipients = new HashSet<>();
			String to = (String) message.get("to");
			String toEmails[] = to.split(",");
			separateRecipientId(toEmails,validToRecipientIds,unregisteredToRecipients);
			for(Long userId:validToRecipientIds){
				setMessageFolder(userId,messageId,Folder.getFolderId(recipientFolderName));
			}
			String cc = (String) message.get("cc");
			if(cc!=null) {
				String ccEmails[] = cc.split(",");
				separateRecipientId(ccEmails,validCcRecipientIds,unregisteredCcRecipients);
				for(Long userId:validCcRecipientIds){
					if(!validToRecipientIds.contains(userId)) {//To reduce duplicate entries in the MessageFolders table, ensure that both the 'To' and 'Cc' fields contain the same email address.
						setMessageFolder(userId,messageId,Folder.getFolderId(recipientFolderName));
					}
				}
			}
			System.out.println("\u001B[32m"+"Message sent successfully.\n"+"\u001B[0m");
			String unregisteredEmails=null;
			if(!unregisteredToRecipients.isEmpty()) {
				for(User user:unregisteredToRecipients) {
					unregisteredEmails+=user.getEmail()+", ";
				}
				throw new InvalidEmailException("To Address not found: "+unregisteredEmails);
			}if(!unregisteredCcRecipients.isEmpty()) {
				unregisteredEmails=null;
				System.out.print("CC Address not found: ");
				for(User user:unregisteredCcRecipients) {
					unregisteredEmails+=user.getEmail()+", ";
				}
				throw new InvalidEmailException("Cc Address not found: "+unregisteredEmails);
			}
		}
	}
	
	public void deleteRecipient(Long messageId,byte recipientType) throws Exception {
		String query = "delete from Recipients where message_id = ? and type_id = ?";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,messageId);
			preparedStatement.setByte(2,recipientType);
			preparedStatement.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Error during edit draft message. Please go back and try again. Error details: " + e.getMessage());
		}
	}
	public void updateSubjectField(Long messageId,String data) throws Exception {
		String query = "update Messages set subject=? where id = ?";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setString(1,data);
			preparedStatement.setLong(2,messageId);
			preparedStatement.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Error during edit draft message. Please go back and try again. Error details: " + e.getMessage());
		}
	}
	public void updateDescriptionField(Long messageId,String data) throws Exception {
		String query = "update Messages set description=? where id = ?";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setString(1,data);
			preparedStatement.setLong(2,messageId);
			preparedStatement.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Error during edit draft message. Please go back and try again. Error details: " + e.getMessage());
		}
	}	
	public void updateMessageFolder(long userId,Long messageId,byte folderId) throws Exception {
		String query = "update MessageFolders set folder_id=? where user_id = ? and message_id = ?";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setByte(1,folderId);
			preparedStatement.setLong(2,userId);
			preparedStatement.setLong(3,messageId);
			preparedStatement.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Error during send message. Please go back and try again. Error details: " + e.getMessage());
		}
	}
	public void separateRecipientId(String emails[],Set<Long> validRecipientIds,Set<User> unregisteredRecipientIds) throws Exception {
		for(String email:emails){
			User user = UserDatabase.userExists(email);
			long userId;
			if(user==null){
				user = UserDatabase.setUser(email);
				if(Validator.isValidSmail(email,false)) {
					unregisteredRecipientIds.add(user);
				}
			}else {
				userId = user.getUserId();
				if(!UserDatabase.isRegisteredUser(userId) && Validator.isValidSmail(email,false)) {
					unregisteredRecipientIds.add(user);
				}else {
					validRecipientIds.add(userId);
				}
			}
		}
	}
	public void setRecipientIds(String emails[],Set<Long> validRecipientIds) throws Exception{
		for(String email:emails){
			email = email.trim();
			User user = UserDatabase.userExists(email);
			if(user==null){
				user = UserDatabase.setUser(email);
			}	
			validRecipientIds.add(user.getUserId());
		}
	}
	public Long setMessage(long id,String subject,String description) throws Exception{
		String query = "insert into Messages(sender_id,subject,description) values(?,?,?)";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){
			preparedStatement.setLong(1,id);
			preparedStatement.setString(2,subject);
			preparedStatement.setString(3,description);
			preparedStatement.executeUpdate();
			ResultSet resultSet = preparedStatement.getGeneratedKeys();
			if(resultSet.next()){
				long messageId = resultSet.getInt(1);
				return messageId;
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to compose message. Please try again later. Error details: "+ e.getMessage());
		}
		return null;
	}
	public void setRecipient(long message_id,long user_id,byte type) throws Exception{
		String query = "insert into Recipients(message_id,user_id,type_id) values(?,?,?)";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,message_id);
			preparedStatement.setLong(2,user_id);
			preparedStatement.setByte(3,type);
			preparedStatement.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to create message. Please try again later. Error details: "+ e.getMessage());
		}
	}
	public void setMessageFolder(long user_id,long message_id,byte folderId) throws Exception{
		String query = "insert into MessageFolders(user_id,folder_id,message_id) values(?,?,?)";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,user_id);
			preparedStatement.setLong(2,folderId);
			preparedStatement.setLong(3,message_id);
			preparedStatement.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to create message. Please try again later. Error details: "+ e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static JSONArray getMessages(String folderName) throws Exception{
		StringBuilder queryBuilder = new StringBuilder(BASE_QUERY);
		if(!Folder.getStarredName().equals(folderName)) {
			queryBuilder.append(" AND f.name = ? ");
		}
		else if(Folder.getStarredName().equals(folderName)) {
			queryBuilder.append(" AND mf.is_starred = true ");
		}
		queryBuilder.append(GROUP_BY);
		Connection connection = DBConnection.getConnection();
		JSONArray messages = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())){
			preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
			if(!"starred".equals(folderName)) {
				preparedStatement.setString(2,folderName);
			}
			try(ResultSet resultSet = preparedStatement.executeQuery()){
				if(resultSet.isBeforeFirst()) {
					messages = new JSONArray();
				}else {
					return messages;
				}
				while(resultSet.next()){
					long messageId = resultSet.getInt("id");
					String from = resultSet.getString("sender_email");
					String to = resultSet.getString("to_recipients");
		            String cc = resultSet.getString("cc_recipients");
		            String subject = resultSet.getString("subject");
		            String description = resultSet.getString("description");
		            boolean isRead = resultSet.getBoolean("is_read");
		            boolean isStarred = resultSet.getBoolean("is_starred");
		            boolean hasAttachment = resultSet.getBoolean("has_attachment");
		            Timestamp createdTime = resultSet.getTimestamp("created_time");
					
		            JSONObject message = new JSONObject();
		            message.put("id", messageId);
		            message.put("from", from);
		            message.put("to", to);
		            message.put("cc", cc);
		            message.put("subject", subject);
		            message.put("description", description);
		            message.put("is_read", isRead);
		            message.put("is_starred", isStarred);
		            message.put("has_attachment", hasAttachment);
		            message.put("created_time", createdTime.toString());
		            
		            messages.add(message); 
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to show message. Please try again later. Error details: "+ e.getMessage());
		}
        return messages;
	}	
	
	public List<Message> getSearchedMessages(String folderName,String searchedkeyword) throws Exception{
		StringBuilder queryBuilder = new StringBuilder(BASE_QUERY);
		if(!Folder.getStarredName().equals(folderName)) {
			queryBuilder.append(" AND f.name = ? ");
		}
		else if(Folder.getStarredName().equals(folderName)) {
			queryBuilder.append(" AND mf.is_starred = true ");
		}
		queryBuilder.append(" AND (m.subject LIKE ? OR m.description LIKE ?) ");
		queryBuilder.append(GROUP_BY);
		Connection connection  = DBConnection.getConnection();
		List<Message> messages=null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())){
			byte index = 1;
			preparedStatement.setLong(index++,UserDatabase.getCurrentUser().getUserId());
			if(!Folder.getStarredName().equals(folderName)) {
				preparedStatement.setString(index++,folderName);
			}
			preparedStatement.setString(index++,"%"+searchedkeyword+"%");
			preparedStatement.setString(index++,"%"+searchedkeyword+"%");
			try(ResultSet resultSet = preparedStatement.executeQuery()){
				if(resultSet.isBeforeFirst()) {
					messages = new ArrayList<>();
				}else {
					System.out.println(MESSAGE_NOT_FOUND);
					return messages;
				}
				while(resultSet.next()){
					long messageId = resultSet.getInt("id");
					String from = resultSet.getString("sender_email");
					String to = resultSet.getString("to_recipients");
		            String cc = resultSet.getString("cc_recipients");
		            String subject = resultSet.getString("subject");
		            String description = resultSet.getString("description");
		            boolean isRead = resultSet.getBoolean("is_read");
		            boolean isStarred = resultSet.getBoolean("is_starred");
		            boolean hasAttachment = resultSet.getBoolean("has_attachment");
		            Timestamp createdTime = resultSet.getTimestamp("created_time");
					
		            messages.add(new Message(messageId,from,to,cc,subject,description,isRead,isStarred,hasAttachment,createdTime));
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to show message. Please try again later. Error details: "+ e.getMessage());
		}
		return messages;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getMessage(String folderName,Long messageId) throws Exception{
		
		StringBuilder queryBuilder = new StringBuilder(BASE_QUERY);
		queryBuilder.append(" AND f.name = ? ");
		queryBuilder.append(" AND mf.message_id = ? ");
		queryBuilder.append(GROUP_BY);
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())){
			preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
			preparedStatement.setString(2,folderName);
			preparedStatement.setLong(3,messageId);
			try(ResultSet resultSet = preparedStatement.executeQuery()){
			
				if(resultSet.next()){
					String from = resultSet.getString("sender_email");
					String to = resultSet.getString("to_recipients");
		            String cc = resultSet.getString("cc_recipients");
		            String subject = resultSet.getString("subject");
		            String description = resultSet.getString("description");
		            boolean isRead = resultSet.getBoolean("is_read");
		            boolean isStarred = resultSet.getBoolean("is_starred");
		            boolean hasAttachment = resultSet.getBoolean("has_attachment");
		            Timestamp createdTime = resultSet.getTimestamp("created_time");
		            
		            JSONObject message = new JSONObject();
		            message.put("id", messageId);
		            message.put("from", from);
		            message.put("to", to);
		            message.put("cc", cc);
		            message.put("subject", subject);
		            message.put("description", description);
		            message.put("is_read", isRead);
		            message.put("is_starred", isStarred);
		            message.put("has_attachment", hasAttachment);
		            message.put("created_time", createdTime.toString());
		            
					return message;
				}else
					System.out.println("Message id not found");
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to show message. Please try again later. Error details: "+ e.getMessage());
		}
		return null;
	}
	
	public void changeMessageFolderId(Long user_id,long message_id,byte oldFolderId,byte binFolderId) throws Exception{
		String query = "update MessageFolders set folder_id=? where user_id = ? and message_id = ? and folder_id=?";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setInt(1,binFolderId);
			preparedStatement.setLong(2,user_id);
			preparedStatement.setLong(3,message_id);
			preparedStatement.setInt(4,oldFolderId);
			int rowsCount = preparedStatement.executeUpdate();
			if(rowsCount>0) {
				System.out.println("The message has been moved to the bin folder.");
			}else {
				System.out.println("\033[31m"+"Message not found"+"\033[0m");
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to delete message. Please try again later. Error details: "+ e.getMessage());
		}
	}
	
	public void deleteMessage(long message_id,byte folderId) throws Exception{
		String query = "delete from MessageFolders where user_id = ? and message_id = ? and folder_id = ?";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
			preparedStatement.setLong(2,message_id);
			preparedStatement.setByte(3,folderId);
			int rowsCount = preparedStatement.executeUpdate();
			if(rowsCount>0) {
				System.out.println("Message deleted successfully.");
			}else {
				System.out.println("\033[31m"+"Message not found"+"\033[0m");
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to delete message. Please try again later. Error details: "+ e.getMessage());
		}
	}

	public void setStarredMessage(long message_id,byte folderId) throws Exception {
		String query = "update MessageFolders set is_starred= not is_starred where user_id = ? and message_id = ? and folder_id=?";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
			preparedStatement.setLong(2,message_id);
			preparedStatement.setLong(3,folderId);
			int rowsCount = preparedStatement.executeUpdate();
			if(rowsCount>0)
				System.out.println("The message has been changed.");
			else
				throw new MessageNotFoundException("\033[31m"+"Message not found"+"\033[0m");
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to starred message. Please try again later. Error details: "+ e.getMessage());
		}
	}
	
	/*public void sendMessage(String to,String cc,String subject,String description,String attachmentPaths,Message message){
	Long messageId = null;
	boolean messageExists = message==null?false:true;
	if(messageExists) {
		messageId = message.getMessageId();
	}
	boolean toEdited = true, ccEdited = true, subjectEdited = true, descriptionEdited = true;
	if(!to.isEmpty()){
		String toEmails[] = to.split(",");
		if(toEmails.length!=0){
			if(Validator.isValidEmails(toEmails)){
				boolean isEmptyCC = cc.isEmpty(),isValidCC = true;
				String ccEmails[] = new String[0];
				if(!isEmptyCC) {
					if(cc.matches("^[,]+$")) {
						isValidCC = false;
					}else {
						ccEmails = cc.split(",");
						isValidCC = Validator.isValidEmails(ccEmails);
					}
				}
				Long userId = null;
				if(isValidCC) {
					userId = currentUser.getUserId();
					if(!messageExists) {
						messageId = setMessage(userId,subject,description);
					}else {
						if(message!=null) {
							if(to.equals(message.getTo())) {
								toEdited=false;
							}else {
								deleteRecipient(messageId,RecipientType.getRecipientType("to"));
							}
							if(cc.equals(message.getCc())) {
								ccEdited=false;
							}else {
								deleteRecipient(messageId,RecipientType.getRecipientType("cc"));
							}
							
							if(subject.equals(message.getSubject())) {
								subjectEdited = false;
							}
							else {
								updateSubjectField(messageId,subject);
							}
							if(description.equals(message.getDescription())) {
								descriptionEdited = false;
							}else {
								updateDescriptionField(messageId,description);
							}
						}
					}
					
					System.out.println("1.Send\n2.Draft");
					InputHandler inputHandler = new InputHandler();
					Byte choice = inputHandler.readByte(CHOICE);
					if(SEND==choice) {
						if(!messageExists) {
							setMessageFolder(userId,messageId,Folder.getSentName());
						}else {
							updateMessageFolder(userId,messageId,Folder.getSentName());
						}
						String folderName;
						if(SpamChecker.isSpam(subject+" "+description)) 
							folderName = Folder.getSpamName();
						else
							folderName = Folder.getInboxName();
						
						if(toEdited || (ccEdited && !isEmptyCC)) {
							validToRecipientIds = new HashSet<>();
							validCcRecipientIds = new HashSet<>();
							unregisteredToRecipients = new HashSet<>();
							unregisteredCcRecipients = new HashSet<>();
						}
						
						if(toEdited) {
							separateRecipientId(toEmails,validToRecipientIds,unregisteredToRecipients);
							for(Long UserId:validToRecipientIds){
								if(!messageExists) {
									setRecipient(messageId,UserId,RecipientType.getRecipientType("to"));
								}
								setMessageFolder(UserId,messageId,folderName);
							}
							if(!messageExists) {
								for(User user:unregisteredToRecipients){
									setRecipient(messageId,user.getUserId(),RecipientType.getRecipientType("to"));
								}
							}
						}
						if(ccEdited && !isEmptyCC) {
							separateRecipientId(ccEmails,validCcRecipientIds,unregisteredCcRecipients);
							for(Long UserId:validCcRecipientIds){
								if(!validToRecipientIds.contains(UserId)) {//To reduce duplicate entries in the MessageFolders table, ensure that both the 'To' and 'Cc' fields contain the same email address.
									setMessageFolder(UserId,messageId,folderName);
								}
								if(!messageExists) {
									setRecipient(messageId,UserId,RecipientType.getRecipientType("cc"));
								}
							}
							if(!messageExists) {
								for(User user:unregisteredCcRecipients){
									if(!unregisteredToRecipients.contains(user.getUserId())) {//To reduce duplicate entries in the MessageFolders table, ensure that both the 'To' and 'Cc' fields contain the same email address.
										setRecipient(messageId,user.getUserId(),RecipientType.getRecipientType("cc"));
									}
								}
							}
						}
						
						/*if(!attachmentPaths.isEmpty()) {
							for(String attachmentPath: attachmentPaths.split(",")) {
								Path filePath = Paths.get(attachmentPath);
								if(Files.exists(filePath)) {
									Path destinationFilePath = Paths.get("D:\\Sakthi\\Eclipse Project\\Attachments\\"+messageId);
									try {
										Files.createDirectories(destinationFilePath);
										
										LocalDateTime now = LocalDateTime.now();
								        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
								        String timestamp = now.format(formatter);
										System.out.println(timestamp);
										
									} catch (IOException e) {
										
										e.printStackTrace();
									}
								}
							}
						}*//*
						
						System.out.println("\u001B[32m"+"Message sent successfully.\n"+"\u001B[0m");
						if(!unregisteredToRecipients.isEmpty()) {
							System.out.print("To Address not found: ");
							for(User user:unregisteredToRecipients) {
								System.out.print(user.getEmail()+", ");
							}
							System.out.println();
						}if(!unregisteredCcRecipients.isEmpty()) {
							System.out.print("CC Address not found: ");
							for(User user:unregisteredCcRecipients) {
								System.out.print(user.getEmail()+", ");
							}
							System.out.println();
						}
					}else if(DRAFT==choice) {
						if(!messageExists) {
							setMessageFolder(userId,messageId,Folder.getDraftName());
						}
						if(toEdited || (ccEdited && !isEmptyCC)) {
							validToRecipientIds = new HashSet<>();
							validCcRecipientIds = new HashSet<>();
							unregisteredToRecipients = new HashSet<>();
							unregisteredCcRecipients = new HashSet<>();
						}
						if(toEdited) {
							separateRecipientId(toEmails,validToRecipientIds,unregisteredToRecipients);
							for(Long id:validToRecipientIds){
								setRecipient(messageId,id,RecipientType.getRecipientType("to"));
							}
							for(User user:unregisteredToRecipients){
								setRecipient(messageId,user.getUserId(),RecipientType.getRecipientType("to"));
							}
						}
		
						if(ccEdited && !isEmptyCC) {
							separateRecipientId(ccEmails,validCcRecipientIds,unregisteredCcRecipients);
							for(Long id:validCcRecipientIds){
								setRecipient(messageId,id,RecipientType.getRecipientType("cc"));
							}
							for(User user:unregisteredCcRecipients){
								setRecipient(messageId,user.getUserId(),RecipientType.getRecipientType("cc"));
							}
						}
						System.out.println("\u001B[32m"+"Message drafted successfully.\n"+"\u001B[0m");
					}else
						System.out.println("\033[31m"+"Invalid choice.Please try again..."+"\033[0m");
				}else
					System.out.println("\033[31m"+"CC is invalid."+"\033[0m");
			}
		}else
			System.out.println("\033[31m"+to+" is invalid"+"\033[0m");
	}else
		System.out.println("\033[31m"+"To address is empty."+"\033[0m");
}*/
	
	
	/*public void sendMessage(Message message) {
	setMessageFolder(currentUser.getUserId(),message.getMessageId(),Folder.getSentName());
	
	String folderName;
	if(SpamChecker.isSpam(message.getSubject()+" "+message.getDescription())) 
		folderName = Folder.getSpamName();
	else
		folderName = Folder.getInboxName();
	
	String toEmails[] = message.getTo().split(",");
	for(Long UserId:validToRecipientIds){
		setMessageFolder(UserId,message.getMessageId(),folderName);
	}
	String ccEmails[] = message.getCc().split(",");
	for(Long UserId:validCcRecipientIds){
		if(!validToRecipientIds.contains(UserId)) {//To reduce duplicate entries in the MessageFolders table, ensure that both the 'To' and 'Cc' fields contain the same email address.
			setMessageFolder(UserId,message.getMessageId(),folderName);
		}
	}
	
	System.out.println("\u001B[32m"+"Message sent successfully.\n"+"\u001B[0m");
	if(!unregisteredToRecipients.isEmpty()) {
		System.out.print("To Address not found: ");
		for(User user:unregisteredToRecipients) {
			System.out.print(user.getEmail()+", ");
		}
		System.out.println();
	}if(!unregisteredCcRecipients.isEmpty()) {
		System.out.print("CC Address not found: ");
		for(User user:unregisteredCcRecipients) {
			System.out.print(user.getEmail()+", ");
		}
		System.out.println();
	}
}*/

/*public void sendMessage(String to,String cc,String subject,String description,String attachmentPaths){
	if(!to.isEmpty()){
		String toEmails[] = to.split(",");
		if(toEmails.length!=0){
			if(Validator.isValidEmails(toEmails)){
				boolean isEmptyCC = cc.isEmpty(),isValidCC = true;
				String ccEmails[] = new String[0];
				if(!isEmptyCC) {
					if(cc.matches("^[,]+$")) {
						isValidCC = false;
					}else {
						ccEmails = cc.split(",");
						isValidCC = Validator.isValidEmails(ccEmails);
					}
				}
				Long messageId = null;
				if(isValidCC) {
					messageId = setMessage(currentUser.getUserId(),subject,description);
					setMessageFolder(currentUser.getUserId(),messageId,Folder.getSentName());
					
					String folderName;
					if(SpamChecker.isSpam(subject+" "+description)) 
						folderName = Folder.getSpamName();
					else
						folderName = Folder.getInboxName();
				
					validToRecipientIds = new HashSet<>();
					validCcRecipientIds = new HashSet<>();
					unregisteredToRecipients = new HashSet<>();
					unregisteredCcRecipients = new HashSet<>();
				
					separateRecipientId(toEmails,validToRecipientIds,unregisteredToRecipients);
					for(Long UserId:validToRecipientIds){
							setRecipient(messageId,UserId,RecipientType.getRecipientType("to"));
						setMessageFolder(UserId,messageId,folderName);
					}
					for(User user:unregisteredToRecipients){
						setRecipient(messageId,user.getUserId(),RecipientType.getRecipientType("to"));
					}
					separateRecipientId(ccEmails,validCcRecipientIds,unregisteredCcRecipients);
					for(Long UserId:validCcRecipientIds){
						if(!validToRecipientIds.contains(UserId)) {//To reduce duplicate entries in the MessageFolders table, ensure that both the 'To' and 'Cc' fields contain the same email address.
							setMessageFolder(UserId,messageId,folderName);
						}
							setRecipient(messageId,UserId,RecipientType.getRecipientType("cc"));
					}
					for(User user:unregisteredCcRecipients){
						if(!unregisteredToRecipients.contains(user.getUserId())) {//To reduce duplicate entries in the MessageFolders table, ensure that both the 'To' and 'Cc' fields contain the same email address.
							setRecipient(messageId,user.getUserId(),RecipientType.getRecipientType("cc"));
						}
					}
					
					System.out.println("\u001B[32m"+"Message sent successfully.\n"+"\u001B[0m");
					if(!unregisteredToRecipients.isEmpty()) {
						System.out.print("To Address not found: ");
						for(User user:unregisteredToRecipients) {
							System.out.print(user.getEmail()+", ");
						}
						System.out.println();
					}if(!unregisteredCcRecipients.isEmpty()) {
						System.out.print("CC Address not found: ");
						for(User user:unregisteredCcRecipients) {
							System.out.print(user.getEmail()+", ");
						}
						System.out.println();
					}
				}else
					System.out.println("\033[31m"+"CC is invalid."+"\033[0m");
			}
		}else
			System.out.println("\033[31m"+to+" is invalid"+"\033[0m");
	}else
		System.out.println("\033[31m"+"To address is empty."+"\033[0m");
}*/
	
	
	/*public boolean isMessageFound(long messageId, long userId, String folderName) {
		String query;
		if(folderName.equals(Folder.getStarredName())) {
			query = "select Exists(select 1 from MessageFolders where message_id=? and user_id=? and is_starred=true limit 1)";
		}else {
			query = "select Exists(select 1 from MessageFolders where message_id=? and user_id=? and folder_id=? limit 1)";
		}
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,messageId);
			preparedStatement.setLong(2,userId);
			if(!folderName.equals(Folder.getStarredName())) {
				preparedStatement.setByte(3,Folder.getFolderId(folderName));
			}
			try(ResultSet resultSet = preparedStatement.executeQuery()){
				if(resultSet.next()){
					return resultSet.getBoolean(1);
				}else
					System.out.println("\033[31m"+"Message not found"+"\033[0m");
			}
		}catch(Exception e){
			System.out.println("An unexpected error occurred. Please try again later.");
			e.printStackTrace();
		}
		return false;
	}*/
	
}
