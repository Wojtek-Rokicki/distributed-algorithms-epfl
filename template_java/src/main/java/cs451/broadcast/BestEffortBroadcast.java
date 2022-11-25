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
	private DatagramSocket socket;
	private List<Host> hosts;
	private List<PerfectLink> perfectLinks;
	private HashMap <Integer, ConcurrentLinkedQueue<Message>> messagesToSend;
	private ConcurrentLinkedQueue<String> logs;
	
	public BestEffortBroadcast(Observer observer, int id, int port, DatagramSocket socket, List<Host> hosts, int noMessagesToSend, ConcurrentLinkedQueue<String> logs){
		this.observer = observer;
		this.id = id;
		this.socket = socket;
		this.hosts = hosts;
		this.messagesToSend = new HashMap <Integer, ConcurrentLinkedQueue<Message>>(); 
		this.messagesToSend = createMessagesToSend(noMessagesToSend, hosts);
		this.perfectLinks = new ArrayList <PerfectLink>();
		for (Host host: hosts) {
			this.perfectLinks.add(new PerfectLink(this, id, port, this.socket, host, messagesToSend.get(host.getId())));
		}
		this.logs = logs;
	}
	
	public void startBroadcast() {
		for (PerfectLink link: perfectLinks) {
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
	
	public void sendMessage(Message m) {
		for (Host host: hosts) {
			ConcurrentLinkedQueue<Message> cql = messagesToSend.get(host.getId());
			cql.add(new Message(m));
		}	
	}

	@Override
	public void deliver(Message message) {
		observer.deliver(message);
	}
	
	public static void stop() {
		PerfectLink.stop();
	}
	
	private HashMap<Integer, ConcurrentLinkedQueue<Message>> createMessagesToSend(int noMessagesToSend, List<Host> hosts){
		HashMap<Integer, ConcurrentLinkedQueue<Message>> messagesToSend = new HashMap <Integer, ConcurrentLinkedQueue<Message>>();
		for (Host host: hosts) {
			ConcurrentLinkedQueue<Message> listOfMessagesToSend = new ConcurrentLinkedQueue<>();
			for (int i = 0; i<noMessagesToSend; i++) {
				listOfMessagesToSend.add(new Message(i+1, id, id, host.getId()));
			}
			messagesToSend.put(host.getId(), listOfMessagesToSend);
		}
		return messagesToSend;
	}

	
}