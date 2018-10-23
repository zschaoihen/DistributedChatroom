package activitystreamer.server;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.time.Instant;
import java.time.Duration;


import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import activitystreamer.util.*;

public class Control extends Thread {
	private static ArrayList<Connection> connections;
	private static boolean term=false;
	private static Listener listener;
	private static Gson gson = new Gson();
	private static ArrayList<Connection> clients;
	private static Hashtable<String, Connection> serverList;
	private static Hashtable<String, String> login;
	private static Hashtable<String, ServerState> announceTable;
	private static Hashtable<String, UserDetail> registerWaiting;
	private static Hashtable<Connection, Instant> loginTime;
	private static Hashtable<String, ArrayList<BufferedMessage>> messageBuffer;
	private static Hashtable<String, ServerState> notConnectServer;
	private static Hashtable<String, ArrayList<String>> connectLossList;
	private static String id = Settings.nextSecret();
	private static int ownLoad;
	private static JSONParser parser;
	private static int ownSN;

	protected static Control control = null;
	
	public static Control getInstance() {
		if(control==null){
			control=new Control();
		} 
		return control;
	}

	public Control() {
		parser = new JSONParser();
		// initialize the connections array
		connections = new ArrayList<Connection>();
		clients = new ArrayList<Connection>();
		serverList = new Hashtable<String, Connection>();
		announceTable = new Hashtable<String, ServerState>();
		registerWaiting = new Hashtable<String, UserDetail>();
		loginTime = new Hashtable<Connection, Instant>();
		messageBuffer = new Hashtable<String, ArrayList<BufferedMessage>>();
		notConnectServer = new Hashtable<String, ServerState>();
		login = new Hashtable<String, String>();
		connectLossList = new Hashtable<String, ArrayList<String>>();
		ownSN = 0;

		System.out.println("Initialising Connection");
		try {
			listener = new Listener();
		} catch (IOException e1) {
			System.out.println("failed to startup a listening thread: "+e1);
			System.exit(-1);
		}
		start();
	}

	@Override
	public void run(){
		initiateConnection(getOwnId(), announceTable.size(), Settings.getRemoteHostname(), Settings.getRemotePort());
		System.out.println("using activity interval of "+Settings.getActivityInterval()+" milliseconds");
		//term initialized as false, means that the connection maintains
		while(!term){
			// do something with 5 second intervals in between
			try {
				Thread.sleep(Settings.getActivityInterval());
			} catch (InterruptedException e) {
				System.out.println("received an interrupt, system is shutting down");
				break;
			}
			if(!term){
				System.out.println("doing activity");
				term=doActivity();
			}

		}
		//if the connection shut down, close these connections
		System.out.println("closing "+connections.size()+" connections");
		// clean up
		for(Connection connection : connections){
			connection.closeCon();
		}
		listener.setTerm(true);
		System.exit(-1);
	}

	public void initiateConnection(String senderId, int connect, String hostname, int port){
		// make a connection to another server if remote hostname is supplied
		if(hostname!=null){
			try {
				Socket s = new Socket();
				s.connect(new InetSocketAddress(hostname, port), 60000);
				//create the socket of TCP to connect the target server
				Connection serverCon = outgoingConnection(s);
				//create ServerMessage instance with AUTHENTICATE instruction
				AuthentiMessage authenticate = new AuthentiMessage("AUTHENTICATE", senderId, connect, Settings.getSecret());
				//send this JSON message
				serverCon.writeMsg(getJsonString(authenticate));
				//add this connection into connections list
				connections.add(serverCon);
			} catch (IOException e) {
				System.out.println("failed to make connection to "+Settings.getRemoteHostname()+":"+Settings.getRemotePort()+" :"+e);
				if(connect==0){
					System.exit(-1);
				}
			}
		}
	}

	public synchronized void connectionClosed(Connection con){
		//remove this connection
		if(!term) connections.remove(con);
	}

