package cs451.links;


import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs451.Host;
import cs451.Message;
import cs451.Observer;

public class StubbornLink implements Observer, Runnable{
	private final Observer observer;
//	private final int id;
//	private final int port;
//	private final Host host;
	private ConcurrentLinkedQueue <Message> messagesToSend;
	private DatagramSocket socket;
	private UDPSender sender;
	private UDPReceiver receiver;
		
	private static boolean run = false;
	
	public StubbornLink(Observer observer, int id, int port, DatagramSocket socket, Host host, ConcurrentLinkedQueue <Message> messagesToSend){
		this.observer = observer;
//		this.id = id;
//		this.port = port;
//		this.host = host;
		this.messagesToSend = messagesToSend;
		this.socket = socket;
		this.sender = new UDPSender(this.socket, host, this.messagesToSend);
		this.receiver = new UDPReceiver(this, id, host, this.socket, this.messagesToSend);
	}
	
	public void run() {
		run = true;
		Thread senderThread = new Thread(sender);
		senderThread.start();
		while(run) {
			receiver.receive();
		}
	}
	
	public void deliver(Message message) {
		observer.deliver(message);
	}
	
	public static void stop() {
		run = false;
		UDPSender.run = false;
	}
}