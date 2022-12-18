package cs451;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import cs451.la.LatticeAgreement;
import cs451.links.PerfectLink;


class Process implements Observer{
	private final int id;
	private final String ip;
	private final int port;
	private DatagramSocket socket;
	private final List<Host> hosts;
	private final String[] config;
	private List<LatticeAgreement> latticeAgreements;
	
	private ArrayList<Message> deliveredMessages;
	private final String logFilename;
		
	private ConcurrentLinkedQueue<String> logs;
	
	Process(int id, String ip, int port, List<Host> hosts, String[] config, String logFilename){
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.hosts = hosts;
		this.config = config;
		this.deliveredMessages = new ArrayList <Message>();
		this.logFilename = logFilename;
		this.logs = new ConcurrentLinkedQueue<>();
		try {
			this.socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.latticeAgreements = new ArrayList <LatticeAgreement>();
		this.la = new LatticeAgreement(this, id, port, this.socket, hosts, config, this.logs);
		
	}
	
	public void startBroadcast() {		
		for (int i = 1; i < config.length; i++) {
        	String[] proposal = config[i].split("\\s+");
        	Set<Integer> proposalSet = new HashSet<Integer>();
        	for (int j; j < proposal.length; j++) {
        		int val = Integer.parseInt(proposal[j]);
        		proposalSet.add(val);
        	}
        	this.latticeAgreements.add(new LatticeAgreement(this, id, port, this.socket, host, message));
		}
		
		for (PerfectLink link: PerfectLinks) {
			Thread t = new Thread(link);
			t.start();
		}
		la.startBroadcast();
	}
	
	public void deliver(Message message) {
		logs.add(String.format("d %d %d\n", message.getSenderId(), message.getSeqNo()));
	}
	
	public void stopBroadcasting() {
		LatticeAgreement.stop();
	}
	
	public void writeLogs() {
		try (FileOutputStream logFileStream = new FileOutputStream(logFilename);){
			logs.forEach(log -> {
				try {
					logFileStream.write(log.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}