package activitystreamer.server;

import java.io.IOException;
import java.util.Hashtable;
import java.time.Instant;
import java.time.Duration;
import java.util.Timer;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.json.simple.JSONObject;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import activitystreamer.util.*;

public class Process extends Thread{
	private Connection con;
	private boolean term = false;
	private long oppoSN;
	private Timer timer = null;
	private static JsonParser parser;
	private static Gson gson;

	public Process(Connection con){
		this.con = con;
		this.oppoSN = 0;
		parser = new JsonParser();
		gson = new Gson();
		start();
	}

	@Override
	public void run(){
		while(!term){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	public boolean msgProcess(String msg){
		System.out.println(msg);
		boolean tempTerm = false;
		//connection is open
		if(!term){
			try{
				boolean isSpec = false;

				JsonObject msgObj = getObject(msg);
				//get the command
				String command = parseCommand(msgObj);
				switch(command){
					case "INVALID_MESSAGE":
						Control.getInstance().setTerm(true);
						tempTerm = true;
						isSpec = true;
						break;
					case "LOGOUT":
						Control.getInstance().removeClient(con);
						tempTerm = true;
						isSpec = true;
						break;
					default:
						break;
				}
				if(!isSpec){
					if(Control.getInstance().isServer(con)){
						//process the server mesage
						tempTerm = processServer(command, msg, msgObj);
						this.resetTimer();
					}
					//process the client message
					else if(Control.getInstance().isClient(con)){
						tempTerm = processClient(command, msg, msgObj);
					}
					/*
					 *the first message to determine whether the connection
					 *is from a client or a server
					 */
					else {
						tempTerm = firstMessageProcess(command, msg, msgObj);
					}
					return tempTerm;
				}
			} catch(ParseException e){
				System.out.println(e);
				responseInfo("INVALID_MESSAGE", "Meaningless message");
				Control.getInstance().removeCon(con, con.getOppoId());
				return true;
			}
		}
		return tempTerm;
	}

	public boolean isDuplicate(long sn, String senderId){
		if(sn <= Control.getServerSN(senderId)){
			return true;
		}
		else{
			return false;
		}
	}

	private boolean processServer(String command, String msg, JsonObject msgObj) throws JsonParseException, ParseException{
		boolean tempTerm = false;
		long msgSN = parseSN(msgObj);
		String senderId = parseId(msgObj);
		String hostname;
		int load;
		int port;
		if(!isDuplicate(msgSN, senderId)){
			if(command.equals("SERVER_ANNOUNCE")){
				hostname = (String) (msgObj.get("hostname")).getAsString();
				load = (int) (msgObj.get("load")).getAsInt();
				port = (int) (msgObj.get("port")).getAsInt();
				ServerAnnounce announceMsg = new ServerAnnounce(senderId, msgSN, load, hostname, port);
				Control.getInstance().updateAnnounce(announceMsg);
			}
			else if(command.equals("ACTIVITY_BROADCAST")){
				BroadcastMessage braodMsg = gson.fromJson(msg, BroadcastMessage.class);
				Instant timestamp = braodMsg.getTimestamp();
				BroadToClient bTCObj = new BroadToClient(braodMsg.getActi());
				Control.getInstance().timeBroadcast(getJsonString(bTCObj), timestamp);
			}
			else if(command.equals("USERLIST")){
				UserList uMsg = gson.fromJson(msg, UserList.class);
				Hashtable<String, String> users = uMsg.getUsers();
				for(Map.Entry<String, String> entry: users.entrySet()){
					Control.addUser(entry.getKey(), entry.getValue());
				}
			}
			else if(command.equals("ADD_SERVER")){
				hostname = (String) (msgObj.get("hostname")).getAsString();
				port = (int) (msgObj.get("port")).getAsInt();
				Control.getInstance().initiateConnection(Control.getOwnId(), Control.getAnnoTable().size(), hostname, port);
			}
			else{
				boolean isConnect = false;
				boolean isLock = false;
				switch(command){
					case "LOCK_REQUEST":
					case "LOCK_DENIED":
					case "LOCK_ALLOWED":
					case "CANCEL_LOCK":
						isLock = true;
						break;
					case "CONNECT_FOUND":
					case "CONNECT_LOSS":
					case "SERVER_ELIMINATE":
						isConnect = true;
						break;
				}
				if(isLock){
					String username = (String) (msgObj.get("username")).getAsString();
					String secret = (String) (msgObj.get("secret")).getAsString();
					switch(command){
						case "LOCK_REQUEST":
							LockMessage lockResponse;
							//judge if the user has registered here
							if(!Control.getLoginTable().containsKey(username)){
								//add the user infomation and send LOCK_ALLOWED
								Control.addUser(username, secret);
								lockResponse = new LockMessage("LOCK_ALLOWED", Control.getOwnId(), Control.getOwnSN(), username, secret);
							}
							else{
								//deny the register
								lockResponse = new LockMessage("LOCK_DENIED", Control.getOwnId(), Control.getOwnSN(), username, secret);
							}
							con.writeMsg(getJsonString(lockResponse));
						break;
						case "LOCK_DENIED":
							String info = String.format("%s is already registered with the system", username);
							ServerMessage regiObj = new ServerMessage("REGISTER_FAILED", info);
							Control.getRegiUserCon(username).writeMsg(getJsonString(regiObj));
							Control.removeRegiUser(username);
							Control.deleteUser(username, secret);
							LockMessage lockObj = new LockMessage("CANCEL_LOCK", Control.getOwnId(), Control.getOwnSN(), username, secret);
							Control.getInstance().multicast(con, getJsonString(lockObj));
							break;
						case "LOCK_ALLOWED":
							addAllowServer(username, senderId);
							checkLock(username);
							break;
						case "CANCEL_LOCK":
							Control.deleteUser(username, secret);
							break;
					}
				}
				else if(isConnect){
					ConnectMessage conMsg = gson.fromJson(msg, ConnectMessage.class);
					String lossId = conMsg.getLossId();
					switch(command){
						case "CONNECT_FOUND":
							if(Control.checkLoss(lossId)){
								ServerState oppoServer = Control.getAnnoTable().get(lossId);
								Control.getInstance().initiateConnection(Control.getOwnId(), Control.getAnnoTable().size(),
									oppoServer.getHostname(), oppoServer.getPort());
							}
							break;
						case "CONNECT_LOSS":
							Control.updateNotConnect(lossId, senderId);
							Control.checkAllConnect(lossId);
							break;
						case "SERVER_ELIMINATE":
							Control.removeAll(lossId);
							Control.checkAllLock();
							break;
					}
				}
				else{
					responseInfo("INVALID_MESSAGE", "Wrong command");
					tempTerm = true;
				}
			}
			Control.setServerSN(senderId, msgSN);
			if(tempTerm){
				Control.getInstance().removeServer(senderId, con);
			}
		}
		return tempTerm;
	}


	@SuppressWarnings("unchecked")
	private boolean processClient(String command, String msg, JsonObject msgObj) throws JsonParseException, ParseException{
		//get the username and secret of the client
		boolean tempTerm = false;
	
		ClientMessage clientMsg = gson.fromJson(msg, ClientMessage.class);
		String username = clientMsg.getUsername();
		String secret = clientMsg.getSecret();
		switch(command){
			case "ACTIVITY_MESSAGE":
				//get the activity
				JSONObject clientActi = clientMsg.getActi();
				clientActi.put("authenticated_user", username);
				ServerMessage actiBroadObj = new ServerMessage("ACTIVITY_BROADCAST", clientActi);
				String atciBroadMsg = getJsonString(actiBroadObj);
				//broadcast this activity
				Control.getInstance().timeBroadcast(atciBroadMsg, Instant.now());
				BroadcastMessage broadObj = new BroadcastMessage("ACTIVITY_BROADCAST", Control.getOwnId(), 
					Instant.now(), Control.getOwnSN(),clientActi);
				Control.getInstance().multicast(null, getJsonString(broadObj));
				break;
			//the command is LOGIN
			case "LOGIN":
				//process by username and secret
				tempTerm = processLogin(username, secret);
				break;
			default:
				responseInfo("INVALID_MESSAGE", "Wrong command");
				tempTerm = true;
		}
		if(tempTerm){
			Control.getInstance().removeClient(con);
		}
		return tempTerm;
	}

	private boolean firstMessageProcess(String command, String msg, JsonObject msgObj) throws JsonParseException, ParseException{
		boolean isClientCommand = false;
		boolean isServerCommand = false;
		boolean isSpecialCommand = false;
		boolean tempTerm = false;
		switch(command){
			//REGISTER and LOGIN comes from a client
			case "REGISTER":
			case "LOGIN":
				isClientCommand = true;
				break;
			//AUTHENTICATE comes from a server
			case "AUTHENTICATE":
			case "AUTHENTICATION_FAIL":
			case "AUTHENTICATION_SUCCESS":
				isServerCommand = true;
				break;
			case "CUT":
				isSpecialCommand = true;
				break;
			case "DUPLICATE_CONNECT":
				tempTerm = true;
				break;
			default:
				responseInfo("INVALID_MESSAGE", "Wrong command");
				tempTerm = true;
		}
		//the connection comes from a client
		if(isClientCommand){
			//get the username and secret
			String username = (String) (msgObj.get("username")).getAsString();
			String secret;
			//the command is REGISTER
			if(command.equals("REGISTER")){
				//no username
				if(username == null){
					responseInfo("INVALID_MESSAGE", "There is not a username in the message.");
					tempTerm = true;
				}
				//username is anonymous
				else if(username.equals("anonymous")){
					responseInfo("REGISTER_FAILED", "Cannot register for anonymous username.");
					tempTerm = true;
				}
				else {
					//check whether the user had successfully registered
					if(Control.alreadyExist(username)){
						String info = String.format("%s is already registered with the system", username);
						responseInfo("REGISTER_FAILED", info);
						tempTerm = true;
					}
					else {
						secret = (String) (msgObj.get("secret")).getAsString();
						if(Control.getInstance().register(con, username, secret)){
							String info = String.format("register success for %s", username);
							responseInfo("REGISTER_SUCCESS", info);
							Control.getInstance().addClient(con);
						}
					}
				}
			}
			else{
				//login with this username and secret
				if(username.equals("anonymous")){
					secret = null;
				}
				else{
					secret = (String) (msgObj.get("secret")).getAsString();
				}
				tempTerm = processLogin(username, secret);
				if(!tempTerm){
					//add this client into related connection list
					Control.getInstance().addClient(con);
				}
			}
		}
		//the connection comes from a server
		else if(isServerCommand){
			//get the secret and judge if its correct
			AuthentiMessage authMsg = gson.fromJson(msg, AuthentiMessage.class);
			String oppoId;
			switch(command){
				case "AUTHENTICATE":
					oppoId = authMsg.getSenderId();
					int connectionNumber = authMsg.getConnect();
					String serverSecret = authMsg.getSecret();
					tempTerm = authenticateServer(oppoId, connectionNumber, serverSecret);
					if(!tempTerm){
						con.setOppoId(oppoId);
						Control.annoNewServer(con, oppoId);
						con.setNormal(false);
						this.startTimer();
					}
					break;
				case "AUTHENTICATION_FAIL":
					System.out.println("wrong secret for the server");
					Control.getInstance().setTerm(true);
					tempTerm = true;
					break;
				case "AUTHENTICATION_SUCCESS":
					oppoId = authMsg.getSenderId();
					con.setOppoId(oppoId);
					Control.annoNewServer(con, oppoId);
					con.setNormal(false);
					this.startTimer();
					break;
			}
		}
		else if(isSpecialCommand){
			long n = parseNumber(msgObj);
			Control.cutConnect(n);
		}
		//the incoming server is legal
		if(tempTerm){
			Control.getInstance().connectionClosed(con);
		}
		return tempTerm;
	}

	private boolean processLogin(String username, String secret){
		//check whether the user has registered successfully
		if(checkLogin(username, secret)){
			String info = String.format("logged in as user %s", username);
			responseInfo("LOGIN_SUCCESS", info);

			//check whether the client need to redirect
			if(Control.getInstance().checkRedirect(con)){
				return true;
			}
			return false;
		}
		else{
			String info = String.format("attempt to login with wrong secret: %s", secret);
			responseInfo("LOGIN_FAILED", info);
			return true;
		}
	}

	private boolean checkLogin(String username, String secret){
		if (username.equals("anonymous")){
			return true;
		}
		//whether the user's infomation legal
		else if(Control.loginAllow(username, secret)){
			return true;
		}
		return false;
	}

	private synchronized void addAllowServer(String username, String serverId){
		Control.getRegiUer(username).addAllowServer(serverId);
	}

	private synchronized void checkLock(String username){
		UserDetail waiting = Control.getRegiUer(username);
		ArrayList<String> allowServer = waiting.getAllowList();
		boolean isSuceess = true;
		for(String s: Control.getAnnoTable().keySet()){
			if(!allowServer.contains(s)){
				isSuceess = false;
				break;
			}
		}
		if(isSuceess){
			Connection clientCon = waiting.getConnection();
			Control.getInstance().addClient(clientCon);
			Control.removeRegiUser(username);
			String info = String.format("register success for %s", username);
			clientCon.writeMsg(getJsonString(new ServerMessage("REGISTER_SUCCESS", info)));
		}
	}

	private boolean authenticateServer(String senderId, int connectionNumber, String serverSecret){
		if (!serverSecret.equals(Settings.getSecret())){
			AuthentiMessage authObj = new AuthentiMessage("AUTHENTICATION_FAIL");
			con.writeMsg(getJsonString(authObj));
			return true;
		}
		else{
			if(Control.getAnnoTable().get(senderId)!=null && Control.getAnnoTable().get(senderId).getConnection()!=null){
				AuthentiMessage dupObj = new AuthentiMessage("DUPLICATE_CONNECT", Control.getOwnId());
				con.writeMsg(getJsonString(dupObj));
				return true;
			}
			else{
				AuthentiMessage authObj = new AuthentiMessage("AUTHENTICATION_SUCCESS", Control.getOwnId());
				con.writeMsg(getJsonString(authObj));
				if(connectionNumber == 0){
					UserList uObj = new UserList(Control.getOwnId(), Control.getOwnSN(), Control.getLoginTable());
					con.writeMsg(getJsonString(uObj));
					Hashtable<String, ServerState> announceTable = Control.getAnnoTable();
					for(Map.Entry<String, ServerState> entry: announceTable.entrySet()){
						ServerState s = entry.getValue();
						String oppoId = s.getId();
						AddServer redictObj = new AddServer(Control.getOwnId(), Control.getOwnSN(), 
							oppoId, s.getHostname(), s.getPort(), s.getLoad());
						con.writeMsg(getJsonString(redictObj));
					}
				}
				return false;
			}
		}
	}

	public final void setTerm(boolean t){
		term=t;
	}

	public void setSN(long oppoSN){
		this.oppoSN = oppoSN;
	}

	private <T> String getJsonString(T msfObj){
		String msg = gson.toJson(msfObj);
		return msg;
	}

	private void responseInfo(String command, String info){
		ServerMessage responseObj = new ServerMessage(command, info);
		System.out.println(info);
		con.writeMsg(getJsonString(responseObj));
	}

	private JsonObject getObject(String msg){
		JsonObject msgObj = (JsonObject) parser.parse(msg);
		return msgObj;
	}

	private String parseCommand(JsonObject msgObj) throws ParseException{
		String command = (String) (msgObj.get("command")).getAsString();
		return command;
	}

	private long parseSN(JsonObject msgObj) throws ParseException{
		long sn = (Long) (msgObj.get("sequenceNumber")).getAsLong();
		return sn;
	}

	private String parseId(JsonObject msgObj) throws ParseException{
		String senderId = (String) (msgObj.get("senderId")).getAsString();
		return senderId;
	}

	private long parseNumber(JsonObject msgObj) throws ParseException{
		long number = (Long) (msgObj.get("n")).getAsLong();
		return number;
	}

	private void startTimer(){
		timer = new Timer();
		this.timer.schedule(new CutConnection(this.con), 10000);
	}

	private void resetTimer(){
		this.timer.cancel();
		this.timer.purge();
		this.timer = new Timer();
		String oppoId = con.getOppoId();
		if(Control.checkLossList(oppoId)){
			ConnectMessage cObj = new ConnectMessage("CONNECT_FOUND", Control.getOwnId(), Control.getOwnSN(), oppoId);
			Control.getInstance().multicast(false, oppoId, getJsonString(cObj));
		}
		this.timer.schedule(new CutConnection(this.con), 10000);
	}
}