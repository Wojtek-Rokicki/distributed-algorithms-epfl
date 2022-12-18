package cs451.broadcast;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	
	public BestEffortBroadcast(Observer observer, int id, int port, DatagramSocket socket, List<Host> hosts){
		this.observer = observer;
		this.id = id;
		this.port = port;
		this.socket = socket;
		this.perfectLinks = new ArrayList <PerfectLink>();
		this.hosts = hosts;
	}
	
	public void startBroadcast(Message m) {
		HashMap <Integer, ArrayList<Message>> messagesToSend = createMessagesToSend(m, hosts);
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
	
	private HashMap<Integer, ArrayList<Message>> createMessagesToSend(Message m, List<Host> hosts){
		HashMap<Integer, ArrayList<Message>> messagesToSend = new HashMap <Integer, ArrayList<Message>>();
		for (Host host: hosts) {
			ArrayList<Message> listOfMessagesToSend = new ArrayList<>();
			Message message = new Message(m);
			message.setSenderId(id);
			message.setDestId(host.getId());
			listOfMessagesToSend.add(message);
			messagesToSend.put(host.getId(), listOfMessagesToSend);
		}
		return messagesToSend;
	}
	
}