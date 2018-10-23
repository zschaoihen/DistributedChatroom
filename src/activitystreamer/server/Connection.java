package activitystreamer.server;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import activitystreamer.util.Settings;

/*
 *ClassName: Connection
 *Version: 3.0
 *Authors: Zhao, Song, Fan and Zhang
 */
public class Connection extends Thread {
	//state the initializion of a input data stream
	private DataInputStream in;
	//state the initializion of a output data stream
	private DataOutputStream out;
	//state the initializion of a buffer reader
	private BufferedReader inreader;
	//state the initializion of a print writer
	private PrintWriter outwriter;
	//represent whether the connection is openning
	private boolean open = false;
	//create the socket
	private Socket socket;
	//use term to control a status of connection
	private boolean term=false;
	////state the initializion of process class
	private Process bindThis;
	private String oppoId = "";
	private boolean normal = true;
	/*
   *FunctionName: Connection
	 *Parameter: socket
	 *Return: Null
	 *Description: Constructed Function
	 */
	Connection(Socket socket) throws IOException{
		//initialize parameters
		in = new DataInputStream(socket.getInputStream());
	    out = new DataOutputStream(socket.getOutputStream());
	    inreader = new BufferedReader( new InputStreamReader(in));
	    outwriter = new PrintWriter(out, true);
	    this.socket = socket;
	    open = true;
	    bindThis = new Process(this);
	    start();
	}

	 /*
    *FunctionName: writeMsg
 	 *Parameter: message
 	 *Return: true or false
 	 *Description: returns true if the message was written, otherwise false
 	 */
	public boolean writeMsg(String msg) {
		if(open){
			outwriter.println(msg);
			outwriter.flush();
			return true;
		}
		return false;
	}

	public void setSN(long oppoSN){
		this.bindThis.setSN(oppoSN);
	}

	/*
	 *FunctionName: closeCon
	*Parameter: Null
	*Return: Null
	*Description: close the connection
	*/
	public void closeCon(){
		if(open){
			System.out.println("closing connection "+Settings.socketAddress(socket));
			try {
				term=true;
				bindThis.setTerm(true);
				inreader.close();
				out.close();
			} catch (IOException e) {
				// already closed?
				System.out.println("received exception closing the connection "+Settings.socketAddress(socket)+": "+e);
			}
		}
	}

	/*
   *FunctionName: run
	 *Parameter: null
	 *Return: Null
	 *Description: run the server and set a regular sleeping interval
	 */
	public void run(){
		try {
			//judge whether the connection is established
			String data;
			while(!term && (data = inreader.readLine())!=null){
				term = bindThis.msgProcess(data);
			}
			System.out.println("connection closed to "+Settings.socketAddress(socket));
			in.close();
			if(!normal){
				Control.updateNotConnect(this.oppoId, Control.getOwnId());
				Control.putInLoss(this.oppoId);
			}
			Control.getInstance().connectionClosed(this);
		} catch (IOException e) {
			System.out.println("connection "+Settings.socketAddress(socket)+" closed with exception: "+e);
			if(!normal){
				Control.updateNotConnect(this.oppoId, Control.getOwnId());
				Control.putInLoss(this.oppoId);
			}
			Control.getInstance().connectionClosed(this);
		}
		open=false;
	}

	public void setNormal(boolean normal){
		this.normal = normal;
	}

	/*
	 *FunctionName: getSocket
	 *Parameter: Null
	 *Return: Null
	 *Description: get the socket
	 */
	public Socket getSocket() {
		return socket;
	}

	/*
	 *FunctionName: isOpen
	 *Parameter: Null
	 *Return: Null
	 *Description: get the status of connection
	 */
	public boolean isOpen() {
		return open;
	}

	public String getOppoId(){
		return this.oppoId;
	}

	public void setOppoId(String oppoId){
		this.oppoId = oppoId;
	}
}
