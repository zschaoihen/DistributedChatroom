package activitystreamer.util;

import activitystreamer.server.Connection;

/*
 *ClassName: ServerState
 *Version: 3.0
 *Authors: Zhao, Song, Fan and Zhang
 */
public class ServerState{
	private String id;
	private String hostname;
	private int port;
	private int load;
	private Connection con;
	private long sequenceNumber;

	/*
   *FunctionName: ServerState
	 *Parameter: id, load, hostname and port
	 *Return: Null
	 *Description: Constructed Function
	 */
	public ServerState(String id, String hostname, int port, 
		int load, Connection con, long sequenceNumber){
		this.id = id;
		this.hostname = hostname;
		this.port = port;
		this.load = load;
		this.con = con;
		this.sequenceNumber = sequenceNumber;
	}

	/*
   *FunctionName: getId
	 *Parameter: Null
	 *Return: id
	 *Description: get the id
	 */
	public String getId(){
		return this.id;
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

	public void setPort(int port){
		this.port = port;
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

	/*
   *FunctionName: setLoad
	 *Parameter: load
	 *Return: Null
	 *Description: set the load
	 */
	public void setLoad(int load){
		this.load = load;
	}

	public Connection getConnection(){
		return this.con;
	}

	public void setConnection(Connection con){
		this.con = con;
	}

	public void setNullConnect(){
		this.con = null;
	}

	public long getSN(){
		return this.sequenceNumber;
	}

	public void setSN(long sequenceNumber){
		this.sequenceNumber = sequenceNumber;
	}

}
