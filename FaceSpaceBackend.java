/**
* Ethan Pavolik etp12@pitt.edu
* Ben Kristofic bmk63@pitt.edu
* Java Driver for CS1555 Term Project
*/

import java.sql.*;
import java.text.ParseException;
import oracle.jdbc.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Date;

public class FaceSpaceBackend {
	private static Connection connection; //connection to the DB
	private Statement statement; //instance of connection
	private PreparedStatement prepStatement;
	private ResultSet resultSet; //result of query
	private String query;

	public FaceSpaceBackend (Connection c){
		connection = c;
	}

	/**
	* This method is used to add a new user to the database.
	* @param name The name of the user
	* @param email The email address for the user
	* @param dob The date of birth for the user
	*/
	public long createUser(String name, String email, String dob) {
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
		return userID+1;
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

	/**
	* This method is used search for users based off a search query.
	* @param query The search query
	*/
	public void searchForUser(String query) {
		String[] queries = query.split("\\s+");
		ArrayList<String> result = new ArrayList<String>();
		String selectQuery;

		if(queries.length <= 0) {
			System.out.println("Error reading query!");
			return;
		}

		try {
			for(int i = 0; i < queries.length; i++) {
				statement = connection.createStatement();
				selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE (F_NAME = '"+queries[i]+"' OR L_NAME = '"+queries[i]+"')";
				resultSet = statement.executeQuery(selectQuery);
				while (resultSet.next()) {
					result.add(resultSet.getString("F_NAME")+" "+resultSet.getString("L_NAME"));
				}
			}

			Iterator<String> itr = result.iterator();
			long count = 1;

			if(!itr.hasNext()) {
				System.out.println("No results found!");
			}
			else {
				System.out.println("Users matching query: "+query);
				while (itr.hasNext()) {
					System.out.println(count+". "+itr.next());
					count++;
				}
			}
		}
		catch(SQLException e) {
			System.out.println("Error running the sample queries.  Machine Error: " +
							 e.toString());
		}
		finally {
			try {
				if (statement != null) statement.close();
			}
			catch (SQLException e) {
				System.out.println("Cannot close Statement. Machine error: "+e.toString());
			}
		}
	}

	/**
	* This method is used to find the most active messagers for a certain time period.
	* @param numUsers The number of users to return
	* @param userID2 The number of months to look back at
	*/
	public void topMessagers(long numUsers, long numMonths) {
		ArrayList<String> result = new ArrayList<String>();
		String selectQuery;
		long millisecondsPerMonth = 2629746000L;
		Date date = new Date();
		date.setTime(date.getTime() - (millisecondsPerMonth*numMonths)); //one month in milliseconds
		Timestamp months = new Timestamp(date.getTime());

		if(numUsers <= 0) {
			System.out.println("Please enter a valid number of users.");
			return;
		}

		if(numMonths <= 0) {
			System.out.println("Please enter a valid number of months.");
			return;
		}

		try {
			statement = connection.createStatement();
			selectQuery = "select users.user_id, count(*) AS numMsgs from (users inner join messages on" +
	    " users.user_id = messages.sender_id OR"+
			" users.user_id = messages.receiver_id)"+
			" where messages.date_sent >= to_timestamp('"+ months.toString() + "', 'yyyy-MM-dd hh24:mi:ss.ff')" +
			" group by users.user_id"+
			" order by numMsgs DESC";

			resultSet = statement.executeQuery(selectQuery);
			while (resultSet.next()) {
				result.add("User ID: "+resultSet.getLong("USER_ID")+"\t Number of messages: "+resultSet.getLong("NUMMSGS"));
			}

			Iterator<String> itr = result.iterator();
			long count = 1;

			if(!itr.hasNext()) {
				System.out.println("No results found!");
			}
			else {
				while (numUsers > 0 && itr.hasNext()) {
					System.out.println(count+".\t"+itr.next());
					numUsers--;
					count++;
				}
			}
		}
		catch(SQLException e) {
			System.out.println("Error running the sample queries.  Machine Error: " +
							 e.toString());
		}
		finally {
			try {
				if (statement != null) statement.close();
			}
			catch (SQLException e) {
				System.out.println("Cannot close Statement. Machine error: "+e.toString());
			}
		}
	}

	/**
	* This method is used to remove a user and their info from the system
	* @param userID The userID of the user to delete
	*/
	public void dropUser(long userID) {
		String selectQuery;
		try {
			statement = connection.createStatement();
			selectQuery = "DELETE FROM Friendships WHERE user_ID1 = "+userID+" OR user_ID2 = "+userID;
			statement.executeQuery(selectQuery);

			statement = connection.createStatement();
			selectQuery = "DELETE FROM Users WHERE user_ID = "+userID;
			statement.executeQuery(selectQuery);
		}
		catch(SQLException e) {
			System.out.println("Error running the sample queries.  Machine Error: " +
							 e.toString());
		}
		finally {
			try {
				if (statement != null) statement.close();
			}
			catch (SQLException e) {
				System.out.println("Cannot close Statement. Machine error: "+e.toString());
			}
		}
	}
}
