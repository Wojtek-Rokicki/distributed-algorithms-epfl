package cs451.broadcast;

import java.util.ArrayList;
import java.util.HashMap;

import cs451.Message;

class MajorityACKUniformBroadcast{
	private Integer N;
	private ArrayList<Message> delivered;
	private ArrayList<Message> pending;
	private HashMap <Message, ArrayList<Integer>> ack;
	
	MajorityACKUniformBroadcast(){
		this.delivered = new ArrayList<Message>();
		this.pending = new ArrayList<Message>();
		//forall m do ack[m] := ∅;
		
	}
	
	public void broadcast(Message m) {
		pending.add(m);
	}

	private boolean canDeliver(Message m) {
		return (boolean)(ack.get(m).size() > (N/2));
	}
}