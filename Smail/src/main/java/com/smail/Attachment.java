package com.smail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class Attachment {
	
	private static Map<String,Byte> fileTypes = new HashMap<>();
	
	public static Map<String, Byte> getFileTypes() {
		return fileTypes;
	}

	public static void loadFileTypes() throws Exception {
		String query = "select * from FileTypes";
		Connection connection = null;
		try {
			connection = DBConnection.getConnection();
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error during send message. Please go back and try again. Error details: " + e.getMessage());
		}
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			try(ResultSet resultSet = preparedStatement.executeQuery()){
				while(resultSet.next()) {
					byte id = resultSet.getByte("id");
					String type = resultSet.getString("type");	
					fileTypes.put(type, id);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("Error during send message. Please go back and try again. Error details: " + e.getMessage());
		}
	}
}
