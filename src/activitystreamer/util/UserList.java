package activitystreamer.util;

import java.util.*;

public class UserList{
	private String command = "USERLIST";
	private String senderId;
	private long sequenceNumber;
	private Hashtable<String, String> users;

	public UserList(String senderId, long sequenceNumber, Hashtable<String, String> users){
		this.senderId = senderId;
		this.sequenceNumber = sequenceNumber;
		this.users = users;
	}

	public Hashtable<String, String> getUsers(){
		return this.users;
	}

	public long getSN(){
		return this.sequenceNumber;
	}

	public String getSenderId(){
		return this.senderId;
	}
}