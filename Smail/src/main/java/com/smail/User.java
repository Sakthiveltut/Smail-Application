package com.smail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
 
public class User{
	private String name,email,password,lastLoginTime;
	private long id;
	
	public User(long id,String email) {
		this.id = id;
		this.email = email;
	}
	
	public User(long id,String name,String email,String password,String lastLoginTime) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.password = password;
		this.lastLoginTime = lastLoginTime;
	}
		
	public long getUserId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getEmail() {
		return email;
	}
	public String getPassword() {
		return password;
	}
	public String getLastLoginTime() {
		return lastLoginTime;
	}
	public void setId(long id) {
		this.id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setLastLoginTime(String lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}
	
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

	/*public void setCurrentUserData(long id){
		String query = "select name,email,login_time from Users u join RegisteredUsers ru  on u.id = ru.user_id where u.id=?";
		
		try(Connection connection  = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(query)){
			preparedStatement.setLong(1,id);
			ResultSet resultSet = preparedStatement.executeQuery();
			if(resultSet.next()){
				lastLoginTime = lastLoginTime==null?resultSet.getString("login_time"):lastLoginTime;
				String name = resultSet.getString("name"),email = resultSet.getString("email");
				setId(id);
				setName(name);
				setEmail(email);
				setLastLoginTime(lastLoginTime);
			}
		}catch(SQLException e){
			e.printStackTrace(); 
		}
	}*/
}