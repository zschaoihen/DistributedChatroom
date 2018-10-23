package activitystreamer.util;

import java.time.Instant;

public class BufferedMessage{
	private Instant timestamp;
	private String message;

	public BufferedMessage(Instant timestamp, String message){
		this.timestamp = timestamp;
		this.message = message;
	}

	public Instant getTimestamp(){
		return this.timestamp;
	}

	public String getMsg(){
 		return this.message;
 	}
}