package activitystreamer.util;

import java.util.TimerTask;
import activitystreamer.server.Connection;

public class CutConnection extends TimerTask{
	private Connection con;

	public CutConnection(Connection con){
		this.con = con;
	}

	@Override
    public void run() {
        this.con.closeCon();
    }
}