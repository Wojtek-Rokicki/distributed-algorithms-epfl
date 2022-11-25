package cs451.broadcast;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

public class MajorityACKUniformBroadcast implements Observer{
	private final Observer observer;
	private final int id;
	private DatagramSocket socket;
	private List<Host> hosts;
	private final int noMessagesToSend;
	
	private Integer N;
	private ArrayList<Message> delivered;
	private ArrayList<Message> pending;
	private HashMap <Message, ArrayList<Integer>> ack;
	private BestEffortBroadcast beb;
	
	public MajorityACKUniformBroadcast(Observer observer, int id, int port, DatagramSocket socket, List<Host> hosts, int noMessagesToSend, ConcurrentLinkedQueue<String> logs){
		this.observer = observer;
		this.id = id;
		this.socket = socket;
		this.hosts = hosts;
		this.noMessagesToSend = noMessagesToSend;
		
		this.N = hosts.size();
		this.delivered = new ArrayList<Message>();
		this.pending = new ArrayList<Message>();
		this.ack = new HashMap <Message, ArrayList<Integer>>();
		this.beb = new BestEffortBroadcast(this, id, port, this.socket, hosts, noMessagesToSend, logs);
	}
	
	public void startBroadcast() {
		initPending();
		beb.startBroadcast();
	}
	
	public void broadcast(Message m) {
		pending.add(m);
		beb.sendMessage(m);
	}

	private boolean canDeliver(Message m, List<Message> keys) {
		int processesSawM = ack.get(keys.get(keys.indexOf(m))).size();
		int majority = (int)(N/2);
		return (boolean)(processesSawM > majority);
	}
	
	@Override
	synchronized public void deliver(Message m) {
		List<Message> keys = new ArrayList<Message>();
		keys.addAll(ack.keySet());
		if(!keys.contains(m)) {
			ack.put(m, new ArrayList<Integer>());
			keys.add(m);
		}
		ack.get(keys.get(keys.indexOf(m))).add(m.getResenderId());
		if (!pending.contains(m)) {
			m.setResenderId(id);
			m.setAck(0);
			broadcast(m);
		}
		if (canDeliver(m, keys) && !delivered.contains(m)) {
			delivered.add(m);
			observer.deliver(m);
		}
		
	}
	
	public void initPending() {
		for (Host host: hosts) {
			for (int i = 0; i<noMessagesToSend; i++) {
				pending.add(new Message(i+1, id, id, host.getId()));
			}
		}
	}
	
	public static void stop() {
		BestEffortBroadcast.stop();
	}
}