package com.smail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


public class UserDatabase {
	
	private static ThreadLocal<User> currentUser = new ThreadLocal<User>();
	
	public static User getCurrentUser() {
		return currentUser.get();
	}
	
	public static void setCurrentUser(User user) {
		currentUser.set(user);
	}
	
	public static void clearCurrentUser() {
		currentUser.remove();
	}
	
	public static User userExists(String email) throws Exception{
		String query = "select id,name,password,login_time from Users u left join RegisteredUsers ru  on u.id = ru.user_id where u.email = ?";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setString(1,email);
			try(ResultSet resultSet = preparedStatement.executeQuery()){
				if(resultSet.next()){
					long userId = resultSet.getLong("id");
					String name = resultSet.getString("name");
					String password = resultSet.getString("password");
					String lastLoginTime = resultSet.getString("login_time");
					return new User(userId,name,email,password,lastLoginTime);
				}
			}
		}catch(Exception e){			
			e.printStackTrace();
			throw new Exception("An unexpected error occurred. Please try again later. Error details: "+ e.getMessage());
		}
		return null;
	}
	
	/*public Long userExists(String email){
		String query = "select id from Users where email=?";
		try(Connection connection  = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setString(1,email);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()){
				Long id = resultSet.getLong("id");
				return id;
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}*/
	public static boolean isRegisteredUser(Long userId) throws Exception {
		String query = "select 1 from RegisteredUsers where user_id=? limit 1";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,userId);
			try(ResultSet resultSet = preparedStatement.executeQuery()){
				if(resultSet.next()){
					return true;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An unexpected error occurred. Please try again later. Error details: "+ e.getMessage());
		}
		return false;
	}
	public static User setUser(String email) throws Exception{
		String query = "insert into Users(email) values(?)";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){
			preparedStatement.setString(1,email);
			preparedStatement.executeUpdate();
			try(ResultSet resultSet = preparedStatement.getGeneratedKeys()){
				if(resultSet.next()){
					long id = resultSet.getLong(1);
					return new User(id, email);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to create user. Please try again later. Error details: "+ e.getMessage());
		}
		return null;
	}
	public static void setUserDetails(Long user_id,String name,String password) throws Exception {
		String query = "insert into RegisteredUsers(user_id,name,password) values(?,?,?)";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,user_id);
			preparedStatement.setString(2,name);
			preparedStatement.setString(3,password);
			preparedStatement.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to create user. Please try again later. Error details: "+ e.getMessage());
		}
	}
	public static void updateLoginTime(long userId) throws Exception{
		String query = "update RegisteredUsers set login_time=now() where user_id=?";
		Connection connection  = DBConnection.getConnection();
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,userId);
			preparedStatement.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An error occurred while trying to update time. Please try again later. Error details: "+ e.getMessage());
		}
	}
}
