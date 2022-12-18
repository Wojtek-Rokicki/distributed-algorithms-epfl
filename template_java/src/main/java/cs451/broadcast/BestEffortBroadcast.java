package cs451.broadcast;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.links.PerfectLink;

public class BestEffortBroadcast implements Observer{
	private final Observer observer;
	private final int id;
	private final int port;
	private DatagramSocket socket;
	private List<Host> hosts;
	private List<PerfectLink> perfectLinks;
	private HashMap <Integer, ArrayList<Message>> messagesToSend;
	private ConcurrentLinkedQueue<String> logs;
	
	public BestEffortBroadcast(Observer observer, int id, int port, DatagramSocket socket, List<Host> hosts, String[] config, ConcurrentLinkedQueue<String> logs){
		this.observer = observer;
		this.id = id;
		this.port = port;
		this.socket = socket;
		this.messagesToSend = new HashMap <Integer, ArrayList<Message>>(); 
		this.messagesToSend = createMessagesToSend(message, hosts);
		this.perfectLinks = new ArrayList <PerfectLink>();
		this.hosts = hosts;
		this.logs = logs;
	}
	
	public void startBroadcast(Message m) {
		for (Host host: hosts) {
			this.perfectLinks.add(new PerfectLink(this, id, port, this.socket, host, messagesToSend.get(host.getId())));
		}
		for (PerfectLink link: perfectLinks) {
			Thread t = new Thread(link);
			t.start();
		}
	}

	@Override
	public void deliver(Message message) {
		observer.deliver(message);
	}
	
	public static void stop() {
		PerfectLink.stop();
	}
	
	private HashMap<Integer, Message> createMessagesToSend(String[] config, List<Host> hosts){
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