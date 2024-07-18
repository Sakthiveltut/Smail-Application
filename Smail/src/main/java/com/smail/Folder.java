package com.smail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


public class Folder {
	
	private static Map<String,Byte> folders = new HashMap<String,Byte>();
	
	private static final String INBOX = "inbox", STARRED = "starred", SENT = "sent", UNREAD = "unread", DRAFT = "draft", SPAM = "spam", BIN = "bin";
	
	public static String getInboxName() {
		return INBOX;
	}

	public static String getStarredName() {
		return STARRED;
	}

	public static String getSentName() {
		return SENT;
	}

	public static String getUnreadName() {
		return UNREAD;
	}

	public static String getDraftName() {
		return DRAFT;
	}

	public static String getSpamName() {
		return SPAM;
	}

	public static String getBinName() {
		return BIN;
	}

	public static byte getFolderId(String folderName) {
		return folders.get(folderName);
	}
	
	public static Map<String,Byte> getFolders() {
		return folders;
	}
	
	public static void setFolders() throws Exception{
		String query = "select id,name from Folders";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery()){
			while(resultSet.next()){
				byte id = resultSet.getByte("id");
				String name = resultSet.getString("name");
				folders.put(name,id);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An unexpected error occurred. Please try again later. Error details: "+ e.getMessage());
		}
	}
	public static void assignDefaultFolders(long id) throws Exception{
		String query = "insert into UserFolders(user_id,folder_id) values(?,?)";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			for(Byte folderId:folders.values()){
				preparedStatement.setLong(1,id);
				preparedStatement.setInt(2,folderId);
				preparedStatement.executeUpdate();
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to create account. Please try again later. Error details: "+ e.getMessage());
		}
	}
}