	public synchronized Connection incomingConnection(Socket s) throws IOException{
		System.out.println("incomming connection: "+Settings.socketAddress(s));
		//instantiate a new socket
		Connection c = new Connection(s);
		//add this connection into connections list
		connections.add(c);
		return c;
	}

	public synchronized Connection outgoingConnection(Socket s) throws IOException{
		System.out.println("outgoing connection: "+Settings.socketAddress(s));
		//instantiate a new socket
		Connection c = new Connection(s);
		//add this connection into connections list
		connections.add(c);
		return c;
	}

	public void sendConnectLoss(String oppoId){
		ConnectMessage cObj = new ConnectMessage("CONNECT_LOSS", getOwnId(), getOwnSN(), oppoId);
		Control.getInstance().multicast(true, oppoId, getJsonString(cObj));
	}

	public boolean checkRedirect(Connection con){
		boolean needRedirect = false;
		String redirectHost = null;
		int redirectPort = 0;
		//if the load of this server exceed more than 2 of other servers, redirect this connection request
		for(ServerState other: announceTable.values()){
			int oppoLoad = other.getLoad();
			if(oppoLoad >= 0){
				if((ownLoad - other.getLoad()) > 2){
					redirectHost = other.getHostname();
					redirectPort = other.getPort();
					needRedirect = true;
					break;
				}
			}
		}
		if(needRedirect){
			//create redirection message
			RedirectMessage redictObj = new RedirectMessage(redirectHost, redirectPort);
			con.writeMsg(getJsonString(redictObj));
			//remove this connection from connections list
			removeClient(con);
		}
		return needRedirect;
	}

	public static boolean alreadyExist(String username){
		return login.containsKey(username);
	}

	private static void buffMessage(String serverId, String msg){
		ArrayList<BufferedMessage> list = messageBuffer.get(serverId);
		list.add(new BufferedMessage(Instant.now(), msg));
	}

	private static void removeOldBuffer(String serverId){
		ArrayList<BufferedMessage> list = messageBuffer.get(serverId);
		Iterator<BufferedMessage> it = list.iterator();
		while (it.hasNext()) {
			BufferedMessage s = it.next();
			if (s.getTimestamp().isBefore(Instant.now().minus(Duration.ofSeconds(12)))){
				it.remove();
			} 
			else{
				break;
			}
		}
	}

	public synchronized void timeBroadcast(String msg, Instant sendTime){
		for(Map.Entry<Connection, Instant> entry: loginTime.entrySet()){
			if(entry.getValue().isBefore(sendTime)){
				entry.getKey().writeMsg(msg);
			}
		}
	}

	public synchronized void multicast(Connection con, String msg){
		for(Map.Entry<String, ServerState> entry: announceTable.entrySet()){
			Connection connection = entry.getValue().getConnection();
			String serverId = entry.getKey();
			if(connection!= null){
				if(con !=null && connection.equals(con)){
					continue;
				}
				connection.writeMsg(msg);
				buffMessage(serverId, msg);
				removeOldBuffer(serverId);
			}
			else{
				buffMessage(entry.getKey(), msg);
			}
		}
	}

	public synchronized void multicast(boolean b, String lossId, String msg){
		if(b){
			for(Map.Entry<String, ServerState> entry: announceTable.entrySet()){
				Connection connection = entry.getValue().getConnection();
				String serverId = entry.getKey();
				if(serverId.equals(lossId)){
					continue;
				}
				else{
					if(connection!= null){
						connection.writeMsg(msg);
						buffMessage(serverId, msg);
						removeOldBuffer(serverId);
					}
					else{
						buffMessage(entry.getKey(), msg);
					}
				}
			}
		}
		else{
			ArrayList<String> lossList = connectLossList.remove(lossId);
			for(String finder: lossList){
				Connection c = announceTable.get(finder).getConnection();
				if(c!= null){
					c.writeMsg(msg);
					buffMessage(finder, msg);
					removeOldBuffer(finder);
				}
				else{
					buffMessage(finder, msg);
				}
			}
		}
	}


