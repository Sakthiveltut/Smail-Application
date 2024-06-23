package email;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import custom_exception.AuthenticationFailedException;
import custom_exception.EmailAlreadyExistsException;

public class Authentication {
	
	public static boolean signUp(String name,String email,String password) throws Exception{
		if(Validator.isValidName(name)){
			if(Validator.isValidEmail(email)) {
				UserDatabase userDatabase = new UserDatabase();
				User user = userDatabase.userExists(email);
				if(user==null && Validator.isValidPassword(password)){
					user = userDatabase.setUser(email);
					userDatabase.setUserDetails(user.getUserId(),name,password);
					Folder.assignDefaultFolders(user.getUserId());
					System.out.println("\u001B[32m"+"Account created successfully."+"\u001B[0m");
					return true;
				}else if(!userDatabase.isRegisteredUser(user.getUserId())) {
					userDatabase.setUserDetails(user.getUserId(), name, password);
					System.out.println("\u001B[32m"+"Account created successfully."+"\u001B[0m");
					return true;
				}else
					throw new EmailAlreadyExistsException("That email id is taken.Try another.");
			}
		}	
		return false;
	}
	
	public static boolean signIn(String email,String password) throws Exception{
		if(Validator.isValidSmail(email)){
			UserDatabase userDatabase = new UserDatabase();
			User user = userDatabase.userExists(email);
			if(user!=null && user.getPassword()!=null && user.getPassword().equals(password)) {
				UserDatabase.setCurrentUser(user);
				UserDatabase.updateLoginTime();
				System.out.println("\u001B[32m"+"Login successfully."+"\u001B[0m");
				return true;
			}else {
				throw new AuthenticationFailedException("Invalid username or password.");
			}				
		}
		return false;
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
