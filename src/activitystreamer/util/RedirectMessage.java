package activitystreamer.util;

/*
 *ClassName: RedirectMessage
 *Version: 3.0
 *Authors: Zhao, Song, Fan and Zhang
 */
public class RedirectMessage{
	//the command is REDIRECT
	private String command = "REDIRECT";
	private String hostname;
	private int port;


	/*
   *FunctionName: RedirectMessage
	 *Parameter: hostname and port
	 *Return: Null
	 *Description: Constructed Function
	 */
	public RedirectMessage(String hostname, int port){
		this.hostname = hostname;
		this.port = port;
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
}
