package com.smail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import org.json.simple.JSONObject;
import org.apache.catalina.tribes.util.Arrays;
import org.json.simple.JSONArray;

import com.smail.custom_exception.InvalidEmailException;
import com.smail.custom_exception.InvalidInputException;
import com.smail.custom_exception.MessageNotFoundException;

public class MessageOperation {
	
	private String to,cc,subject,description;
	private Set<Long> validToRecipientIds,validCcRecipientIds;
	private Set<User> unregisteredToRecipients,unregisteredCcRecipients;
	
	private static final String MESSAGE_NOT_FOUND = "\033[31m"+"Message not found."+"\033[0m";
	
	private final static String BASE_QUERY = """
			SELECT 
			    m.id,
			    su.email AS sender_email,
			    m.subject,
			    m.description,
			    mf.is_read,
			 	mf.is_starred,
			    m.created_time,
			    m.has_attachment,
			    GROUP_CONCAT(DISTINCT CASE WHEN rt.type = 'to' THEN u.email END SEPARATOR ', ') AS to_recipients,
			    GROUP_CONCAT(DISTINCT CASE WHEN rt.type = 'cc' THEN u.email END SEPARATOR ', ') AS cc_recipients,
			    GROUP_CONCAT(a.id SEPARATOR ', ') AS attachment_ids,
			    GROUP_CONCAT(a.name SEPARATOR ', ') AS attachment_names,
			    GROUP_CONCAT(a.path SEPARATOR ', ') AS attachment_paths,
			    GROUP_CONCAT(ft.type SEPARATOR ', ') AS attachment_types
			FROM 
			    Messages m
			LEFT JOIN 
			    Recipients r ON m.id = r.message_id
			LEFT JOIN 
			    Users u ON r.user_id = u.id
			LEFT JOIN 
			    RecipientTypes rt ON r.type_id = rt.id
			LEFT JOIN 
				Attachments a ON m.id = a.message_id
			LEFT JOIN 
			 	FileTypes ft ON a.type_id = ft.id
			JOIN 
			    MessageFolders mf ON m.id = mf.message_id
			JOIN 
			    Users su ON m.sender_id = su.id
			JOIN 
			    Folders f ON mf.folder_id = f.id
			WHERE 
			    mf.user_id = ?   
			""";
	
	private final static String GROUP_BY = " GROUP BY m.id, su.email, m.subject, m.description, mf.is_read, mf.is_starred, m.created_time, m.has_attachment";
	private final static String ORDER_BY = " ORDER BY m.created_time DESC;";
	
