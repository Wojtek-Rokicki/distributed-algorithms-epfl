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


class Process implements Observer{
	private final int id;
	private final int port;
	private DatagramSocket socket;
	private final List<Host> hosts;
	private final String[] config;
	private List<LatticeAgreement> latticeAgreements;
	
	private final String logFilename;
		
	private ConcurrentLinkedQueue<String> logs;
	
	Process(int id, int port, List<Host> hosts, String[] config, String logFilename){
		this.id = id;
		this.port = port;
		this.hosts = hosts;
		this.config = config;
		this.logFilename = logFilename;
		this.logs = new ConcurrentLinkedQueue<>();
		try {
			this.socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.latticeAgreements = new ArrayList <LatticeAgreement>();		
	}
	
	public void startBroadcast() {		
		for (int i = 1; i < config.length; i++) {
        	String[] proposal = config[i].split("\\s+");
        	Set<Integer> proposalSet = new HashSet<Integer>();
        	for (int j = 0; j < proposal.length; j++) {
        		int val = Integer.parseInt(proposal[j]);
        		proposalSet.add(val);
        	}
        	this.latticeAgreements.add(new LatticeAgreement(this, id, port, this.socket, hosts, logs, i, proposalSet));
		}
		
		for (LatticeAgreement link: latticeAgreements) {
			Thread t = new Thread(link);
			t.start();
		}
	}
	
	public void deliver(Message message) {
		String log = "";
		for (Integer value : message.getPropVals()) {
			log += Integer.toString(value) + " ";
		}
		logs.add(log+"\n");
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