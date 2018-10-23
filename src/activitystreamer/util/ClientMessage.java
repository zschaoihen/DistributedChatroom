package activitystreamer.util;

import org.json.simple.JSONObject;

/*
 *ClassName: ClientMessage
 *Version: 3.0
 *Authors: Zhao, Song, Fan and Zhang
 */
public class ClientMessage{
	private String command;
	private String username;
	private String secret;
	private JSONObject activity = null;

	/*
   *FunctionName: ClientMessage
	 *Parameter: command, username, secret and activity
	 *Return: Null
	 *Description: Constructed Function
	 */
	 @SuppressWarnings("unchecked")
	 public ClientMessage(String command, String username, String secret, JSONObject obj){
 		this.command = command;
 		this.username = username;
 		this.secret = secret;
 		this.activity = obj;
 	}

	/*
   *FunctionName: ClientMessage
	 *Parameter: command, username, secret
	 *Return: Null
	 *Description: Constructed Function
	 */
	public ClientMessage(String command, String username, String secret){
		this.command = command;
		this.username = username;
		this.secret = secret;
	}

	/*
   *FunctionName: ClientMessage
	 *Parameter: command
	 *Return: Null
	 *Description: Constructed Function
	 */
	 public ClientMessage(String command){
		 this.command = command;
	 }

	/*
   *FunctionName: getCommand
	 *Parameter: Null
	 *Return: command
	 *Description: get the command
	 */
	public String getCommand(){
		return command;
	}

	/*
   *FunctionName: getUsername
	 *Parameter: Null
	 *Return: username
	 *Description: get the username
	 */
	public String getUsername(){
		return username;
	}

	/*
   *FunctionName: getSecret
	 *Parameter: Null
	 *Return: secret
	 *Description: get the secret
	 */
	public String getSecret(){
		return secret;
	}

	/*
   *FunctionName: getActi
	 *Parameter: Null
	 *Return: activity
	 *Description: get the activity
	 */
	 public JSONObject getActi(){
 		return activity;
 	}
}
