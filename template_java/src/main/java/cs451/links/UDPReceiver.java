package cs451.links;

import java.net.DatagramSocket;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.IOException;
import java.net.DatagramPacket;

import cs451.Message;
import cs451.Observer;
import cs451.Host;

class UDPReceiver {
	private final Observer observer;
	private final int id;
	private final Host host;
	private ConcurrentLinkedQueue <Message> messagesToSend;
	private DatagramSocket socket;
	private byte[] buffer = new byte[1024];
	
	
	UDPReceiver(Observer observer, int id, Host host, DatagramSocket socket, ConcurrentLinkedQueue <Message> messagesToSend){
		this.observer = observer;
		this.id = id;
		this.host = host;
		this.messagesToSend = messagesToSend;
		this.socket = socket;
	}
	
	public void receive() {
		DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
		try {
			socket.receive(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Message message = Message.fromByteArray(packet.getData());
		
		if (!message.isAck()) {
			message.ack();
			byte[] byteMessage = message.toByteArray();
			DatagramPacket ackPacket = new DatagramPacket(byteMessage, byteMessage.length, packet.getAddress(), packet.getPort());
			try {
				socket.send(ackPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}
			observer.deliver(message);
		} else if (message.getDestId() == host.getId()) {
			messagesToSend.remove(message);
		}

	}
	
	
}