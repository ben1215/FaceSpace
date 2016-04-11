/**
* Ethan Pavolik etp12@pitt.edu
* Ben Kristofic bmk63@pitt.edu
* Java Driver for CS1555 Term Project
*/

import java.sql.*;
import java.text.ParseException;
import oracle.jdbc.*;

public class Driver {
	private static Connection connection; //connection to the DB
	private Statement statement; //instance of connection
	private PreparedStatement prepStatement;
	private ResultSet resultSet; //result of query
	private String query;

	/**
	* This method is used to add a new user to the database.
	* @param name The name of the user
	* @param email The email address for the user
	* @param dob The date of birth for the user
	*/
	public void createUser(String name, String email, String dob) {
		long userID = -1;

		//get the next userID
		try {
			statement = connection.createStatement();
			String selectQuery = "SELECT MAX(USER_ID) FROM Users";

			resultSet = statement.executeQuery(selectQuery);

			resultSet.next();
			userID = resultSet.getLong(1);

			String[] names = name.split(" ");

			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
			java.sql.Date birthday = new java.sql.Date (df.parse(dob).getTime());

			String insertQuery = "INSERT INTO USERS(USER_ID, F_NAME, L_NAME, EMAIL, BIRTH) VALUES(?, ?, ?, ?, ?)";
			prepStatement = connection.prepareStatement(insertQuery);

			prepStatement.setLong(1, (userID+1));
			prepStatement.setString(2, names[0]);
			prepStatement.setString(3, names[1]);
			prepStatement.setString(4, email);
			prepStatement.setDate(5, birthday);

			prepStatement.executeUpdate();

		}
		catch(SQLException Ex) {
	    		System.out.println("Error running the sample queries.  Machine Error: " +
			       Ex.toString());
		} catch (ParseException e) {
			System.out.println("Error parsing the date. Machine Error: " +
			e.toString());
		}
		finally{
			try {
				if (statement != null) statement.close();
				if (prepStatement != null) prepStatement.close();
			} catch (SQLException e) {
				System.out.println("Cannot close Statement. Machine error: "+e.toString());
			}
		}

	}

	/**
	* This method is used to initiate a pending friendship request from one user
	* to another.
	* @param userID1 The user sending the friendship request
	* @param userID2 The user receiving the friendship request
	*/
	public void initiateFriendship(int userID1, int userID2) {


	}

	/**
	* This method is used to create a bilateral friendship between two users.
	* @param userID1 The user ID of the first user
	* @param userID2 The user ID of the second user
	*/
	public void establishFriendship(int userID1, int userID2) {


	}

	public static void main(String args[]) throws SQLException {
		String username, password;
		username = "etp12";
		password = "3981457";

		try {
			//register Oracle Driver
			System.out.println("Registering DB...");
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

			//set the location of the database
			String url = "jdbc:oracle:thin:@class3.cs.pitt.edu:1521:dbclass";

			//create connection
			System.out.println("Attempting to connect to DB...");
			connection = DriverManager.getConnection(url, username, password);
		}
		catch(Exception e) {
			System.out.println("Error connecting to database! Machine Error: "
				+ e.toString());
		}
		finally {

			//do driver stuff
			Driver test = new Driver();
			test.createUser("Ethan Pavolik", "etp12@pitt.edu", "1996-05-19");
			connection.close();
		}
	}


}
