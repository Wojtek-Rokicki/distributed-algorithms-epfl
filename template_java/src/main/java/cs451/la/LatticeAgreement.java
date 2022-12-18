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
	private BestEffortBroadcast beb;
	
	private final int slot;
	private final int n;
	private final int f;
	
	private boolean active;
	private int ackCount;
	private int nackCount;
	private int activePropNo;
	private Set<Integer> propVals;
	private Set<Integer> acceptedVals;

	
	public LatticeAgreement(Observer observer, int id, int port, DatagramSocket socket, List<Host> hosts, ConcurrentLinkedQueue<String> logs, int slot, Set<Integer> propVals){
		this.observer = observer;
		this.id = id;
		this.port = port;
		this.socket = socket;
		this.logs = logs;
		this.beb = new BestEffortBroadcast(this, id, port, this.socket, hosts);
		
		this.slot = slot;
		this.n = hosts.size();
		this.f = this.n/2;
	
		this.active = false;
		this.ackCount = 0;
		this.nackCount = 0;
		this.activePropNo = 0;
		this.propVals = propVals;
		this.acceptedVals = new HashSet<Integer>();
	}
	
	public void run() {
		this.propose();
	}
	
	public void propose() {
		this.active = true;
		this.activePropNo += 1;
		this.ackCount = 0;
		this.nackCount = 0;
		Message message = new Message(slot, Message.MssgType.PROPOSAL, this.activePropNo, this.propVals);
		this.beb.startBroadcast(message);
	}
	
	public static void stop() {
		BestEffortBroadcast.stop();
	}
	
	private void checkAckCnt() {
		if(this.ackCount >= this.f + 1 && this.active == true) {
			Message m = new Message(slot, propVals);
			observer.deliver(m);
			this.active = false;
		}
	}
	
	private void checkNAckCnt() {
		if(this.nackCount > 0 && this.ackCount+this.nackCount >= this.f+1 && this.active == true) {
			this.activePropNo += 1;
			this.ackCount = 0;
			this.nackCount = 0;
			Message message = new Message(slot, Message.MssgType.PROPOSAL, this.activePropNo, this.propVals);
			this.beb.startBroadcast(message);		
		}
	}
	
	
	
	synchronized public void deliver(Message message) {
		if (message.getSlot() == slot) {
			
			Message.MssgType mType = message.getMssgType();
			if (mType == Message.MssgType.ACK) {
				if (message.getPropNo() == activePropNo) {
					ackCount += 1;
					checkAckCnt();
					checkNAckCnt();
				}
			} else if (mType == Message.MssgType.NACK) {
				if (message.getPropNo() == activePropNo) {
					propVals.addAll(message.getPropVals()); // union
					nackCount += 1;
					checkNAckCnt();
				}
			} else if (mType == Message.MssgType.PROPOSAL) {
				if (message.getPropVals().containsAll(acceptedVals)) {
					this.acceptedVals = message.getPropVals();
					Message m = new Message(slot, Message.MssgType.ACK, message.getPropNo(), message.getPropVals());
					beb.startBroadcast(m); // should send only to pj
				} else {
					this.acceptedVals.addAll(message.getPropVals());
					Message m = new Message(slot, Message.MssgType.NACK, message.getPropNo(), this.acceptedVals);
					beb.startBroadcast(m); // should send only to pj
				}
			}
			
		}
		
	}
	
}
