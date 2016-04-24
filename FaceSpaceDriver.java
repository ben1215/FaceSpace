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

public class FaceSpaceDriver {
	private static Connection connection; //connection to the DB
	private Statement statement; //instance of connection
	private PreparedStatement prepStatement;
	private ResultSet resultSet; //result of query
	private String query;

public static void main(String args[]) throws SQLException {
		String username, password;
		username = "bmk63";
		password = "3844490";

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
			System.out.println("Error connecting to database! Error Code: "
				+ e.toString());
		}
		finally {
			System.out.println("Successfully connected!");
			System.out.println();

			//do driver stuff
			FaceSpaceBackend backend = new FaceSpaceBackend(connection);
			Scanner input = new Scanner(System.in);
			int op;

			while(true) {
				try{
					System.out.println(backend.outputMessage);
					System.out.println();
					System.out.println("Select a function to test.");
					System.out.println("0. Exit");
					System.out.println("1. createUser()");
					System.out.println("2. initiateFriendship()");
					System.out.println("3. establishFriendship()");
					System.out.println("4. displayFriends()");
					System.out.println("5. createGroup()");
					System.out.println("6. addToGroup()");
					System.out.println("7. sendMessageToUser()");
					System.out.println("8. displayMessages()");
					System.out.println();
					op = input.nextInt();
					input.nextLine();
					
					switch(op) {

					case 1:
						System.out.println("Enter a full name (first and last): ");
						String name = input.nextLine();
						System.out.println("Enter an email: ");
						String email = input.nextLine();
						System.out.println("Enter a date of birth - YYYY-MM-DD");
						String dob = input.nextLine();
						String[] names = name.split(" ");
						System.out.println();
						if (names.length == 1)
							backend.createUser(names[0], "", email, dob);
						else
							backend.createUser(names[0], names[1], email, dob);
						break;

					case 2:
						System.out.println("Enter the user ID of the user sending the request: ");
						int u1 = input.nextInt();
						System.out.println("Enter the user ID of the user receiving the request: ");
						int u2 = input.nextInt();
						System.out.println();
						backend.initiateFriendship(u1, u2);
						break;

					case 3:
						System.out.println("Enter the initiator user ID: ");
						int us1 = input.nextInt();
						System.out.println("Enter the confirmer user ID: ");
						int us2 = input.nextInt();
						System.out.println();
						backend.establishFriendship(us1, us2);
						break;

					case 4:
						System.out.println("Enter the user ID for the user: ");
						int user = input.nextInt();
						backend.displayFriends(user);
						System.out.println();
						break;

					case 5: 
						System.out.println("Enter the group name: ");
						String gname = input.nextLine();
						System.out.println("Enter in a brief description: ");
						String gdescription = input.nextLine();
						System.out.println("Enter in the max number of group members: ");
						int maxusers = input.nextInt();
						System.out.println();
						backend.createGroup(gname, gdescription, maxusers);
						break;

					case 6:
						System.out.println("Enter the user ID:");
						user = input.nextInt();
						System.out.println("Enter in the group ID:");
						int gid = input.nextInt();
						backend.addToGroup(gid, user);
						System.out.println();
						break;

					case 7:
						System.out.println("Enter in the message subject:");
						String subject = input.nextLine();
						System.out.println("Enter in the message:");
						String body = input.nextLine();
						System.out.println("Who is sending the message? (Enter in the user ID)");
						int sender = input.nextInt();
						System.out.println("Who is receiving the message: (Enter in the user ID)");
						int receiver = input.nextInt();
						System.out.println();
						backend.sendMessageToUser(sender, receiver, subject, body);
						break;

					case 8:
						System.out.println("What user? (User ID)");
						user = input.nextInt();
						System.out.println();
						backend.displayMessages(user);
						break;

					default:
						System.out.println("Closing the connection...");
						connection.close();
						return;
					}
				}
				catch (InputMismatchException e) {
					backend.outputMessage = "Data not properly entered, please try again.";
					input.next();
				}
			}
		}
	}
}
