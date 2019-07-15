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

5. Notes:

Acceptable commands are displayed are consoles. It should be very clear. If you have question, please feel free to contact me at zhengyi5411@hotmail.com. I will respond as soon as possible!

----------------------------------------------------------*/


import java.io.*; 
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

class Worker extends Thread{
	
	Socket sock;

	Worker(Socket s){
		this.sock = s;
	}
	
	
	public void run() {
		
		BufferedReader in; 
		PrintStream out;
		
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			
			out = new PrintStream(sock.getOutputStream()); /* creates a new print stream. The output stream of socket can be used for server to write stuff through and for client to read from. */ 
			
			try {
				String name = in.readLine(); // reads a line from socket. The DNS to look up.
				
				String ID = in.readLine();//ID sent by client
				if (ID.equals("not_initialized")){//if first time, then generate new ID
					ID = UUID.randomUUID().toString();
					JokeServer.map.put(ID, new boolean[8]);//hash table for storing state
				}
				
				String joke_prov = null;
				String letterIndex = null;
				
				boolean[] list = JokeServer.map.get(ID);//get state
				if(!JokeServer.mode) {//false for joke mode
					System.out.println("Retrieving joke for "+name);
					//joke_prov = null;
					ArrayList<Integer> indice = new ArrayList<>();
					for (int i=0; i<4; i++) {
						indice.add(i);
					}
					Collections.shuffle(indice);//indice is an array list that is randomized
					while(joke_prov==null) {//if no joke is retrived
						for (int i=0;i<4;i++) {
							int j = indice.get(i);//randomized index into the boolean array for storing state
							if(!list[j]) {//false means a joke not seen b4
								joke_prov = JokeServer.jokes[j];//get joke from global array
								list[j] = true;//now it's seen
								JokeServer.map.put(ID,list);//update global hash table
								Character c = (char) (j+65);
								letterIndex = "J"+Character.toString(c);//concatenation
								break;
							}
						}
					
						if (joke_prov == null) {//if all jokes have been seen, refresh state
							for (int i=0;i<4;i++) {
								list[i] = false;
							}
						}
					}
				}
				else {//same logic as jokes
					System.out.println("Retrieving proverb for "+name);
					//joke_prov = null;
					ArrayList<Integer> indice47 = new ArrayList<>();
					for (int i=4; i<8; i++) {
						indice47.add(i);
					}
					Collections.shuffle(indice47);
					while(joke_prov==null) {
						for (int i=0;i<4;i++) {
							int j = indice47.get(i);
							if(!list[j]) {
								joke_prov = JokeServer.proverbs[j-4];
								list[j] = true;
								JokeServer.map.put(ID,list);
								Character c = (char) (j+61);
								letterIndex = "P"+Character.toString(c);
								break;
							}
						}
					
						if (joke_prov == null) {
							for (int i=4;i<8;i++) {
								list[i] = false;
							}
						}
					}
				}

				printJP(letterIndex, name, joke_prov, ID, out);
				
			} catch(IOException x) {
				System.out.println("Server read error");
				x.printStackTrace();
			}
			sock.close(); // closes connections with the client.
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
	
	static void printJP(String letterIndex, String name, String joke_prov, String ID, PrintStream out) {
		
		//System.out.println("Sending back joke/proverb "+joke_prov);
		System.out.printf("%s %s: %s\n", letterIndex,name,joke_prov);//concatenation
		out.printf("%s %s: %s\n", letterIndex,name,joke_prov); //What's printed is sent to client.
		System.out.println(ID);//print to server screen for debug
		out.println(ID);//sent to client
		out.flush();
			
	}
	
	
}

class AdminWorker extends Thread {
	Socket sock;
	
	AdminWorker(Socket s){
		this.sock = s;
	}
	
	public void run() {
		BufferedReader in;
		PrintStream out;
		
		try {
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
			String input = in.readLine();
			String output;
			if ((input!=null)&&input.equals("toggle")) {//user hits enter
				if(JokeServer.mode) {//switch modes
					JokeServer.mode = false;
					output = "joke mode";
					out.println(output);//output back to client
				}
				else {
					JokeServer.mode = true;
					output = "proverb mode";
					out.println(output);
				}
			}
			
				
			
			sock.close();
		} catch(IOException ioe) {
			System.out.println(ioe);
		}
	}
}

class AdminLooper implements Runnable {
	public static boolean adminControlSwitch = true;
	int port;
	
	public AdminLooper(int port) { //constructor takes port to differentiate between primary and secondary
		this.port = port;
	}
	
	public void run() {
		//System.out.println("In the admin looper thread");
		
		int q_len = 6;
		//int port = 5050;
		Socket sock;
		
		try {
			ServerSocket servsock = new ServerSocket(port, q_len);
			while(adminControlSwitch) {                             //block waiting
				sock = servsock.accept();
				new AdminWorker(sock).start();
			}
		}catch(IOException ioe) {System.out.println(ioe);}
	}
}

public class JokeServer {
	
	static boolean mode;//global var for mode
	static HashMap<String, boolean[]> map = new HashMap<>();//global hash table for storing state

	static String[] jokes =
		{
				"What did the flame say to his buddies after he fell in love? I found the perfect match!",
				"Did you hear about the bed bugs who fell in love? They're getting married in the spring.",
				"Why do skunks love Valentine's Day? Because they're scent-imental creatures!",
				"How are stars like false teeth? They both come out at night!"
		};
	static String[] proverbs = 
		{
				"The old horse in the stable still yearns to run.",
				"A spark can start a fire that burns the entire prairie.",
				"Give a man a fish and you feed him for a day; teach a man to fish and you feed him for a lifetime.",
				"Absence makes the heart grow fonder."
		};
	
	public static void main(String[] args) throws IOException {
		int q_len = 6;  
		int port = 4545;
		int admin_port = 5050;
		
		if (args.length > 0) {
			if(args[0].equals("secondary")) {
				port = 4546; 
				admin_port = 5051;
			}
		}
		
		Socket sock;
		
		AdminLooper AL = new AdminLooper(admin_port);
		Thread t = new Thread(AL);//thread constructor takes a target AL
		t.start();//listen for admin client
		
		ServerSocket servsock = new ServerSocket(port, q_len);
		
		System.out.printf("Yi's Joke server starting up, listening at port %d.\n",port);
		
		while (true) {
			
			sock = servsock.accept();
			//System.out.println("received something from client");
			
			new Worker(sock).start();
		}
	}
}
