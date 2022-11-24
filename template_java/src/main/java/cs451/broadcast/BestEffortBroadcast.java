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
	private List<PerfectLink> PerfectLinks;
	private HashMap <Integer, ArrayList<Message>> messagesToSend;
	private ConcurrentLinkedQueue<String> logs;
	
	public BestEffortBroadcast(Observer observer, int id, int port, DatagramSocket socket, List<Host> hosts, int noMessagesToSend, ConcurrentLinkedQueue<String> logs){
		this.observer = observer;
		this.id = id;
		this.socket = socket;
		this.messagesToSend = new HashMap <Integer, ArrayList<Message>>(); 
		this.messagesToSend = createMessagesToSend(noMessagesToSend, hosts);
		this.PerfectLinks = new ArrayList <PerfectLink>();
		for (Host host: hosts) {
			this.PerfectLinks.add(new PerfectLink(this, id, port, this.socket, host, messagesToSend.get(host.getId())));
		}
		this.logs = logs;
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

	@Override
	public void deliver(Message message) {
		observer.deliver(message);
	}
	
	public static void stop() {
		PerfectLink.stop();
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