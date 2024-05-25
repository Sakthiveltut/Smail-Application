import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DBConnection{
	private static final String URL = "jdbc:mysql://localhost:3306/email";
	private static final String USER = "root", PASSWORD = "root";
	private static Connection connection;

	private DBConnection(){}
	
	public static synchronized Connection getConnection(){
		try{
			if(connection == null){
				Class.forName("com.mysql.cj.jdbc.Driver");
				connection = DriverManager.getConnection(URL,USER,PASSWORD);
			}
		}catch(ClassNotFoundException|SQLException e){
			e.printStackTrace();
		}
		return connection;
	}
}