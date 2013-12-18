package lolPoro;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String host = "localhost:3306/";
		String uName = "aschenoni";
		String uPass = "yitbos";
		try {
			Connection con = DriverManager.getConnection(host, uName, uPass);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

}
