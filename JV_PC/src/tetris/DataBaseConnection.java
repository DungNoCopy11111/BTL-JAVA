package tetris;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.cj.jdbc.Driver;
//import com.mysql.cj.jdbc.Statement;
import java.sql.Statement;
public class DataBaseConnection {
	private static final String db_url = "jdbc:mysql://localhost:3306/Score";
	private static final String user = "root";
	private static final String password = "123456789";
	private static Connection conn;
	
	public static Connection getConn()
	{
		
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			conn = DriverManager.getConnection(db_url,user,password);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return conn;
	}
	public static void executeUpdate(String query) {
	    Connection conn = null;
	    Statement stmt = null;
	    try {
	        conn = getConn();
	        stmt = conn.createStatement();
	        int rows = stmt.executeUpdate(query);
	        System.out.println("Successful query!!! "+rows);
	    } catch (SQLException e) {
	        System.out.println("Query execution failed!!!");
	        e.printStackTrace();
	    } finally {
	        try {
	            if (stmt != null) stmt.close();
	            if (conn != null) conn.close();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}
//	public static int gethighscore()
//	{
//		int max = 0;
//		try {
//			conn = getConn();
//			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
//                    ResultSet.CONCUR_UPDATABLE);
//            // get data from table 'student'
//            ResultSet rs = stmt.executeQuery("SELECT MAX(Diem) FROM Scores");
//            // getting the record of 3rd row
//            rs.absolute(1);
//			max = rs.getInt(1);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return max;
//	}
}
