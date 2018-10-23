package activitystreamer.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import activitystreamer.util.Settings;

/*
 *ClassName: Listener
 *Version: 3.0
 *Authors: Zhao, Song, Fan and Zhang
 */
public class Listener extends Thread{
	//create the socket
	private ServerSocket serverSocket=null;
	//use term to control a status of connection
	private boolean term = false;
	//represent the prot
	private int portnum;

	/*
   *FunctionName: Listener
	 *Parameter: Null
	 *Return: Null
	 *Description: Constructed Function
	 */
	public Listener() throws IOException{
		// keep our own copy in case it changes later
		portnum = Settings.getLocalPort();
		serverSocket = new ServerSocket(portnum);
		serverSocket.setSoTimeout(60000);
		start();
	}

	/*
   *FunctionName: run
	 *Parameter: null
	 *Return: Null
	 *Description: run the server and set a regular sleeping interval
	 */
	@Override
	public void run() {
		System.out.println("listening for new connections on "+portnum);
		while(!term){
			Socket clientSocket;
			try {
				//establish the connection
				clientSocket = serverSocket.accept();
				Control.getInstance().incomingConnection(clientSocket);
			} catch (IOException e) {
				System.out.println("received exception, shutting down");
				term=true;
			}
		}
	}

	/*
	 *FunctionName: setTerm
	 *Parameter: true or false
	 *Return: null
	 *Description: announce set term with true or false which controls the connection status
	 */
	public void setTerm(boolean term) {
		this.term = term;
		if(term) interrupt();
	}

}
