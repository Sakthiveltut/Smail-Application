import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.ArrayList;

public class User{
	private static Connection connection = DBConnection.getConnection();
	String lastLoginTime;
	static List<Integer> folderIds;
	
	static{getFolderIds();}
	
	public boolean signUp(String name,String email,String password1,String password2){
		if(Validator.isValidName(name)){ 
			if(userExists(email)==null){
				if(Validator.isValidPassword(password1)&& Validator.isValidPassword(password2)){
					if(password1.equals(password2)){
						String query = "insert into Users(name,email,password) values(?,?,?)";
						try(PreparedStatement preparedStatement = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){
							preparedStatement.setString(1,name);
							preparedStatement.setString(2,email);
							preparedStatement.setString(3,password2);
							preparedStatement.executeUpdate();
							ResultSet resultSet = preparedStatement.getGeneratedKeys();
							if(resultSet.next()){
								assignFolders(resultSet.getInt(1));
							}
							System.out.println("\u001B[32m"+"Account created successfully."+"\u001B[0m");
							return true;
						}catch(SQLException e){
							e.printStackTrace();
						}
					}else
						System.out.println("\033[31m"+"Password mismatch."+"\033[0m");
				}
			}else
				System.out.println("\033[31m"+"That email id is taken.Try another."+"\033[0m");
		}	
		return false;
	}
	public Integer signIn(String email,String password){
		if(Validator.isValidSmail(email)){
			String query = "select id,email,password,login_time from Users where email = ? and password = ?";
			try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
				preparedStatement.setString(1,email);
				preparedStatement.setString(2,password);
				ResultSet resultSet = preparedStatement.executeQuery();
				if(resultSet.next()){
					int id = resultSet.getInt("id");
					lastLoginTime = resultSet.getString("login_time");
					updateLoginTime(id);
					System.out.println("\u001B[32m"+"Login successfully."+"\u001B[0m");
					return id;
				}
			}
			catch(SQLException e){
				e.printStackTrace();
			}
			System.out.println("\033[31m"+"Invalid username or password."+"\033[0m");
		}
		return null;
	}
	public Integer setAnonymousUser(String email){
		String query = "insert into AnonymousUser(email) values(?)";
		try(PreparedStatement insertStatement = connection.prepareStatement(query,Statement.RETURN_GENERATED_KEYS)){
			insertStatement.setString(1,email);
			insertStatement.executeUpdate();
			try(ResultSet resultSet = insertStatement.getGeneratedKeys()){
				if(resultSet.next()){
					int id = resultSet.getInt("id");
					resultSet.close();
					return id;
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
		return null;
	}
	public Integer anonymousUserExists(String email){
		if(Validator.isValidEmail(email) && !Validator.isValidSmail(email)){
			String query = "select email from AnonymousUser where email=?";
			try(PreparedStatement selectStatement = connection.prepareStatement(query)){
				selectStatement.setString(1,email);
				ResultSet resultSet = selectStatement.executeQuery();
				if(resultSet.next()){
					return resultSet.getInt("id");
				}
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	public void updateLoginTime(int id){
		String query = "update Users set login_time=now() where id=?";
		try(PreparedStatement updateStatement = connection.prepareStatement(query)){
			updateStatement.setInt(1,id);
			updateStatement.executeUpdate();
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public Integer userExists(String email){
		if(Validator.isValidSmail(email)){
			String query = "select id from Users where email=?";
			try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
				preparedStatement.setString(1,email);
				ResultSet resultSet = preparedStatement.executeQuery();
				if(resultSet.next()){
					return resultSet.getInt("id");
				}
				resultSet.close();
			}catch(SQLException e){
				e.printStackTrace();
			}
		}
		return null;
	}
	public static void getFolderIds(){
		String query = "select id from Folders";
		try(PreparedStatement preparedStatement = connection.prepareStatement(query);
			ResultSet resultSet = preparedStatement.executeQuery()){
			folderIds = new ArrayList<>();
			while(resultSet.next()){
				folderIds.add(resultSet.getInt("id"));
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public void assignFolders(int id){
		String query = "insert into UserFolders(created_by,folder_id) values(?,?)";
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			for(int folderId:folderIds){
				preparedStatement.setInt(1,id);
				preparedStatement.setInt(2,folderId);
				preparedStatement.executeUpdate();
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	public String[] getUserData(int id){
		String query = "select name,email,login_time from Users where id=?";
		
		try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setInt(1,id);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()){
				lastLoginTime = lastLoginTime==null?resultSet.getString("login_time"):lastLoginTime;
				String userDetails[] = {resultSet.getString("name"),
										resultSet.getString("email"),
										lastLoginTime
				};
				return userDetails;
			}
		}catch(SQLException e){
			e.printStackTrace(); 
		}
		return null;
	}
}