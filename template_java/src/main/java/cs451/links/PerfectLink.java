package cs451.links;

import java.util.List;
import java.net.DatagramSocket;

import cs451.Observer;
import cs451.Message;
import cs451.Host;

public class PerfectLink implements Observer, Runnable{
	private final Observer observer;
	private final StubbornLink stubbornLink;
	
	public PerfectLink(Observer observer, int id, int port, DatagramSocket socket, Host host, List <Message> messagesToSend){
		this.observer = observer;
		this.stubbornLink = new StubbornLink(this, id, port, socket, host, messagesToSend);
	}
		
	public void run() {
		stubbornLink.run();
	}
	
	public void deliver(Message message) {
		observer.deliver(message);
	}
	
	public static void stop() {
		StubbornLink.stop();
	}
}