	public synchronized boolean register(Connection con, String username, String secret){
		//record the client's username and secret
		login.put(username, secret);
		//if the server does not connect to any other servers
		if(serverList.isEmpty()){
			return true; //register successfully
		}
		else{
			//put this user into the waiting queue
			UserDetail waiting = new UserDetail(con);
			registerWaiting.put(username, waiting);
			//broadcast LOCK_REQUEST to all other servers
			LockMessage lockRequest = new LockMessage("LOCK_REQUEST", id, getOwnSN(), username, secret);
			multicast(null, getJsonString(lockRequest));
			return false;
		}
	}

	public static synchronized void addUser(String username, String secret){
		login.put(username, secret);
	}

	public static synchronized void deleteUser(String username, String secret){
		login.remove(username, secret);
	}

	public static UserDetail getRegiUer(String username){
		return registerWaiting.get(username);
	}

	public static Connection getRegiUserCon(String username){
		return registerWaiting.get(username).getConnection();
	}

	public static void removeRegiUser(String username){
		registerWaiting.remove(username);
	}

	public static Hashtable<String, ServerState> getAnnoTable(){
		return announceTable;
	}


	public static Hashtable<String, String> getLoginTable(){
		return login;
	}

	public static void annoNewServer(Connection con, String oppdId){
		if(announceTable.containsKey(oppdId)){
			ServerState oldState = announceTable.get(oppdId);
			con.setSN(oldState.getSN());
			notConnectServer.remove(oppdId);
			connectLossList.remove(oppdId);
			serverList.put(oppdId, con);
			retransferData(con, oppdId);
			oldState.setConnection(con);
		}
		else{
			ServerState newState = new ServerState(oppdId, con.getSocket().getInetAddress().getHostAddress(), 
				con.getSocket().getPort(), -1, con, -1);
			announceTable.put(oppdId, newState);
			serverList.put(oppdId, con);
			messageBuffer.put(oppdId, new ArrayList<BufferedMessage>());
			ServerAnnounce announceObj = new ServerAnnounce(getOwnId(), getOwnSN(), ownLoad, 
			Settings.getLocalHostname(), Settings.getLocalPort());
			con.writeMsg(getJsonString(announceObj));
		}
	}

	private static void retransferData(Connection con, String oppdId){
		ArrayList<BufferedMessage> messageList = messageBuffer.get(oppdId);
		for(BufferedMessage bMsg: messageList){
			con.writeMsg(bMsg.getMsg());
		}
	}

	public void updateAnnounce(ServerAnnounce announceMsg){
		//get the server'ID of this announce
		String serverId = announceMsg.getSenderId();
		//get the server' load of this announce
		int load = announceMsg.getLoad();
		announceTable.get(serverId).setLoad(load);
		announceTable.get(serverId).setPort(announceMsg.getPort());
	}


	public final ArrayList<Connection> getConnections(){
		return connections;
	}

	public boolean isClient(Connection con){
		if(clients.contains(con)){
			return true;
		}
		else{
			return false;
		}
	}

	public boolean isServer(Connection con){
		if(serverList.containsValue(con)){
			return true;
		}
		else{
			return false;
		}
	}

	public synchronized void addClient(Connection con){
		if(!term) {
			clients.add(con);
			loginTime.put(con, Instant.now());
		}
	}

	public synchronized void removeClient(Connection con){
		if(!term) {
			clients.remove(con);
			loginTime.remove(con);
			connectionClosed(con);
		}
	}

	public synchronized void removeServer(String serverId, Connection con){
		if(!term) {
			serverList.remove(serverId);
			connectionClosed(con);
		}
	}

