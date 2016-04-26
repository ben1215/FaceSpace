/**
* Ethan Pavolik etp12@pitt.edu
* Ben Kristofic bmk63@pitt.edu
* Java Driver for CS1555 Term Project
*/

import java.sql.*;
import java.text.ParseException;
import oracle.jdbc.*;
import java.util.*;
import java.lang.*;

public class FaceSpaceBackend {
	private static Connection connection; //connection to the DB
	private Statement statement; //instance of connection
	private PreparedStatement prepStatement;
	private ResultSet resultSet; //result of query
	private String query;
	public static String outputMessage = "Welcome To FaceSpace! :)";

	public FaceSpaceBackend (Connection c){
		connection = c;
	}

	/**
	* This method is used to add a new user to the database.
	* @param fname The first name of the user
	* @param lname The last name of the user
	* @param email The email address for the user
	* @param dob The date of birth for the user
	*/
	public void createUser(String fname, String lname, String email, String dob) {
		long userID = -1;
		// checking all of the input data to make sure it is properly formatted
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
			connection.setAutoCommit(false); //setups transaction

			//gets info needed for the next USER_ID
			statement = connection.createStatement();
			String selectQuery = "SELECT MAX(USER_ID) FROM Users";
			resultSet = statement.executeQuery(selectQuery);
			resultSet.next();
			userID = resultSet.getLong(1);

			//converts data into proper format for SQL insert
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
			connection.commit(); // closes transation

			outputMessage = "Successfully created the user:\n" + fname + " " + lname + "\nUser ID:\n" + Long.toString(userID+1);
			updateLoginTime(userID+1);
		}
		catch(SQLException Ex) {
			outputMessage = "Error accessing the database. Error code: " + Ex.toString();
			try{
				connection.rollback(); //rolls back the sql statements made in the transactions if there was an error.
			}
			catch (SQLException e) {
				outputMessage = "Error accessing the database. Error code: " + e.toString();
			}
		}
		catch (ParseException e) { //error parsing the data, not in the correct format
			outputMessage = "Error parsing the date. Error code: " + e.toString();
		}
		finally {
			try {
				connection.setAutoCommit(true); // sets the transactions control back to automatic.
				if (statement != null) statement.close();
				if (prepStatement != null) prepStatement.close();
			}
			catch (SQLException e) {
				outputMessage = "Error accessing the database. Error code: " + e.toString();
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
			connection.setAutoCommit(false); //starts transation
			statement = connection.createStatement();
			String fname1 = "", fname2 = "", lname1 = "", lname2 = "";

			if (user_ID1 == user_ID2){
				outputMessage = "Cannot initiate friendship with yourself. Please enter valid input."; return;
			}

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

			selectQuery = "SELECT FRIENDSHIP_ID FROM FRIENDSHIPS WHERE (USER_ID1 = " + Long.toString(user_ID1) + " AND USER_ID2 = " + Long.toString(user_ID2) + ") OR (USER_ID1 = " + Long.toString(user_ID2) + " AND USER_ID2 = " + Long.toString(user_ID1) + ")";
 			resultSet = statement.executeQuery(selectQuery);
			if (resultSet.next()){
				outputMessage = "The friendship already exists. It must be confirmed."; return;
			}

			//get the next friendship_ID
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
			connection.commit(); //finishes transaction
			outputMessage = "Friendship initiated between users: " + fname1 + " " + lname1 + " (" + user_ID1 + ") and  " + fname2 + " " + lname2 + " (" + user_ID2 + ") with a friendship ID of " + Long.toString(friendshipID+1);
			updateLoginTime(user_ID1);
		}
		catch(SQLException e) {
			outputMessage = "Error accessing the database. Error code: " + e.toString();
			try{
				connection.rollback(); // rolls back the changes made to the database if there was an error.
			}
			catch (SQLException ex) {
				outputMessage = "Error accessing the database. Error code: " + ex.toString();
			}
		}
		finally {
			try {
				connection.setAutoCommit(true); // sets the transactions control back to automatic.
				if (statement != null) statement.close();
				if (prepStatement != null) prepStatement.close();
			}
			catch (SQLException e) {
				outputMessage = "Error accessing the database. Error code: " + e.toString();
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
			connection.setAutoCommit(false); // starts a mannual transaction
			statement = connection.createStatement();
			String fname1 = "", fname2 = "", lname1 = "", lname2 = "";

			if (user_ID1 == user_ID2){
				outputMessage = "Cannot establish friendship with yourself. Please enter valid input."; return;
			}

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

			selectQuery = "SELECT FRIENDSHIP_ID FROM FRIENDSHIPS WHERE (USER_ID1 = " + Long.toString(user_ID1) + " AND USER_ID2 = " + Long.toString(user_ID2) + ")";
 			resultSet = statement.executeQuery(selectQuery);
			if (!resultSet.next()){
				outputMessage = "The friendship does not exist. It must be initiated before you can confirm it."; return;
			}

			//get the friendshipID of the friendship to establish
			selectQuery = "SELECT FRIENDSHIP_ID FROM FRIENDSHIPS WHERE (USER_ID1 = "+user_ID1+" AND USER_ID2 = "+user_ID2+")";
			resultSet = statement.executeQuery(selectQuery);
			resultSet.next();

			friendshipID = resultSet.getLong(1);

			//since the friendship should already be initiated just update the status and the date
			String updateQuery = "UPDATE FRIENDSHIPS SET FRIEND_STATUS = 1, DATE_ESTABLISHED = systimestamp WHERE (FRIENDSHIP_ID = "+friendshipID+")";
			statement.executeUpdate(updateQuery);
			connection.commit(); //closes transaction
			outputMessage = "Friendship established for users: " + fname1 + " " + lname1 + " (" + user_ID1 + ") and  " + fname2 + " " + lname2 + " (" + user_ID2 + ")";
			updateLoginTime(user_ID1);
		}
		catch(SQLException e) {
			outputMessage = "Error accessing the database. Error code: " + e.toString();
			try{
				connection.rollback();
			}
			catch (SQLException ex) {
				outputMessage = "Error accessing the database. Error code: " + ex.toString();
			}
		}
		finally {
			try {
				connection.setAutoCommit(true); // sets the transactions control back to automatic.
				if(statement != null) statement.close();
				if(prepStatement != null) prepStatement.close();
			}
			catch(SQLException e) {
				outputMessage = "Error accessing the database. Error code: "+ e.toString();
			}
		}
	}

	public void displayFriends (long user_ID){
		try {
			connection.setAutoCommit(false); // starts a mannual transaction
			statement = connection.createStatement();
			outputMessage = "";
			//checks to see if user exists
			String selectQuery = "SELECT * FROM USERS WHERE USER_ID = " + Long.toString(user_ID);
			resultSet = statement.executeQuery(selectQuery);
			if (!resultSet.next()){
				outputMessage = "The user " + Long.toString(user_ID) + " does not exist. Please try again."; return;
			}
			selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE USER_ID IN (SELECT USER_ID2 AS FRIEND_ID FROM FRIENDSHIPS WHERE (USER_ID1 = " + user_ID + " AND FRIEND_STATUS = 1) UNION SELECT USER_ID1 AS FRIEND_ID FROM FRIENDSHIPS WHERE (USER_ID2 = " + user_ID + " AND FRIEND_STATUS = 1))";
 			resultSet = statement.executeQuery(selectQuery);
			if (!resultSet.isBeforeFirst()){
				outputMessage = outputMessage.concat("This user has no established friends :(\n");
			}
			else{
				outputMessage = outputMessage.concat("\nTheir established friends are:\n");
				while (resultSet.next()){
					String fname = resultSet.getString(1);
					String lname = resultSet.getString(2);
					outputMessage = outputMessage.concat(fname + " " + lname +"\n");
				}
			}

			selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE USER_ID IN (SELECT USER_ID2 AS FRIEND_ID FROM FRIENDSHIPS WHERE (USER_ID1 = " + user_ID + " AND FRIEND_STATUS = 0) UNION SELECT USER_ID1 AS FRIEND_ID FROM FRIENDSHIPS WHERE (USER_ID2 = " + user_ID + " AND FRIEND_STATUS = 0))";
 			resultSet = statement.executeQuery(selectQuery);
			if (!resultSet.isBeforeFirst()){
				outputMessage = outputMessage.concat("This user has no pending friends.\n");
			}
			else{
				outputMessage = outputMessage.concat("\nTheir pending friends are:\n");
				while (resultSet.next()){
					String fname = resultSet.getString(1);
					String lname = resultSet.getString(2);
					outputMessage = outputMessage.concat(fname + " " + lname +"\n");
				}
			}
			connection.commit(); //closes transaction
		}
		catch(SQLException e) {
			outputMessage = "Error accessing the database. Error code: " + e.toString();
			try{
				connection.rollback();
			}
			catch (SQLException ex) {
				outputMessage = "Error accessing the database. Error code: " + ex.toString();
			}
		}
		finally {
			try {
				connection.setAutoCommit(true); // sets the transactions control back to automatic.
				if(statement != null) statement.close();
				if(prepStatement != null) prepStatement.close();
			}
			catch(SQLException e) {
				outputMessage = "Error accessing the database. Error code: "+ e.toString();
			}
		}
	}

	public void createGroup (String name, String description, int limit){
		long groupID = -1;
		// checking all of the input data to make sure it is properly formatted
		if (name == null || description == null){
			outputMessage = "A field cannot be empty. Please try again."; return;
		}

		if (name.length() > 32){
				outputMessage = "Group name is too long, please shorten it to 32 characters or less."; return;
		}

		if (description.length() > 1024){
				outputMessage = "Group description is too long, please shorten it to 1024 characters or less."; return;
		}

		try {
			connection.setAutoCommit(false); //setups transaction

			//gets info needed for the next USER_ID
			statement = connection.createStatement();
			String selectQuery = "SELECT MAX(GROUP_ID) FROM GROUPS";
			resultSet = statement.executeQuery(selectQuery);
			resultSet.next();
			groupID = resultSet.getLong(1);

			//execute an insert statement
			String insertQuery = "INSERT INTO GROUPS(GROUP_ID, NAME, DESCRIPTION, MEMBER_LIMIT) VALUES(?, ?, ?, ?)";
			prepStatement = connection.prepareStatement(insertQuery);
			prepStatement.setLong(1, (groupID+1));
			prepStatement.setString(2, name);
			prepStatement.setString(3, description);
			prepStatement.setInt(4, limit);
			prepStatement.executeUpdate();
			connection.commit(); // closes transation

			outputMessage = "Successfully created the group:\n\n" + name + ": " + description + "\nWith a member limit of: " + limit + "\nGroup ID: " + Long.toString(groupID+1); 
		}
		catch(SQLException Ex) {
			outputMessage = "Error accessing the database. Error code: " + Ex.toString();
			try{
				connection.rollback(); //rolls back the sql statements made in the transactions if there was an error.
			}
			catch (SQLException e) {
				outputMessage = "Error accessing the database. Error code: " + e.toString();
			}
		}
		//catch (ParseException e) { //error parsing the data, not in the correct format
			//outputMessage = "Error parsing the date. Error code: " + e.toString();
		//}
		finally {
			try {
				connection.setAutoCommit(true); // sets the transactions control back to automatic.
				if (statement != null) statement.close();
				if (prepStatement != null) prepStatement.close();
			}
			catch (SQLException e) {
				outputMessage = "Error accessing the database. Error code: " + e.toString();
			}
		}

	}

	public void addToGroup (long groupID, long userID){
		try {
			connection.setAutoCommit(false); // starts a mannual transaction
			statement = connection.createStatement();
			
			//checks to see if user exists
			String selectQuery = "SELECT * FROM USERS WHERE USER_ID = " + Long.toString(userID);
			resultSet = statement.executeQuery(selectQuery);
			if (!resultSet.next()){
				outputMessage = "The user " + Long.toString(userID) + " does not exist. Please try again."; return;
			}

			//checks to see if group exists
			selectQuery = "SELECT * FROM GROUPS WHERE GROUP_ID = " + Long.toString(groupID);
			resultSet = statement.executeQuery(selectQuery);
			if (!resultSet.next()){
				outputMessage = "The group " + Long.toString(groupID) + " does not exist. Please try again."; return;
			}
			
			//checks to see if user is already in group 
			selectQuery = "SELECT * FROM GROUP_MEMBERS WHERE (USER_ID = " + Long.toString(userID) + " AND GROUP_ID = " + Long.toString(groupID) + ")";
			resultSet = statement.executeQuery(selectQuery);
			if (resultSet.next()){
				outputMessage = "The user " + Long.toString(userID) + " is already apart of the group"; return;
			}

			//checks to see if there are already max number of members in the group
			selectQuery = "SELECT COUNT(*) FROM GROUP_MEMBERS WHERE GROUP_ID = " + Long.toString(groupID);
			resultSet = statement.executeQuery(selectQuery);
			resultSet.next();
			int memberCount = resultSet.getInt(1);
			selectQuery = "SELECT MEMBER_LIMIT FROM GROUPS WHERE GROUP_ID = " + Long.toString(groupID);
			resultSet = statement.executeQuery(selectQuery);
			resultSet.next();
			int memberLimit = resultSet.getInt(1);

			System.out.println("Count: " + memberCount  + " Limit: " + memberLimit);
			if (memberCount == memberLimit){
				outputMessage = "The max number of members in this group has been reached. Sorry."; return;
			}

			statement = connection.createStatement();
			selectQuery = "SELECT MAX(GROUP_NUMBER_ID) FROM GROUP_MEMBERS";
			resultSet = statement.executeQuery(selectQuery);
			resultSet.next();
			Long groupnumberID = resultSet.getLong(1);

			String insertQuery = "INSERT INTO GROUP_MEMBERS(GROUP_NUMBER_ID, USER_ID, GROUP_ID) VALUES(?, ?, ?)";
			prepStatement = connection.prepareStatement(insertQuery);
			prepStatement.setLong(1, (groupnumberID+1));
			prepStatement.setLong(2, userID);
			prepStatement.setLong(3, groupID);
			prepStatement.executeUpdate();
			connection.commit(); //closes transaction

			outputMessage = "User " + userID + " added to the group.";
		}
		catch(SQLException e) {
			outputMessage = "Error accessing the database. Error code: " + e.toString();
			try{
				connection.rollback();
			}
			catch (SQLException ex) {
				outputMessage = "Error accessing the database. Error code: " + ex.toString();
			}
		}
		finally {
			try {
				connection.setAutoCommit(true); // sets the transactions control back to automatic.
				if(statement != null) statement.close();
				if(prepStatement != null) prepStatement.close();
			}
			catch(SQLException e) {
				outputMessage = "Error accessing the database. Error code: "+ e.toString();
			}
		}
	}

	public void sendMessageToUser (long senderID, long receiverID, String subject, String body){
		long messageID = -1;

		try {
			connection.setAutoCommit(false); //starts transation
			statement = connection.createStatement();
		
			//checks to see if the receiver exist
			String selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE USER_ID = " + Long.toString(senderID);
			resultSet = statement.executeQuery(selectQuery);
			if (!resultSet.next()){
				outputMessage = "The sender user " + Long.toString(senderID) + " does not exist. Please try again."; return;
			}

			selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE USER_ID = " + Long.toString(receiverID);
			resultSet = statement.executeQuery(selectQuery);
			if (!resultSet.next()){
				outputMessage = "The recipient user " + Long.toString(receiverID) + " does not exist. Please try again."; return;
			}

			if (subject.length() > 32){
				outputMessage = "Your subject length is too long. Please make it 32 characters or less."; return;
			}

			if (body.length() > 100){
				outputMessage = "Your body length is too long. Please make it 100 character or less."; return;
			}

			//gets info needed for the next message_ID
			statement = connection.createStatement();
			selectQuery = "SELECT MAX(MESSAGE_ID) FROM MESSAGES";
			resultSet = statement.executeQuery(selectQuery);
			resultSet.next();
			messageID = resultSet.getLong(1);

			java.util.Date currTime = new java.util.Date();
			Timestamp messageTimeSent = new Timestamp(currTime.getTime());

			//now insert the new message
			String insertQuery = "INSERT INTO MESSAGES(MESSAGE_ID, SUBJECT, BODY, SENDER_ID, RECEIVER_ID, DATE_SENT) VALUES(?, ?, ?, ?, ?, ?)";
			
			prepStatement = connection.prepareStatement(insertQuery);
			prepStatement.setLong(1, (messageID+1));
			prepStatement.setString(2, subject);
			prepStatement.setString(3, body);
			prepStatement.setLong(4, senderID);
			prepStatement.setLong(5, receiverID);
			prepStatement.setTimestamp(6, messageTimeSent);
			prepStatement.executeUpdate();

			connection.commit(); //finishes transaction
			outputMessage = "Message Sent. Message ID: " + Long.toString(messageID+1) + "\nSender ID: "  + senderID + "\nReceiver ID: " + receiverID + "\nSubject:\n" + subject + "\nBody:\n" + body;
			updateLoginTime(senderID);
		}
		catch(SQLException e) {
			outputMessage = "Error accessing the database. Error code: " + e.toString();
			try{
				connection.rollback(); // rolls back the changes made to the database if there was an error.
			}
			catch (SQLException ex) {
				outputMessage = "Error accessing the database. Error code: " + ex.toString();
			}
		}
		finally {
			try {
				connection.setAutoCommit(true); // sets the transactions control back to automatic.
				if (statement != null) statement.close();
				if (prepStatement != null) prepStatement.close();
			}
			catch (SQLException e) {
				outputMessage = "Error accessing the database. Error code: " + e.toString();
			}
		}
		
	}

	public void displayMessages (long receiverID){
		try {
			connection.setAutoCommit(false); //starts transation
			statement = connection.createStatement();
			outputMessage = "";
		
			String selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE USER_ID = " + Long.toString(receiverID);
			resultSet = statement.executeQuery(selectQuery);
			if (!resultSet.next()){
				outputMessage = "The user " + Long.toString(receiverID) + " does not exist. Please try again."; return;
			}

			selectQuery = "SELECT F_NAME, L_NAME, SUBJECT, BODY, DATE_SENT FROM MESSAGES INNER JOIN USERS ON MESSAGES.SENDER_ID = USERS.USER_ID WHERE RECEIVER_ID = " + Long.toString(receiverID);
			resultSet = statement.executeQuery(selectQuery);
			if (!resultSet.next()){
				outputMessage = "The user " + Long.toString(receiverID) + " has no messages. Sorry!"; return;
			}
			else
			{
				outputMessage = outputMessage.concat("\n\nTheir inbox:\n\n");

				while (resultSet.next()){
					String fname = resultSet.getString(1);
					String lname = resultSet.getString(2);
					String subject = resultSet.getString(3);
					String body = resultSet.getString(4);
					Timestamp timeS = resultSet.getTimestamp(5);
					String time = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss").format(timeS);
					outputMessage = outputMessage.concat("From: " + fname + " " + lname + "\nReceived: " + time + "\nSubject: " + subject + "\nMessage: " + body + "\n\n");
				}
			}
			connection.commit(); //finishes transaction
			updateLoginTime(receiverID);
		}
		catch(SQLException e) {
			outputMessage = "Error accessing the database. Error code: " + e.toString();
			try{
				connection.rollback(); // rolls back the changes made to the database if there was an error.
			}
			catch (SQLException ex) {
				outputMessage = "Error accessing the database. Error code: " + ex.toString();
			}
		}
		finally {
			try {
				connection.setAutoCommit(true); // sets the transactions control back to automatic.
				if (statement != null) statement.close();
				if (prepStatement != null) prepStatement.close();
			}
			catch (SQLException e) {
				outputMessage = "Error accessing the database. Error code: " + e.toString();
			}
		}
	}

	public void threeDegrees (long a, long b){
		try {
			connection.setAutoCommit(false); // starts a mannual transaction
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			Statement usersStatement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			Statement statement2 = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			String selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE USER_ID = " + Long.toString(a);
			resultSet = statement.executeQuery(selectQuery);
			if (!resultSet.next()){
				outputMessage = "The user " + Long.toString(a) + " does not exist. Please try again."; return;
			}

			selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE USER_ID = " + Long.toString(b);
			resultSet = statement.executeQuery(selectQuery);
			if (!resultSet.next()){
				outputMessage = "The user " + Long.toString(b) + " does not exist. Please try again."; return;
			}
			
			outputMessage = "";
			String nameA = "", nameB = "", nameC = "", nameD = "";
			long bFriend = 0;
			if (areFriends(a,b)){
				nameA = getName(a, usersStatement);
				nameB = getName(b, usersStatement);
				outputMessage = outputMessage.concat(nameA + " and " + nameB + " are friends.\n");
				return;
			}

			ResultSet userBFriends = getFriends(b, statement);
			while (userBFriends.next()){
				bFriend = userBFriends.getLong(1);
				if (areFriends(a, bFriend)){
					nameA = getName(a, usersStatement);
					nameB = getName(b, usersStatement);
					nameC = getName(bFriend, usersStatement);
					outputMessage = outputMessage.concat(nameA + " and " + nameB + " are friends, who is friends with " + nameC + "\n");		
					return;
				}
			}

			userBFriends.beforeFirst();
			
			while (userBFriends.next()){
					long user1 = userBFriends.getLong(1);
					ResultSet userBFriends_Friends = getFriends(user1, statement2);
					while (userBFriends_Friends.next()){
						long user2 = userBFriends_Friends.getLong(1);
						if (areFriends(user1, user2)){
							nameA = getName(a, usersStatement);
							nameB = getName(b, usersStatement);
							nameC = getName(bFriend, usersStatement);
							nameD = getName(user2, usersStatement);
							outputMessage = outputMessage.concat(nameA + " and " + nameD + " are friends, who is friends with " + nameC + ", who is friends with " + nameB + "\n");		
							return;
						}
					}
			}

			outputMessage = "Alas, there is no connection of friends between these two.";
			connection.commit(); //closes transaction
			return;
		}
		catch(SQLException e) {
			outputMessage = "Error accessing the database. Error code: " + e.toString();
			try{
				connection.rollback();
			}
			catch (SQLException ex) {
				outputMessage = "Error accessing the database. Error code: " + ex.toString();
			}
		}
		finally {
			try {
				connection.setAutoCommit(true); // sets the transactions control back to automatic.
				if(statement != null) statement.close();
				if(prepStatement != null) prepStatement.close();
			}
			catch(SQLException e) {
				outputMessage = "Error accessing the database. Error code: "+ e.toString();
			}
		}
	}

	private void updateLoginTime(long user_ID){
		try{
			statement = connection.createStatement();
			String selectQuery = "UPDATE USERS SET LAST_LOGIN = SYSTIMESTAMP WHERE (USER_ID = " + user_ID + ")";
			resultSet = statement.executeQuery(selectQuery);
		}
		catch(SQLException e) {
			outputMessage = "Error accessing the database. Error code: " + e.toString();
		}
	}

	private boolean areFriends (long a, long b){
		try{
			statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String selectQuery = "SELECT COUNT(*) FROM FRIENDSHIPS WHERE (FRIEND_STATUS = 1) AND (((USER_ID1 = " + Long.toString(a) + ") AND (USER_ID2 = " + Long.toString(b) + ")) OR ((USER_ID1 = " + Long.toString(b) + ") AND (USER_ID2 = " + Long.toString(a) + ")))";
			resultSet = statement.executeQuery(selectQuery);
			resultSet.next();
			int result = resultSet.getInt(1);
			
			if (result > 0)
				return true;
			else
				return false;
		}
		catch(SQLException e) {
			outputMessage = "Error accessing the database. Error code: " + e.toString();
			return false;
		}
	}

	private ResultSet getFriends (long user, Statement s){
		try{
			//s = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			String selectQuery = "SELECT USER_ID1 FROM FRIENDSHIPS WHERE (USER_ID2 = " + Long.toString(user) + ") AND (FRIEND_STATUS = 1) UNION SELECT USER_ID2 FROM FRIENDSHIPS WHERE (USER_ID1 = " + Long.toString(user) + ") AND (FRIEND_STATUS = 1)";
			resultSet = s.executeQuery(selectQuery);
			return resultSet;
		}
		catch(SQLException e) {
			outputMessage = "Error accessing the database. Error code: " + e.toString();
			return null;
		}
	}

	private String getName (long userID, Statement s){
		try {
		String selectQuery = "SELECT F_NAME, L_NAME FROM USERS WHERE USER_ID = " + Long.toString(userID);
		ResultSet usersResultSet = s.executeQuery(selectQuery);
		usersResultSet.next();
		String fullName = usersResultSet.getString(1) + " " + usersResultSet.getString(2);
		return fullName;
		}
		catch(SQLException e) {
			outputMessage = "Error accessing the database. Error code: " + e.toString();
			return null;
		}	
	}
}
