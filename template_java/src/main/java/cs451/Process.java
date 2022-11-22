package cs451;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs451.links.PerfectLink;

class Process implements Observer{
	private final int id;
	private final String ip;
	private final int port;
	private DatagramSocket socket;
	private final List<Host> hosts;
	private final int noMessagesToSend;
	private HashMap <Integer, ArrayList<Message>> messagesToSend;
	private final int messagesReceiverProcessId;
	private ArrayList<Message> deliveredMessages;
	private final String logFilename;
	
	private List<PerfectLink> PerfectLinks;
	
	private ConcurrentLinkedQueue<String> logs;
	
	Process(int id, String ip, int port, List<Host> hosts, int noMessagesToSend, int messagesReceiverProcessId, String logFilename){
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.hosts = hosts;
		this.noMessagesToSend = noMessagesToSend;
		this.messagesReceiverProcessId = messagesReceiverProcessId;
		this.deliveredMessages = new ArrayList <Message>();
		this.logFilename = logFilename;
		try {
			this.socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		this.messagesToSend = new HashMap <Integer, ArrayList<Message>>(); 
		this.messagesToSend = createMessagesToSend(noMessagesToSend, hosts);
		this.PerfectLinks = new ArrayList <PerfectLink>();
		for (Host host: hosts) {
			this.PerfectLinks.add(new PerfectLink(this, id, port, this.socket, host, messagesToSend.get(host.getId())));
		}
		
		this.logs = new ConcurrentLinkedQueue<>();
	}
	
	public void startBroadcast() {
		for (PerfectLink link: PerfectLinks) {
			Thread t = new Thread(link);
			t.start();
		}
		
		for (Integer proc_id: messagesToSend.keySet()) {
			for (Message m: messagesToSend.get(proc_id)) {
				logs.add(String.format("b %d\n", m.getSeqNo()));
			}
			break;
		}
	}
	
	synchronized public void deliver(Message message) {
		// Check if it is not in delivered array
		if (messagesReceiverProcessId == id) { // only for Perfect links
		boolean delivered = deliveredMessages.contains(message);
		if (!delivered){
			deliveredMessages.add(message);
			logs.add(String.format("d %d %d\n", message.getSenderId(), message.getSeqNo()));
		}
		}
	}
	
	public void stopBroadcasting() {
		PerfectLink.stop();
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
	
	private HashMap<Integer, ArrayList<Message>> createMessagesToSend(int noMessagesToSend, List<Host> hosts){
		HashMap<Integer, ArrayList<Message>> messagesToSend = new HashMap <Integer, ArrayList<Message>>();
		for (Host host: hosts) {
			ArrayList<Message> listOfMessagesToSend = new ArrayList<>();
			for (int i = 0; i<noMessagesToSend; i++) {
				listOfMessagesToSend.add(new Message(i+1, id, host.getId()));
			}
			messagesToSend.put(host.getId(), listOfMessagesToSend);
		}
		return messagesToSend;
	}
}