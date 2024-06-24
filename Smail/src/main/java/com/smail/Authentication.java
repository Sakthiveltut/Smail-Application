package com.smail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.smail.custom_exception.AuthenticationFailedException;
import com.smail.custom_exception.EmailAlreadyExistsException;
import com.smail.custom_exception.InvalidInputException;

public class Authentication {
	
	public static boolean signUp(String name,String email,String password) throws InvalidInputException, EmailAlreadyExistsException, Exception{
		if(Validator.isValidName(name)){
			if(Validator.isValidEmail(email)) {
				User user = UserDatabase.userExists(email);
				if(user==null && Validator.isValidPassword(password)){
					user = UserDatabase.setUser(email);
					UserDatabase.setUserDetails(user.getUserId(),name,password);
					Folder.assignDefaultFolders(user.getUserId());
					System.out.println("\u001B[32m"+"Account created successfully."+"\u001B[0m");
					return true;
				}else if(!UserDatabase.isRegisteredUser(user.getUserId())) {
					UserDatabase.setUserDetails(user.getUserId(), name, password);
					System.out.println("\u001B[32m"+"Account created successfully."+"\u001B[0m");
					return true;
				}else
					throw new EmailAlreadyExistsException("That email id is taken.Try another.");
			}
		}	
		return false;
	}
	
	public static User signIn(String email,String password) throws Exception{
		if(Validator.isValidSmail(email)){
			User user = UserDatabase.userExists(email);
			if(user!=null && user.getPassword()!=null && user.getPassword().equals(password)) {
				UserDatabase.updateLoginTime(user.getUserId());
				return user;
			}else {
				throw new AuthenticationFailedException("Invalid username or password.");
			}				
		}
		return null;
	}
	/*public Long signIn(String email,String password){
		if(Validator.isValidSmail(email)){
			String query = "select id,name,login_time from Users u join RegisteredUsers ru  on u.id = ru.user_id where u.email = ? and ru.password = ?";
			try(Connection connection  = DBConnection.getConnection();
					PreparedStatement preparedStatement = connection.prepareStatement(query)){
				preparedStatement.setString(1,email);
				preparedStatement.setString(2,password);
				try(ResultSet resultSet = preparedStatement.executeQuery()){
					if(resultSet.next()){
						long id = resultSet.getInt("id");
						lastLoginTime = resultSet.getString("login_time");
						updateLoginTime(id);
						System.out.println("\u001B[32m"+"Login successfully."+"\u001B[0m");
						return id;
					}
				}
			}
			catch(SQLException e){
				e.printStackTrace();
			}
			System.out.println("\033[31m"+"Invalid username or password."+"\033[0m");
		}
		return null;
	}*/
}
