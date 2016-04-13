/**
* Ethan Pavolik etp12@pitt.edu
* Ben Kristofic bmk63@pitt.edu
* Java Driver for CS1555 Term Project
*/

import java.sql.*;
import java.text.ParseException;
import oracle.jdbc.*;
import java.util.Scanner;

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

		try {
			//get the next userID
			statement = connection.createStatement();
			String selectQuery = "SELECT MAX(USER_ID) FROM Users";

			resultSet = statement.executeQuery(selectQuery);

			resultSet.next();
			userID = resultSet.getLong(1);

			String[] names = name.split(" ");

			java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd");
			java.sql.Date birthday = new java.sql.Date (df.parse(dob).getTime());

			//execute an insert statement
			String insertQuery = "INSERT INTO USERS(USER_ID, F_NAME, L_NAME, EMAIL, BIRTH) VALUES(?, ?, ?, ?, ?)";
			prepStatement = connection.prepareStatement(insertQuery);

			prepStatement.setLong(1, (userID+1));
			prepStatement.setString(2, names[0]);
			prepStatement.setString(3, names[1]);
			prepStatement.setString(4, email);
			prepStatement.setDate(5, birthday);
			prepStatement.executeUpdate();
			System.out.println("Successfully created user with ID: "+(userID+1));
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
	public void initiateFriendship(long user_ID1, long user_ID2) {
		long friendshipID = -1;

		try {
			//get the next friendshipID
			statement = connection.createStatement();
			String selectQuery = "SELECT MAX(FRIENDSHIP_ID) FROM FRIENDSHIPS";

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

			System.out.println("Friendship initiated between users: "+user_ID1+ " "+user_ID2);

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
			String selectQuery = "SELECT FRIENDSHIP_ID FROM FRIENDSHIPS WHERE (USER_ID1 = "+user_ID1+" AND USER_ID2 = "+user_ID2+")";
			resultSet = statement.executeQuery(selectQuery);
			resultSet.next();

			friendshipID = resultSet.getLong(1);

			//since the friendship should already be initiated just update the status and the date
			String updateQuery = "UPDATE FRIENDSHIPS SET FRIEND_STATUS = 1, DATE_ESTABLISHED = systimestamp WHERE (FRIENDSHIP_ID = "+friendshipID+")";
			statement.executeUpdate(updateQuery);

		}
		catch(SQLException e) {
			System.out.println("Error running the sample queries.  Machine Error: " +
				       e.toString());
		}
		finally {
			System.out.println("Friendship established for users: "+user_ID1+" "+user_ID2);
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

			//do driver stuff
			Driver test = new Driver();
			Scanner input = new Scanner(System.in);
			Scanner str = new Scanner(System.in);
			Scanner num = new Scanner(System.in);
			int op;

			while(true) {
				System.out.println("Select a function to test.");
				System.out.println("0. Exit");
				System.out.println("1. createUser()");
				System.out.println("2. initiateFriendship()");
				System.out.println("3. establishFriendship()");
				op = input.nextInt();

				switch(op) {

				case 1:
					System.out.println("Enter a fullname: ");
					String name = str.nextLine();
					System.out.println("Enter an email: ");
					String email = str.nextLine();
					System.out.println("Enter a date of birth - YYYY-MM-DD");
					String dob = str.nextLine();
					test.createUser(name, email, dob);
					break;

				case 2:
					System.out.println("Enter the user ID of the user sending the request: ");
					int u1 = num.nextInt();
					System.out.println("Enter the user ID of the user receiving the request: ");
					int u2 = num.nextInt();
					test.initiateFriendship(u1, u2);
					break;

				case 3:
					System.out.println("Enter the first user ID: ");
					int us1 = num.nextInt();
					System.out.println("Enter the second user ID: ");
					int us2 = num.nextInt();
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
