package activitystreamer.util;

import java.time.LocalDateTime;

import org.json.simple.JSONObject;

public class BroadToClient{
	private String command = "ACTIVITY_BROADCAST";
	private JSONObject activity;

	@SuppressWarnings("unchecked")
	public BroadToClient(JSONObject activity){
		this.activity = activity;
	}

	public String getCommand(){
		return this.command;
	}

	public JSONObject getActi(){
 		return this.activity;
 	}
}