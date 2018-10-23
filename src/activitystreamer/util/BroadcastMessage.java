package activitystreamer.util;

import java.time.Instant;

import org.json.simple.JSONObject;

public class BroadcastMessage{
	private String command;
	private String senderId;
	private Instant timestamp = null;
	private long sequenceNumber;
	private JSONObject activity;

	@SuppressWarnings("unchecked")
	public BroadcastMessage(String command, String senderId, Instant timestamp, long sequenceNumber, JSONObject activity){
		this.command = command;
		this.senderId = senderId;
		this.timestamp = timestamp;
		this.sequenceNumber = sequenceNumber;
		this.activity = activity;
	}

	public String getCommand(){
		return this.command;
	}

	public String getSenderId(){
		return this.senderId;
	}

	public Instant getTimestamp(){
		return this.timestamp;
	}

	public long getSN(){
		return this.sequenceNumber;
	}

	public JSONObject getActi(){
 		return this.activity;
 	}
}