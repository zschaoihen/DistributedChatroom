package activitystreamer.util;

import org.json.simple.JSONObject;

/*
 *ClassName: ServerMessage
 *Version: 3.0
 *Authors: Zhao, Song, Fan and Zhang
 */
public class ServerMessage{
	private String command;
	private String info;
	private JSONObject activity = null;

	/*
   *FunctionName: ServerMessage
	 *Parameter: command and information
	 *Return: Null
	 *Description: Constructed Function
	 */
	public ServerMessage(String command, String info){
		this.command = command;
		//if command is ACTIVITY_BROADCAST, the information is activity		
		this.info =info;
	}

	@SuppressWarnings("unchecked")
	public ServerMessage(String command, JSONObject activity){
		this.command = command;
		this.activity = activity;
	}

	/*
   *FunctionName: getCommand
	 *Parameter: Null
	 *Return: command
	 *Description: get the command
	 */
	public String getCommand(){
		return this.command;
	}

	/*
   *FunctionName: getCommand
	 *Parameter: Null
	 *Return: information
	 *Description: get the information
	 */
	public String getInfo(){
		return this.info;
	}

	public JSONObject getActi(){
 		return this.activity;
 	}

}
