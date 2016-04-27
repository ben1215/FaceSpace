public class FaceSpaceDemo {
  private FaceSpaceBackend backend;

  //sets a backend object
  public FaceSpaceDemo(FaceSpaceBackend b) {
    backend = b;
  }

  //runs the demo
  public void run() {
    long u1, u2;

    //1. createUser
    System.out.println("Creating two new users...");
    System.out.println("1. Name: Teddy Brown, Email: tbrown@pitt.edu, Birthday: May 19th, 1996..");
    u1 = backend.createUser("Teddy", "Brown", "tbrown@pitt.edu", "1996-05-19");
    System.out.println("2. Name: George Robinson, Email: grobin@pitt.edu, Birthday: March 29th, 1996..");
    u2 = backend.createUser("George", "Robinson", "grobin@pitt.edu", "1996-03-29");
    System.out.println();

    //2. initiateFriendship
    System.out.println("Teddy is initiating a friendship to George...");
    backend.initiateFriendship(u1, u2);
    System.out.println();

    //3. establishFriendship
    System.out.println("George is accepting Teddy's friendship request...");
    backend.establishFriendship(u1, u2);
    System.out.println();

    //9. searchForUser
    System.out.println("Sending a search query for 'Teddy Robinson'...");
    backend.searchForUser("Teddy Robinson");
    System.out.println();

    //11. topMessagers
    System.out.println("Displaying the top five users for have sent the most messages in the past month..");
    backend.topMessagers(5, 1);
    System.out.println();

    //12. dropUser
    System.out.println("Deleting both Teddy Brown and George Robinson..");
    backend.dropUser(u1);
    backend.dropUser(u2);
    System.out.println("Resending another search query for 'Teddy Robinson'...");
    backend.searchForUser("Teddy Robinson");
    System.out.println();

    System.out.println("Demo is finished.");
  }
}
