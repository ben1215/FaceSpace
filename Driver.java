/**
* Ethan Pavolik etp12@pitt.edu
* Ben Kristofic bmk63@pitt.edu
* Java Driver for CS1555 Term Project
*/

import java.sql.*;
import java.text.ParseException;
import oracle.jdbc.*;
import java.util.Scanner;
import java.lang.Object;

public class Driver {
	private static Connection connection; //connection to the DB
	private Statement statement; //instance of connection
	private PreparedStatement prepStatement;
	private ResultSet resultSet; //result of query
	private String query;
	public static String outputMessage = "Welcome To FaceSpace! :)";

	/**
	* This method is used to add a new user to the database.
	* @param fname The first name of the user
	* @param lname The last name of the user
	* @param email The email address for the user
	* @param dob The date of birth for the user
	*/
	public void createUser(String fname, String lname, String email, String dob) {
		long userID = -1;

		if (fname == null || lname == null || email == null || dob == null){
			outputMessage = "A field cannot be empty. Please try again."; return;
		}

		if (fname.length() > 32){
				outputMessage = "First Name is too long, please shorten it to 32 characters or less."; return;
		}

		if (lname.length() > 32){
				outputMessage = "Last name is too long, please shorten it to 32 characters or less."; return;
		}

		if (email.length() > 32){
				outputMessage = "Email is too long, please shorten it to 32 characters or less."; return;
		}

		try {
			//get the next userID
			statement = connection.createStatement();
			String selectQuery = "SELECT MAX(USER_ID) FROM Users";

			resultSet = statement.executeQuery(selectQuery);

			//sets nest USER_ID to the next available number
			resultSet.next();
			userID = resultSet.getLong(1);

			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
			java.sql.Date birthday = new java.sql.Date (df.parse(dob).getTime());

			//execute an insert statement
			String insertQuery = "INSERT INTO USERS(USER_ID, F_NAME, L_NAME, EMAIL, BIRTH) VALUES(?, ?, ?, ?, ?)";
			prepStatement = connection.prepareStatement(insertQuery);

			prepStatement.setLong(1, (userID+1));
			prepStatement.setString(2, fname);
			prepStatement.setString(3, lname);
			prepStatement.setString(4, email);
			prepStatement.setDate(5, birthday);
			prepStatement.executeUpdate();

			outputMessage = "Successfully created the user:\n" + fname + " " + lname + "\nUser ID:\n" + Long.toString(userID+1);
		}
		catch(SQLException Ex) {
	    		System.out.println("Error running the sample queries.  Machine Error: " +
			       Ex.toString());
		} catch (ParseException e) {
			System.out.println("Error parsing the date. Machine Error: " + e.toString());
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
	public void initiateFriendship(long user_ID1, long user_ID2) {
		long friendshipID = -1;

		try {
			statement = connection.createStatement();
			String fname1 = "", fname2 = "", lname1 = "", lname2 = "";
			//checks to see if both users exist
			String selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE USER_ID = " + Long.toString(user_ID1);
			resultSet = statement.executeQuery(selectQuery);
			
			if (!resultSet.next()){
				outputMessage = "The user " + Long.toString(user_ID1) + " does not exist. Please try again."; return;
			}
			
			fname1 = resultSet.getString(1);
			lname1 = resultSet.getString(2);	

			selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE USER_ID =  " + Long.toString(user_ID2);
			resultSet = statement.executeQuery(selectQuery);
			
			if (!resultSet.next()){
				outputMessage = "The user " + Long.toString(user_ID2) + " does not exist. Please try again."; return;
			}

			fname2 = resultSet.getString(1);
			lname2 = resultSet.getString(2);

			//get the next friendshipID
			selectQuery = "SELECT MAX(FRIENDSHIP_ID) FROM FRIENDSHIPS";

			//sets FRIENDSHIP_ID value
			resultSet = statement.executeQuery(selectQuery);
			resultSet.next();
			friendshipID = resultSet.getLong(1);

			//now insert the new friendship
			String insertQuery = "INSERT INTO FRIENDSHIPS(FRIENDSHIP_ID, USER_ID1, USER_ID2, FRIEND_STATUS) VALUES(?, ?, ?, ?)";
			prepStatement = connection.prepareStatement(insertQuery);

			prepStatement.setLong(1, (friendshipID+1));
			prepStatement.setLong(2, user_ID1);
			prepStatement.setLong(3, user_ID2);
			prepStatement.setLong(4, 0); //friendship is pending

			prepStatement.executeUpdate();

			outputMessage = "Friendship initiated between users: " + fname1 + " " + lname1 + " (" + user_ID1 + ") and  " + fname2 + " " + lname2 + " (" + user_ID2 + ")";

		}
		catch(SQLException e) {
			System.out.println("Error running the sample queries.  Machine Error: " +
				       e.toString());
		}
		finally {
			try {
				if (statement != null) statement.close();
				if (prepStatement != null) prepStatement.close();
			}
			catch (SQLException e) {
				System.out.println("Cannot close Statement. Machine error: "+e.toString());
			}
		}
	}

	/**
	* This method is used to create a bilateral friendship between two users.
	* @param userID1 The user ID of the first user
	* @param userID2 The user ID of the second user
	*/
	public void establishFriendship(long user_ID1, long user_ID2) {
		long friendshipID = -1;

		try {
			//get the friendshipID of the friendship to establish
			statement = connection.createStatement();
			String fname1 = "", fname2 = "", lname1 = "", lname2 = "";
			//checks to see if both users exist
			String selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE USER_ID = " + Long.toString(user_ID1);
			resultSet = statement.executeQuery(selectQuery);
			
			if (!resultSet.next()){
				outputMessage = "The user " + Long.toString(user_ID1) + " does not exist. Please try again."; return;
			}
			
			fname1 = resultSet.getString(1);
			lname1 = resultSet.getString(2);	

			selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE USER_ID =  " + Long.toString(user_ID2);
			resultSet = statement.executeQuery(selectQuery);
			
			if (!resultSet.next()){
				outputMessage = "The user " + Long.toString(user_ID2) + " does not exist. Please try again."; return;
			}

			fname2 = resultSet.getString(1);
			lname2 = resultSet.getString(2);


			selectQuery = "SELECT FRIENDSHIP_ID FROM FRIENDSHIPS WHERE (USER_ID1 = "+user_ID1+" AND USER_ID2 = "+user_ID2+")";
			resultSet = statement.executeQuery(selectQuery);
			resultSet.next();

			friendshipID = resultSet.getLong(1);

			//since the friendship should already be initiated just update the status and the date
			String updateQuery = "UPDATE FRIENDSHIPS SET FRIEND_STATUS = 1, DATE_ESTABLISHED = systimestamp WHERE (FRIENDSHIP_ID = "+friendshipID+")";
			statement.executeUpdate(updateQuery);

			outputMessage = "Friendship established for users: " + fname1 + " " + lname1 + " (" + user_ID1 + ") and  " + fname2 + " " + lname2 + " (" + user_ID2 + ")";

		}
		catch(SQLException e) {
			System.out.println("Error running the sample queries.  Machine Error: " +
				       e.toString());
		}
		finally {
			try {
				if(statement != null) statement.close();
				if(prepStatement != null) prepStatement.close();
			}
			catch(SQLException e) {
				System.out.println("Cannot close Statement. Machine error: "+e.toString());
			}
		}

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
			System.out.println("Successfully connected!");
			System.out.println();

			//do driver stuff
			Driver test = new Driver();
			Scanner input = new Scanner(System.in);
			Scanner str = new Scanner(System.in);
			Scanner num = new Scanner(System.in);
			int op;

			while(true) {
				System.out.println(outputMessage);
				System.out.println();
				System.out.println("Select a function to test.");
				System.out.println("0. Exit");
				System.out.println("1. createUser()");
				System.out.println("2. initiateFriendship()");
				System.out.println("3. establishFriendship()");
				System.out.println();
				op = input.nextInt();

				switch(op) {

				case 1:
					System.out.println("Enter a full name: ");
					String name = str.nextLine();
					System.out.println("Enter an email: ");
					String email = str.nextLine();
					System.out.println("Enter a date of birth - YYYY-MM-DD");
					String dob = str.nextLine();
					String[] names = name.split(" ");
					System.out.println();
					test.createUser(names[0], names[1], email, dob);
					break;

				case 2:
					System.out.println("Enter the user ID of the user sending the request: ");
					int u1 = num.nextInt();
					System.out.println("Enter the user ID of the user receiving the request: ");
					int u2 = num.nextInt();
					System.out.println();
					test.initiateFriendship(u1, u2);
					break;

				case 3:
					System.out.println("Enter the first user ID: ");
					int us1 = num.nextInt();
					System.out.println("Enter the second user ID: ");
					int us2 = num.nextInt();
					System.out.println();
					test.establishFriendship(us1, us2);
					break;

				default:
					System.out.println("Closing the connection...");
					connection.close();
					return;
				}
			}
		}
	}
}