	public synchronized void removeCon(Connection con, String oppoId){
		if(!term){
			if(isClient(con)){
				removeClient(con);
			}
			else if(isServer(con)){
				removeServer(oppoId, con);
			}
			else{
				connectionClosed(con);
			}
		}
	}

	public static void cutConnect(long n){
		int i = 0;
		for(Map.Entry<String, ServerState> entry: announceTable.entrySet()){
			if(n > i++){
				entry.getValue().getConnection().closeCon();
			}
			else{
				break;
			}
		}
	}

	public synchronized static int getOwnSN(){
		return ownSN++;
	}

	public static boolean loginAllow(String username, String secret){
		if(login.containsKey(username) && login.get(username).equals(secret)){
			return true;
		}
		else{
			return false;
		}
	}

	public synchronized static void updateNotConnect(String lossId, String senderId){
		if(connectLossList.containsKey(lossId)){
			ArrayList<String> requestList = connectLossList.get(lossId);
			if(!requestList.contains(senderId)){
				requestList.add(senderId);
			}
		}
		else{
			ArrayList<String> requestList = new ArrayList<String>();
			requestList.add(senderId);
			connectLossList.put(lossId, requestList);
		}
	}

	public synchronized static void checkAllConnect(String lossId){
		boolean isGone = true;
		ArrayList<String> requestList = connectLossList.get(lossId);
		if(requestList.contains(getOwnId())){
			for(Map.Entry<String, Connection> entry: serverList.entrySet()){
				if(!requestList.contains(entry.getKey())){
					isGone = false;
					break;
				}
			}
		}
		else{
			isGone = false;
		}
		if(isGone){
			ConnectMessage cObj = new ConnectMessage("SERVER_ELIMINATE", 
				getOwnId(), getOwnSN(), lossId);
			Control.getInstance().multicast(true, lossId, getJsonString(cObj));
			removeAll(lossId);
			checkAllLock();
		}
	}

	public  static boolean checkLoss(String lossId){
		if(notConnectServer.containsKey(lossId)){
			return true;
		}
		else{
			return false;
		}
	}

	public static void putInLoss(String lossId){
		serverList.remove(lossId);
		notConnectServer.put(lossId, announceTable.get(lossId));
		announceTable.get(lossId).setNullConnect();
		Control.getInstance().sendConnectLoss(lossId);
	}

	public static boolean checkLossList(String lossId){
		if(connectLossList.containsKey(lossId)){
			return true;
		}
		else{
			return false;
		}
	}

	public static void removeAll(String lossId){
		if(announceTable.containsKey(lossId)){
			announceTable.remove(lossId);
			messageBuffer.remove(lossId);
			notConnectServer.remove(lossId);
			connectLossList.remove(lossId);
			for(Map.Entry<String, ArrayList<String>> entry: connectLossList.entrySet()){
				entry.getValue().remove(lossId);
			}
		}
		
	}

	public synchronized static void checkAllLock(){
		for(Map.Entry<String, UserDetail> entry: registerWaiting.entrySet()){
			UserDetail waiting = entry.getValue();
			String username = entry.getKey();
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
	}

	public boolean doActivity(){
		//get the number of connected clients
		ownLoad = clients.size();
		//the announcement includes id, hostname, load and port
		ServerAnnounce announceObj = new ServerAnnounce(getOwnId(), getOwnSN(), ownLoad, 
			Settings.getLocalHostname(), Settings.getLocalPort());
		//broadcast this Json type announcement to other servers
		multicast(null, getJsonString(announceObj));
		return false;
	}

	public synchronized static String getOwnId(){
		return Control.id;
	}

	public static long getServerSN(String senderId){
		return announceTable.get(senderId).getSN();
	}

	public static void setServerSN(String senderId, long sn){
		announceTable.get(senderId).setSN(sn);
	}

	public final void setTerm(boolean t){
		term=t;
	}

	private static <T> String getJsonString(T msfObj){
		String msg = gson.toJson(msfObj);
		return msg;
	}
}