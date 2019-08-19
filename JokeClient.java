//package distributed;

/*--------------------------------------------------------

1. Name / Date: Yi Zheng

2. Java version used, if not the official version for the class:

Java se-10

3. Precise command-line compilation examples / instructions:



> javac JokeServer.java
> javac JokeClient.java
> javac JokeClientAdmin.java


4. Precise examples / instructions to run this program:



In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

> java JokeServer secondary
> java JokeClient localhost localhost
> java JokeClientAdmin localhost localhost

You can replace localhost with other ip addresses

5. List of files needed for running the program.


 JokeServer.java
 JokeClient.java
 JokeClientAdmin.java



----------------------------------------------------------*/


import java.io.*; 
import java.net.*;

public class JokeClient {
	
	public static void main(String[] args) {
		String ID = "not_initialized";
		String ID2 = "not_initialized";
		String userName;
		String serverName;
		String serverName2 = null;
		if (args.length<1) {
			serverName = "localhost";
		}
		else if(args.length == 1){
			serverName = args[0];
		}
		
		else if (args.length == 2) {
			serverName = args[0];
			serverName2 = args[1];
		}
		
		else {
			serverName = "localhost";
		}

		System.out.println("Yi's Joke Client, 1.8.\n");
		System.out.printf("Server one: " + serverName + ", Port: 4545\n");
		if(serverName2!=null) {
			System.out.printf("Server two: " + serverName2 + ", Port: 4546\n");
		}
		
		BufferedReader in = new BufferedReader (new InputStreamReader(System.in)); 
		
		
		try {
			System.out.print("Please enter user name: ");
			System.out.flush();
			userName = in.readLine();//input from keyboard
			String input=null;
			boolean s = true; //control switch defaults to primary server
			
			do {
				
				System.out.print("Press enter to get joke/proverb, (s) to switch servers, (quit) to end: ");
				System.out.flush(); //flush makes sure the output is printed to screen instead of being buffered.
				input = in.readLine();// reads a line from keyboard.
				if (input.equals("s")) {
					if(serverName2!=null) {
						if(s) {
							s = false;
							System.out.printf("Now communicating with: %s, port 4546\n", serverName2);
						}
						else {
							s = true;
							System.out.printf("Now communicating with: %s, port 4545\n", serverName);
						}
						continue;
					}
					else {
						System.out.println("No secondary server being used");
						continue;
					}
				}
				if (input.isEmpty() ) {  //if enter is pressed
					if(s) {
						ID = getJP(userName, ID, serverName, 4545);
					}
					else {
						ID2 = getJP(userName, ID2, serverName2, 4546);
					}
				}
				
			}while(input.indexOf("quit") < 0); //if "quit" is not in the line just read, read the next input
			System.out.println("Cancelled by user request."); 
		} catch(IOException x) {
			x.printStackTrace();
		}
	}
	
	
	static String getJP(String userName, String ID, String serverName, int port) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		
		//System.out.println("getJP gets called");
		
		try {
			sock = new Socket(serverName, port); 
			
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream())); //used to read from server
			toServer = new PrintStream(sock.getOutputStream()); //used to write to server
			
			toServer.println(userName); //user name sent to server
			//if (ID!=null) {
			toServer.println(ID);//ID sent to server, important for server to know who you are
			//}
			toServer.flush();
			
			//for (int i=1; i<=3; i++) {
			textFromServer = fromServer.readLine(); //read a line from server through socket
			if(textFromServer != null) {
				if(port == 4545) {
					System.out.println(textFromServer);
				}
				else if(port == 4546) {
					System.out.println("<S2> "+textFromServer);
				}
			}//concatenated string
			ID = fromServer.readLine();
			//}
			sock.close(); //closes the connection
		} catch(IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}
		return ID;//if new ID, return new ID. if old ID, the value shouldn't change
	}
}
