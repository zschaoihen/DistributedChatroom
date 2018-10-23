package activitystreamer.util;

public class AuthentiMessage{
	private String command;
	private String senderId;
	private Integer connect;
	private String secret;
	private String hostname;

	public AuthentiMessage(String command, String senderId, Integer connect, String secret){
		this.command = command;
		this.senderId = senderId;
		this.connect = connect;
		this.secret = secret;
	}

	public AuthentiMessage(String command, String senderId){
		this.command = command;
		this.senderId = senderId;
	}

	public AuthentiMessage(String command){
		this.command = command;
	}

	public String getCommand(){
		return this.command;
	}

	public String getSenderId(){
		return this.senderId;
	}

	public Integer getConnect(){
		return this.connect;
	}

	public String getSecret(){
		return this.secret;
	}
}