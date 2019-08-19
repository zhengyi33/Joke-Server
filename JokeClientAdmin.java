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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class JokeClientAdmin {

	public static void main(String[] args) {
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String serverName;
		String serverName2 = null;
		int port;
		int port2;
		
		if (args.length<1) {//no args
			serverName = "localhost";
			port = 5050;
		}
		else if(args.length==1) {//primary server ip
			serverName = args[0];
			port = 5050;
			}
		else if(args.length == 2) {//primary and secondary server ip
			serverName = args[0];
			serverName2 = args[1];
			port = 5050;
			port2 = 5051;
		}
		else {//other random situations
			serverName = "localhost";
			port = 5050;
		}
			
		
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String current_server = serverName;
		int current_port = port;
		boolean s = true;//s for switching between servers
		String input = null;
		try{
			do {
				System.out.println("Press enter to toggle between modes, <s> to switch servers, <quit> to end");

				input = in.readLine();
				if (input.equals("s")) {
					s = !s;
				
					if((serverName2!=null)&&(s==false)) {
						//sock = new Socket(serverName2, 5051);
						current_port = 5051;
						current_server = serverName2;//I only set some variables here to be passed later for declaring new socket
						System.out.printf("Now communicating with: %s, port 5051\n", serverName2);
						//continue;
					}
					else if((serverName2==null)&&(s==false)) {
						current_port = 5050;
						current_server = serverName;
						System.out.println("No secondary server being used");
						s = true;
						//continue;
					}
					else if(s) {
						//sock = new Socket(serverName, 5050);
						current_port = 5050;
						current_server = serverName;
						System.out.printf("Now communicating with: %s, port 5050\n", serverName);
						//continue;
					}
				}
				
				else if(input.isEmpty()) {
					/*if(s) {
						sock = new Socket(serverName, 5050);
					}
					else {
						sock = new Socket(serverName2, 5051);
					}*/
					sock = new Socket(current_server,current_port);
					fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
					toServer = new PrintStream(sock.getOutputStream());
					toServer.println("toggle");//can send whatever you want. just remember to receive and check on server side
					String mode = fromServer.readLine();
					System.out.println("current mode is " + mode+".");
					System.out.println();
				}
			}while(input.indexOf("quit") < 0);
			System.out.println("Cancelled by user request.");
		}catch (IOException x) {
			x.printStackTrace();
		}
		
		/*try {
			
			while(true) {
				System.out.println("Press enter to toggle between modes.");
				String input = in.readLine();
				
				sock = new Socket(serverName, 5050);
				fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
				toServer = new PrintStream(sock.getOutputStream());
				if (input.isEmpty()) {
					toServer.println("toggle");
					String mode = fromServer.readLine();
					System.out.println("current mode is " + mode+".");
					System.out.println();
				}
				
				
			}
		}catch(IOException x) {
			System.out.println("Socket error.");
			x.printStackTrace();
		}*/

	}

}
