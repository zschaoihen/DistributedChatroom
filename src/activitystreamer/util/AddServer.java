package activitystreamer.util;

import java.time.LocalDateTime;
import java.time.Duration;

public class AddServer{

	private String command = "ADD_SERVER";
	private String senderId;
	private long sequenceNumber;
	private String id;
	private String hostname;
	private int port;
	private int load;


	public AddServer(String senderId, long sequenceNumber, String id, 
		String hostname, int port, int load){
		this.senderId = senderId;
		this.sequenceNumber = sequenceNumber;
		this.id = id;
		this.hostname = hostname;
		this.port = port;
		this.load = load;
	}

	public String getCommand(){
		return this.command;
	}

	public String getId(){
		return this.id;
	}

	public String getSenderId(){
		return this.senderId;
	}

	public long getSN(){
		return this.sequenceNumber;
	}

	public String getHostname(){
		return this.hostname;
	}

	public int getPort(){
		return this.port;
	}

	public int getLoad(){
		return this.load;
	}
}