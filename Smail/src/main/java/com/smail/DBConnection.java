package com.smail;
/*import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.BasicDataSource;*/

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnection{
	/*private static HikariDataSource dataSource;
	static {
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:mysql://localhost:3306/email");
		config.setUsername("Sakthi");
		config.setPassword("Sakthi");
		config.setMaximumPoolSize(10);
		
		dataSource = new HikariDataSource(config);
	}*/
	
	/*private static BasicDataSource dataSource;
	
	static {
		dataSource = new BasicDataSource();
		dataSource.setUrl("jdbc:mysql://localhost:3306/email");
		dataSource.setUsername("root");
		dataSource.setPassword("root");
		dataSource.setMaxTotal(10);
		dataSource.setMinIdle(2);
		dataSource.setMaxIdle(5);
	}
	
	public static Connection getConnection() throws SQLException{
		 return dataSource.getConnection();
	}	
	
	
	public static void closeDataSource() {
		if(dataSource!=null) {
			try {
				dataSource.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}*/
	
	private static final String URL = "jdbc:mysql://localhost:3306/email";
	private static final String USER = "root", PASSWORD = "root";
	private static Connection connection;

	private DBConnection(){}
	
	public static synchronized Connection getConnection() throws Exception{
		try{
			if(connection == null || !isValidConnection()){
				Class.forName("com.mysql.cj.jdbc.Driver");
				connection = DriverManager.getConnection(URL,USER,PASSWORD);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new Exception("An unexpected error occurred. Please try again later.");
		}
		return connection;
	}
	
	public static boolean isValidConnection() throws Exception {
		try(Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery("select 1")){
			if(resultSet.next()) {
				return true;
			}
		}catch (Exception e) {
			e.printStackTrace();
			throw new Exception("An unexpected error occurred. Please try again later.");
		}
		return false;
	}
}
