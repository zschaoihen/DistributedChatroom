package activitystreamer.util;
/*
 *ClassName: ServerAnnounce
 *Version: 3.0
 *Authors: Zhao, Song, Fan and Zhang
 */
public class ServerAnnounce{
	//the command is SERVER_ANNOUNCE
	private String command = "SERVER_ANNOUNCE";
	private String senderId;
	private long sequenceNumber;
	private String hostname;
	private int port;
	private int load;

	/*
   *FunctionName: ServerAnnounce
	 *Parameter: id, load, hostname and port
	 *Return: Null
	 *Description: Constructed Function
	 */
	public ServerAnnounce(String senderId, long sequenceNumber, int load , String hostname, 
		int port){
		this.senderId = senderId;
		this.sequenceNumber = sequenceNumber;
		this.load = load;
		this.hostname = hostname;
		this.port = port;
	}

	/*
   *FunctionName: getId
	 *Parameter: Null
	 *Return: id
	 *Description: get the id
	 */
	public String getSenderId(){
		return this.senderId;
	}

	public long getSN(){
		return this.sequenceNumber;
	}

	/*
   *FunctionName: getHostname
	 *Parameter: Null
	 *Return: hostname
	 *Description: get the hostname
	 */
	public String getHostname(){
		return this.hostname;
	}

	/*
   *FunctionName: getPort
	 *Parameter: Null
	 *Return: port
	 *Description: get the port
	 */
	public int getPort(){
		return this.port;
	}

	/*
   *FunctionName: getLoad
	 *Parameter: Null
	 *Return: load
	 *Description: get the load
	 */
	public int getLoad(){
		return this.load;
	}
}
