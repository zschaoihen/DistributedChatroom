package activitystreamer.util;

import java.util.ArrayList;

import activitystreamer.server.Connection;

/*
 *ClassName: UserDetail
 *Version: 3.0
 *Authors: Zhao, Song, Fan and Zhang
 */
public class UserDetail{
	//the connection from client
	private Connection con;
	//the number that the register request has been allowed
	private ArrayList<String> allowServer;

	/*
   *FunctionName: UserDetail
	 *Parameter: connection
	 *Return: Null
	 *Description: Constructed Function
	 */
	public UserDetail(Connection con){
		this.con = con;
		this.allowServer = new ArrayList<String>();
	}

	/*
   *FunctionName: getAllowCount
	 *Parameter: Null
	 *Return: allow count
	 *Description: get the number that the register request has been allowed
	 */
	public ArrayList<String> getAllowList(){
		return this.allowServer;
	}

	/*
   *FunctionName: setAllowCount
	 *Parameter: allow count
	 *Return: Null
	 *Description: set the number that the register request has been allowed
	 */
	public void addAllowServer(String id){
		this.allowServer.add(id);
	}

	/*
   *FunctionName: getConnection
	 *Parameter: Null
	 *Return: Connection
	 *Description: get the connection
	 */
	public Connection getConnection(){
		return con;
	}
}
