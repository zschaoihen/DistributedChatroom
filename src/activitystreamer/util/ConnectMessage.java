package activitystreamer.util;

public class ConnectMessage{
	private String command;
	private String senderId;
	private long sequenceNumber;
	private String lossId;

	public ConnectMessage(String command, String senderId, long sequenceNumber, String lossId){
		this.command = command;
		this.senderId = senderId;
		this.sequenceNumber = sequenceNumber;
		this.lossId = lossId;
	}

	public String getCommand(){
		return this.command;
	}

	public String getSenderId(){
		return this.senderId;
	}

	public long getSN(){
		return this.sequenceNumber;
	}

	public String getLossId(){
		return this.lossId;
	}
}