package cs451.la;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import cs451.Host;
import cs451.Message;
import cs451.Observer;
import cs451.broadcast.BestEffortBroadcast;

public class LatticeAgreement implements Observer, Runnable{
	private final Observer observer;
	private final int id;
	private final int port;
	private DatagramSocket socket;
	private ConcurrentLinkedQueue<String> logs;
	private int N;
	private int[] next;
	private ArrayList<Message> pending;
	private BestEffortBroadcast beb;
	
	private boolean active;
	private int ackCount;
	private int nackCount;
	private int activePropNo;
	private Set<Integer> propVals;
	

	
	public LatticeAgreement(Observer observer, int id, int port, DatagramSocket socket, List<Host> hosts, String[] config, ConcurrentLinkedQueue<String> logs){
		this.observer = observer;
		this.id = id;
		this.port = port;
		this.socket = socket;
		this.logs = logs;
		this.N = hosts.size();
		this.next = new int[N];
		Arrays.fill(this.next, 1);
		this.pending = new ArrayList<Message>();
		this.beb = new BestEffortBroadcast(this, id, port, this.socket, hosts, noMessagesToSend, this.logs);
	
		this.active = false;
		this.ackCount = 0;
		this.nackCount = 0;
		this.activePropNo = 0;
		this.propVals = new HashSet<Integer>();
	}
	
	public void run() {
		stubbornLink.run();
	}
	
	public void propose(Set<Integer> proposal) {
		this.propVals = proposal;
		Message message = new Message(proposal);
	}
	
	public void startBroadcast() {
		beb.startBroadcast();
	}
	
	public static void stop() {
		BestEffortBroadcast.stop();
	}
	
	public boolean containsMssgWithSeqNo(final List<Message> list, final int sn){
	    return list.stream().filter(m -> ((Integer)m.getSeqNo()).equals((Integer)sn)).findFirst().isPresent();
	}
	
	public Message mssgWithSeqNo(final List<Message> list, final int sn) {
		return list.stream()
			    .filter(m -> Objects.equals(m.getSeqNo(), sn))
			    .collect(Collectors.toList()).get(0);
	}
	
	synchronized public void deliver(Message message) {
		pending.add(new Message(message));
		// Get pending messages from message's sender
		int senderId = message.getSenderId();
		List<Message> messagesFromSender = pending.stream()
		    .filter(m -> Objects.equals(m.getSenderId(), senderId))
		    .collect(Collectors.toList());
		// While there is a message which can be delivered, deliver.
		while(containsMssgWithSeqNo(messagesFromSender, next[senderId-1])) {
			Message nextMessage = mssgWithSeqNo(messagesFromSender, next[senderId-1]);
			pending.remove(nextMessage);
			observer.deliver(nextMessage);
			next[senderId-1] = next[senderId-1]+1;
		}
	}
	
}
