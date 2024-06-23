package com.smail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class RecipientType {
	
	private static Map<String,Byte> recipientTypes = new HashMap<>();
	
	public static byte getRecipientType(String recipientType) {
		return recipientTypes.get(recipientType);
	}
	
	public static void setRecipientTypes() throws Exception {
		String query = "select * from RecipientTypes";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery()){
			while(resultSet.next()){
				byte id = resultSet.getByte("id");
				String name = resultSet.getString("type");
				recipientTypes.put(name,id);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An unexpected error occurred. Please try again later. Error details: "+ e.getMessage());
		}
	}
}
