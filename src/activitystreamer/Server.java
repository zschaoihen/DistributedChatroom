package activitystreamer;


import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


import activitystreamer.server.Control;
import activitystreamer.util.Settings;

/*
 *ClassName: Server
 *Version: 3.0
 *Authors: Zhao, Song, Fan and Zhang
 */
public class Server {

	private static void help(Options options){
		String header = "An ActivityStream Server for Unimelb COMP90015\n\n";
		String footer = "\ncontact aharwood@unimelb.edu.au for issues.";
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("ActivityStreamer.Server", header, options, footer, true);
		System.exit(-1);
	}

	/*
   *FunctionName: main
	 *Parameter: args
	 *Return: Null
	 *Description: scan options in CMD args, and set initial parameters in Settings
	 *Cmd: activitystreamer.Client -lp (localPort) -rh (remoteHost) -rp (remotePort)
	 *     -lh (localHost) -a (activity interval in milliseconds) -s (secret)
	 */
	public static void main(String[] args) {

		System.out.println("reading command line options");

		Options options = new Options();
		options.addOption("lp",true,"local port number");
		options.addOption("rp",true,"remote port number");
		options.addOption("rh",true,"remote hostname");
		options.addOption("lh",true,"local hostname");
		options.addOption("a",true,"activity interval in milliseconds");
		options.addOption("s",true,"secret for the server to use");


		// build the parser
		CommandLineParser parser = new DefaultParser();

		CommandLine cmd = null;
		try {
			cmd = parser.parse( options, args);
		} catch (ParseException e1) {
			help(options);
		}

		//set local port name in Settings
		if(cmd.hasOption("lp")){
			try{
				int port = Integer.parseInt(cmd.getOptionValue("lp"));
				Settings.setLocalPort(port);
			} catch (NumberFormatException e){
				System.out.println("-lp requires a port number, parsed: "+cmd.getOptionValue("lp"));
				help(options);
			}
		}

		//set remote host name in Settings
		if(cmd.hasOption("rh")){
			Settings.setRemoteHostname(cmd.getOptionValue("rh"));
		}

		//set remote port name in Settings
		if(cmd.hasOption("rp")){
			try{
				int port = Integer.parseInt(cmd.getOptionValue("rp"));
				Settings.setRemotePort(port);
			} catch (NumberFormatException e){
				System.out.println("-rp requires a port number, parsed: "+cmd.getOptionValue("rp"));
				help(options);
			}
		}

		//set activity interval in milliseconds in Settings
		if(cmd.hasOption("a")){
			try{
				int a = Integer.parseInt(cmd.getOptionValue("a"));
				Settings.setActivityInterval(a);
			} catch (NumberFormatException e){
				System.out.println("-a requires a number in milliseconds, parsed: "+cmd.getOptionValue("a"));
				help(options);
			}
		}

		try {
			Settings.setLocalHostname(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			System.out.println("failed to get localhost IP address");
		}

		//set local port name in Settings
		if(cmd.hasOption("lh")){
			Settings.setLocalHostname(cmd.getOptionValue("lh"));
		}

		//set secret in Settings
		if(cmd.hasOption("s")){
			Settings.setSecret(cmd.getOptionValue("s"));
		}

		System.out.println("starting server");

		//instantiate Control for servers
		final Control c = Control.getInstance();
		// the following shutdown hook doesn't really work, it doesn't give us enough time to
		// cleanup all of our connections before the jvm is terminated.
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				c.setTerm(true);
				c.interrupt();
		    }
		 });
	}

}
