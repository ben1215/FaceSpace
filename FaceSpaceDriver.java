/**
* Ethan Pavolik etp12@pitt.edu
* Ben Kristofic bmk63@pitt.edu
* Java Driver for CS1555 Term Project
*/

import java.sql.*;
import java.text.ParseException;
import oracle.jdbc.*;
import java.util.Scanner;

public class FaceSpaceDriver {
	private static Connection connection; //connection to the DB
	private Statement statement; //instance of connection
	private PreparedStatement prepStatement;
	private ResultSet resultSet; //result of query
	private String query;
	private FaceSpaceBackend backend;

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

			FaceSpaceBackend backend = new FaceSpaceBackend(connection);
			//do driver stuff
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
					backend.createUser(name, email, dob);
					break;

				case 2:
					System.out.println("Enter the user ID of the user sending the request: ");
					int u1 = num.nextInt();
					System.out.println("Enter the user ID of the user receiving the request: ");
					int u2 = num.nextInt();
					backend.initiateFriendship(u1, u2);
					break;

				case 3:
					System.out.println("Enter the first user ID: ");
					int us1 = num.nextInt();
					System.out.println("Enter the second user ID: ");
					int us2 = num.nextInt();
					backend.establishFriendship(us1, us2);
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
