package activitystreamer.util;

public class LockMessage{
	private String command;
	private String senderId;
	private long sequenceNumber;
	private String username;
	private String secret;

	/*
   *FunctionName: ServerMessage
	 *Parameter: command, username and secret
	 *Return: Null
	 *Description: Constructed Function
	 */
	public LockMessage(String command, String senderId, long sequenceNumber, String username, String secret){
		this.command = command;
		this.senderId = senderId;
		this.sequenceNumber = sequenceNumber;
		this.username = username;
		this.secret = secret;
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

	public String getSenderId(){
		return this.senderId;
	}

	public long getSN(){
		return this.sequenceNumber;
	}

	/*
   *FunctionName: getCommand
	 *Parameter: Null
	 *Return: username
	 *Description: get the username
	 */
	public String getUsername(){
		return this.username;
	}

	/*
   *FunctionName: getCommand
	 *Parameter: Null
	 *Return: secret
	 *Description: get the secret
	 */
	public String getSecret(){
		return this.secret;
	}


}