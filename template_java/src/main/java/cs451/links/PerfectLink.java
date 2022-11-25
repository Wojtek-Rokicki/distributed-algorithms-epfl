package cs451.links;

import java.util.ArrayList;
import java.util.List;
import java.net.DatagramSocket;

import cs451.Observer;
import cs451.Message;
import cs451.Host;

public class PerfectLink implements Observer, Runnable{
	private final Observer observer;
	private final StubbornLink stubbornLink;
	private ArrayList<Message> deliveredMessages;
	
	public PerfectLink(Observer observer, int id, int port, DatagramSocket socket, Host host){
		this.observer = observer;
		this.deliveredMessages = new ArrayList<Message>();
		this.stubbornLink = new StubbornLink(this, id, port, socket, host);
	}
		
	public void run() {
		stubbornLink.run();
	}
	
	public void send(Message m) {
		stubbornLink.send(m);
	}
	
	public void deliver(Message message) {
		boolean delivered = deliveredMessages.contains(message);
		if (!delivered){
			deliveredMessages.add(new Message(message));
			observer.deliver(message);
		}
	}
	
	public static void stop() {
		StubbornLink.stop();
	}
}