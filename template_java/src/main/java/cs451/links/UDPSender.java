package cs451.links;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import cs451.Message;
import cs451.Host;

import java.net.DatagramPacket;
import java.io.IOException;

// Sends packet
class UDPSender implements Runnable{
	private InetAddress ipAddress;
	private final int targetPort;
	private ConcurrentLinkedQueue <Message> messagesToSend;
	
	private DatagramSocket socket;
	
	UDPSender(DatagramSocket socket, Host host, ConcurrentLinkedQueue <Message> messagesToSend){
		try {
			this.ipAddress = InetAddress.getByName(host.getIp());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.targetPort = host.getPort();
		this.socket = socket;
		this.messagesToSend = messagesToSend;
	}
	
	public void run() {
		System.out.println("++Sending to node with port "+targetPort+" started.");
		while (messagesToSend.size() != 0) {
			for (Message m: messagesToSend) {
				byte[] byteMessage = m.toByteArray();
				DatagramPacket packet = new DatagramPacket(byteMessage, byteMessage.length, ipAddress, targetPort);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}		
		System.out.println("++-Sending to node with port "+targetPort+" finished.");
	}
	
	
}