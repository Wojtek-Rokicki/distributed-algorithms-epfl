package cs451.broadcast;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

public class FIFOBroadcast implements Observer{
	private final Observer observer;
	private final int id;
	private final int port;
	private DatagramSocket socket;
	private ConcurrentLinkedQueue<String> logs;
	private int N;
	private int[] next;
	private ArrayList<Message> pending;
	private MajorityACKUniformBroadcast unif;

	
	public FIFOBroadcast(Observer observer, int id, int port, DatagramSocket socket, List<Host> hosts, int noMessagesToSend, ConcurrentLinkedQueue<String> logs){
		this.observer = observer;
		this.id = id;
		this.port = port;
		this.socket = socket;
		this.logs = logs;
		this.N = hosts.size();
		this.next = new int[N];
		Arrays.fill(this.next, 1);
		this.pending = new ArrayList<Message>();
		this.unif = new MajorityACKUniformBroadcast(this, id, port, this.socket, hosts, noMessagesToSend, this.logs);
	}
	
	public void startBroadcast() {
		unif.startBroadcast();
	}
	
	public static void stop() {
		MajorityACKUniformBroadcast.stop();
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