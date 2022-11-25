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
	private HashMap <Integer, PerfectLink> perfectLinks;
	private ConcurrentLinkedQueue<String> logs;
	
	public BestEffortBroadcast(Observer observer, int id, int port, DatagramSocket socket, List<Host> hosts, ConcurrentLinkedQueue<String> logs){
		this.observer = observer;
		this.id = id;
		this.port = port;
		this.socket = socket;
		this.perfectLinks = initPerfectLinks(hosts);
		this.logs = logs;
	}
	
	public void startBroadcast(Message m) {
		perfectLinks.get(m.getDestId()).send(m);
		logs.add(String.format("b %d\n", m.getSeqNo()));
	}

	@Override
	public void deliver(Message message) {
		observer.deliver(message);
	}
	
	public static void stop() {
		PerfectLink.stop();
	}
	
	private HashMap <Integer, PerfectLink> initPerfectLinks(List<Host> hosts){
		HashMap <Integer, PerfectLink> PerfectLinks = new HashMap <Integer, PerfectLink>();
		for (Host host: hosts) {
			PerfectLink pl = new PerfectLink(this, id, port, this.socket, host);
			PerfectLinks.put(Integer.valueOf(host.getId()), pl);
			Thread t = new Thread(pl);
			t.start();
		}
		return PerfectLinks;
	}

	
}