	public void updateDraftMessage(JSONObject originalMessage) throws Exception {
		if(isValidMessage()) {
			if(!originalMessage.get("to").equals(to)) {
				deleteRecipient((long)originalMessage.get("id"),RecipientType.getRecipientType("to"));
				setToRecipients((long)originalMessage.get("id"));
			}
			if(!(originalMessage.get("cc")==null && cc==null) || (originalMessage.get("cc")!=null && !originalMessage.get("cc").equals(cc))) {
				deleteRecipient((long)originalMessage.get("id"),RecipientType.getRecipientType("cc"));
				setCcRecipients((long)originalMessage.get("id"));
			}
			if(!originalMessage.get("subject").equals(subject)) {
				updateSubjectField((long)originalMessage.get("id"),subject);
			}
			if(!originalMessage.get("description").equals(description)) {
				updateDescriptionField((long)originalMessage.get("id"),description);
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
	
	private boolean isValidMessage() throws InvalidInputException {
		if(to!=null && !to.isEmpty()){
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
	
	private void setToRecipients(long messageId) throws Exception {
		String toEmails[] = to.split(",");
		validToRecipientIds = new HashSet<>();	
		setRecipientIds(toEmails,validToRecipientIds);
		for(Long UserId:validToRecipientIds){
			setRecipient(messageId,UserId,RecipientType.getRecipientType("to"));
		}
	}
	private void setCcRecipients(long messageId) throws Exception {
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
			String unregisteredEmails="";
			if(!unregisteredToRecipients.isEmpty()) {
				for(User user:unregisteredToRecipients) {
					unregisteredEmails+=user.getEmail()+", ";
				}
				throw new InvalidEmailException("To Address not found: "+unregisteredEmails);
			}if(!unregisteredCcRecipients.isEmpty()) {
				unregisteredEmails="";
				System.out.print("CC Address not found: ");
				for(User user:unregisteredCcRecipients) {
					unregisteredEmails+=user.getEmail()+", ";
				}
				throw new InvalidEmailException("Cc Address not found: "+unregisteredEmails);
			}
		}
	}
	
	private void deleteRecipient(Long messageId,byte recipientType) throws Exception {
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
	
	public void deleteAttachment(long attachmentId,long messageId) throws Exception {	
		String query = """
				DELETE A
				FROM Attachments A
				JOIN MessageFolders MF ON A.message_id = MF.message_id
				WHERE MF.user_id = ?
				  AND A.message_id = ?
				  AND A.id = ?
				  AND MF.folder_id = ?;
				""";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
			preparedStatement.setLong(2,messageId);
			preparedStatement.setLong(3,attachmentId);
			preparedStatement.setLong(4,Folder.getFolderId(Folder.getDraftName()));
			int rowsCount = preparedStatement.executeUpdate();
			if(rowsCount>0) {
				changeAttachmentStatus(messageId);
			}else {
				throw new Exception("Error during delete attachment. Please go back and try again.");				
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Error during send message. Please go back and try again. Error details: " + e.getMessage());
		}
	}
	
	private void changeAttachmentStatus(long messageId) throws Exception {
		String query = """
					UPDATE Messages M
					SET M.has_attachment = (
					    SELECT CASE
					               WHEN EXISTS (
					                   SELECT 1
					                   FROM Attachments A
					                   JOIN MessageFolders MF ON A.message_id = MF.message_id
					                   WHERE MF.message_id = M.id
					                     AND MF.user_id = ?
					                     AND MF.folder_id = ?
					               )
					                   THEN true
					               ELSE false
					           END
					)
					WHERE M.id = ?;
				""";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
			preparedStatement.setLong(2,Folder.getFolderId(Folder.getDraftName()));
			preparedStatement.setLong(3,messageId);
			preparedStatement.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Error during send message. Please go back and try again. Error details: " + e.getMessage());
		}
	}

	private void updateSubjectField(Long messageId,String data) throws Exception {
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
	private void updateDescriptionField(Long messageId,String data) throws Exception {
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
	private void updateMessageFolder(long userId,Long messageId,byte folderId) throws Exception {
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
	public String fetchAttachmentPath(long attachmentId,long messageId) throws Exception {	
		String query = """
				SELECT 
				    a.path 
				FROM 
				    Attachments a
				JOIN 
				    MessageFolders mf ON a.message_id = mf.message_id
				WHERE 
				    mf.user_id = ? 
				    AND mf.message_id = ? 
				    AND a.id = ?;
				""";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
			preparedStatement.setLong(2,messageId);
			preparedStatement.setLong(3,attachmentId);
			try(ResultSet resultSet = preparedStatement.executeQuery()){
				if(resultSet.next()) {
					String path = resultSet.getString("path");
					return path;					
				}else {
					throw new Exception("Attachment not found");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Error during send message. Please go back and try again. Error details: " + e.getMessage());
		}
	}
	private void separateRecipientId(String emails[],Set<Long> validRecipientIds,Set<User> unregisteredRecipientIds) throws Exception {
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
	private void setRecipientIds(String emails[],Set<Long> validRecipientIds) throws Exception{
		for(String email:emails){
			email = email.trim();
			User user = UserDatabase.userExists(email);
			if(user==null){
				user = UserDatabase.setUser(email);
			}	
			validRecipientIds.add(user.getUserId());
		}
	}
	private Long setMessage(long id,String subject,String description) throws Exception{
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
	private void setRecipient(long message_id,long user_id,byte type) throws Exception{
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
		if(Folder.getStarredName().equals(folderName)) {
			queryBuilder.append(" AND mf.is_starred = true AND f.name !='bin'");
		}else if("unread".equals(folderName)) {
			queryBuilder.append(" AND mf.is_read = false AND f.name ='inbox' ");
		}else {
			queryBuilder.append(" AND f.name = ? ");
		}
		queryBuilder.append(GROUP_BY);
		queryBuilder.append(ORDER_BY);
		Connection connection = DBConnection.getConnection();
		JSONArray messages = null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())){
			preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
			if(!"starred".equals(folderName) && !"unread".equals(folderName)) {
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
		            
		            System.out.println(messages);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to show message. Please try again later. Error details: "+ e.getMessage());
		}
        return messages;
	}	
	
	@SuppressWarnings("unchecked")
	public static JSONArray getSearchedMessages(String folderName,String searchedkeyword) throws Exception{
		System.out.println("searchedkeyword"+searchedkeyword);
		
		StringBuilder queryBuilder = new StringBuilder(BASE_QUERY);
		if(Folder.getStarredName().equals(folderName)) {
			queryBuilder.append(" AND mf.is_starred = true ");
		}else if("unread".equals(folderName)) {
			queryBuilder.append(" AND mf.is_read = false ");
		}else {
			queryBuilder.append(" AND f.name = ? ");
		}
		queryBuilder.append(" AND (m.subject LIKE ? OR m.description LIKE ?) ");
		queryBuilder.append(GROUP_BY);
		Connection connection  = DBConnection.getConnection();
		JSONArray messages=null;
		try(PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())){
			byte index = 1;
			preparedStatement.setLong(index++,UserDatabase.getCurrentUser().getUserId());
			if(!Folder.getStarredName().equals(folderName) && !"unread".equals(folderName)) {
				preparedStatement.setString(index++,folderName);
			}
			preparedStatement.setString(index++,"%"+searchedkeyword+"%");
			preparedStatement.setString(index++,"%"+searchedkeyword+"%");
			try(ResultSet resultSet = preparedStatement.executeQuery()){
				if(resultSet.isBeforeFirst()) {
					messages = new JSONArray();
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
	
	@SuppressWarnings("unchecked")
	public JSONObject getMessage(String folderName,Long messageId) throws Exception{
		
		StringBuilder queryBuilder = new StringBuilder(BASE_QUERY);
		if(Folder.getStarredName().equals(folderName)) {
			queryBuilder.append(" AND mf.is_starred = true ");
		}else if("unread".equals(folderName)) {
			queryBuilder.append(" AND mf.is_read = false ");
		}else {
			queryBuilder.append(" AND f.name = ? ");
		}
		queryBuilder.append(" AND mf.message_id = ? ");
		queryBuilder.append(GROUP_BY);
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())){
			byte index=1;
			preparedStatement.setLong(index++,UserDatabase.getCurrentUser().getUserId());
			if(!Folder.getStarredName().equals(folderName) && !"unread".equals(folderName)) {
				preparedStatement.setString(index++,folderName);
			}
			preparedStatement.setLong(index++,messageId);
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
		            
		            if(!isRead) {
		            	setMessageAsRead(messageId,folderName);
		            }
		            
		            JSONObject message = new JSONObject();
		            message.put("id", messageId);
		            message.put("from", from);
		            message.put("to", to);
		            message.put("cc", cc);
		            message.put("subject", subject);
		            message.put("description", description);
		            
		            String attachmentIds = resultSet.getString("attachment_ids");
		            String attachmentNames = resultSet.getString("attachment_names");
		            String attachmentPaths = resultSet.getString("attachment_paths");
		            String attachmentTypes = resultSet.getString("attachment_types");
		            
		            JSONArray attachments = new JSONArray();
		            if(attachmentNames!=null) {
			            String attachmentIdsArr[] = attachmentIds.split(",");
			            String attachmentNamesArr[] = attachmentNames.split(",");
			            String attachmentPathsArr[] = attachmentPaths.split(",");
			            String attachmentTypesArr[] = attachmentTypes.split(",");
			            
			            System.out.println(Arrays.toString(attachmentNamesArr));
			            
			            for(int i=0;i<attachmentNamesArr.length;i++) {
			            	JSONObject attachment = new JSONObject();
			            	attachment.put("id", attachmentIdsArr[i]);		            	
			            	attachment.put("name", attachmentNamesArr[i]);		            	
			            	attachment.put("path", attachmentPathsArr[i]);		            	
			            	attachment.put("type", attachmentTypesArr[i]);	
			            	attachments.add(attachment);
			            }
		            }
		            message.put("attachments", attachments);
		            
		            message.put("is_read", isRead);
		            message.put("is_starred", isStarred);
		            message.put("has_attachment", hasAttachment);
		            message.put("created_time", createdTime.toString());
		            
		            System.out.println("messageDetails "+message);
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
	
	private void setMessageAsRead(long message_id,String folderName) throws Exception{
		String query = null;
		if(Folder.getStarredName().equals(folderName)) {
			query = "update MessageFolders set is_read=true where user_id = ? and message_id = ? and is_starred=true";			
		} else if("unread".equals(folderName)) {
			query = "update MessageFolders set is_read=true where user_id = ? and message_id = ? and is_read=false";			
		}else {			
			query = "update MessageFolders set is_read=true where user_id = ? and message_id = ? and folder_id=?";			
		}
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
			preparedStatement.setLong(2,message_id);
			if(!Folder.getStarredName().equals(folderName) && !"unread".equals(folderName)) {
				preparedStatement.setLong(3,Folder.getFolderId(folderName));
			}
			preparedStatement.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to delete message. Please try again later. Error details: "+ e.getMessage());
		}
	}
	
	public void saveAttachment(long messageId,String fileName,byte typeId,long size,String path) throws Exception{
		String query = "insert into Attachments(message_id,name,type_id,size,path) values(?,?,?,?,?)";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,messageId);
			preparedStatement.setString(2,fileName);
			preparedStatement.setByte(3,typeId);
			preparedStatement.setLong(4,size);
			preparedStatement.setString(5,path);
			preparedStatement.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("An error occurred while trying to add attachments. Please try again later. Error details: "+ e.getMessage());
		}
	}
	
	public void changeMessageFolderId(long message_id,String option,byte binFolderId) throws Exception{
		String query = null;
		if(Folder.getStarredName().equals(option)) {
			query = "update MessageFolders set folder_id=? where user_id = ? and message_id = ? and is_starred=true";
		} else if("unread".equals(option)) {
			query = "update MessageFolders set folder_id=? where user_id = ? and message_id = ? and is_read=false";
		}else {			
			query = "update MessageFolders set folder_id=? where user_id = ? and message_id = ? and folder_id=?";
		}
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setInt(1,binFolderId);
			preparedStatement.setLong(2,UserDatabase.getCurrentUser().getUserId());
			preparedStatement.setLong(3,message_id);
			if(!Folder.getStarredName().equals(option) && !"unread".equals(option)) {
				preparedStatement.setLong(4,Folder.getFolderId(option));
			}			
			int rowsCount = preparedStatement.executeUpdate();
			if(rowsCount>0) {
				System.out.println("The message has been moved to the bin folder.");
			}else {
				throw new Exception("Message not found");
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to delete message. Please try again later. Error details: "+ e.getMessage());
		}
	}
	
	public void deleteMessage(long message_id,String option) throws Exception{
		String query = null;
		if(Folder.getStarredName().equals(option)) {
			query = "delete from MessageFolders where user_id = ? and message_id = ? and is_starred=true ?";
		} else if("unread".equals(option)) {
			query = "delete from MessageFolders where user_id = ? and message_id = ? and is_read=false";
		}else {
			query = "delete from MessageFolders where user_id = ? and message_id = ? and folder_id = ?";			
		}
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
			preparedStatement.setLong(2,message_id);
			if(!Folder.getStarredName().equals(option) && !"unread".equals(option)) {
				preparedStatement.setLong(3,Folder.getFolderId(option));
			}			
			int rowsCount = preparedStatement.executeUpdate();
			if(rowsCount>0) {
				System.out.println("Message deleted successfully.");
			}else {
				throw new Exception("Message not found");
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to delete message. Please try again later. Error details: "+ e.getMessage());
		}
	}

	public void starredMessage(String option,long message_id) throws Exception {
		String query = null;
		if(Folder.getStarredName().equals(option)) {
			query = "update MessageFolders set is_starred= not is_starred where user_id = ? and message_id = ? and is_starred=true";			
		} else if("unread".equals(option)) {
			query = "update MessageFolders set is_starred= not is_starred where user_id = ? and message_id = ? and is_read=false";			
		}else {			
			query = "update MessageFolders set is_starred= not is_starred where user_id = ? and message_id = ? and folder_id=?";
		}
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
			preparedStatement.setLong(2,message_id);
			if(!Folder.getStarredName().equals(option) && !"unread".equals(option)) {
				preparedStatement.setLong(3,Folder.getFolderId(option));
			}
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

	public void updateAttachmentStatus(long messageId) throws Exception {
		String query = "update Messages set has_attachment=true where id = ?";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,messageId);
			preparedStatement.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to delete message. Please try again later. Error details: "+ e.getMessage());
		}
	}
	
	/*private boolean hasAttachment(long messageId) throws Exception {
	String query = """
			SELECT EXISTS (
			    SELECT 1
			    FROM Attachments A
			    JOIN MessageFolders MF ON A.message_id = MF.message_id
			    WHERE MF.user_id = ?
			      AND MF.message_id = ?
			      AND MF.folder_id = ?
			);
			""";
	Connection connection  = DBConnection.getConnection();
	try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
		preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
		preparedStatement.setLong(2,messageId);
		preparedStatement.setLong(3,Folder.getFolderId(Folder.getDraftName()));
		try(ResultSet resultSet = preparedStatement.executeQuery()){
			if(resultSet.next()) {
				return  resultSet.getBoolean(1);
			}else {
				throw new Exception("Attachment not found");
			}
		}
	}catch(Exception e){
		e.printStackTrace();
		throw new Exception("Error during send message. Please go back and try again. Error details: " + e.getMessage());
	}
}*/
	
	/*public void setMessageUnstar(long message_id) throws Exception {
		String query = "update MessageFolders set is_starred=false where user_id = ? and message_id = ? and is_starred=true";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,UserDatabase.getCurrentUser().getUserId());
			preparedStatement.setLong(2,message_id);
			int rowsCount = preparedStatement.executeUpdate();
			if(rowsCount>0)
				System.out.println("The message has been changed.");
			else
				throw new MessageNotFoundException("\033[31m"+"Message not found"+"\033[0m");
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to starred message. Please try again later. Error details: "+ e.getMessage());
		}
	}*/
	
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
