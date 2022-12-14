package cs451;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import cs451.broadcast.FIFOBroadcast;


class Process implements Observer{
	private final int id;
	private final String ip;
	private final int port;
	private DatagramSocket socket;
	private final List<Host> hosts;
	private final int noMessagesToSend;
	private final FIFOBroadcast fifo;
	
	private ArrayList<Message> deliveredMessages;
	private final String logFilename;
		
	private ConcurrentLinkedQueue<String> logs;
	
	Process(int id, String ip, int port, List<Host> hosts, int noMessagesToSend, String logFilename){
		this.id = id;
		this.ip = ip;
		this.port = port;
		this.hosts = hosts;
		this.noMessagesToSend = noMessagesToSend;
		this.deliveredMessages = new ArrayList <Message>();
		this.logFilename = logFilename;
		this.logs = new ConcurrentLinkedQueue<>();
		try {
			this.socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.fifo = new FIFOBroadcast(this, id, port, this.socket, hosts, noMessagesToSend, this.logs);
		
	}
	
	public void startBroadcast() {
		fifo.startBroadcast();
	}
	
	public void deliver(Message message) {
		logs.add(String.format("d %d %d\n", message.getSenderId(), message.getSeqNo()));
	}
	
	public void stopBroadcasting() {
		FIFOBroadcast.stop();
	}
	
	public void writeLogs() {
		try (FileOutputStream logFileStream = new FileOutputStream(logFilename);){
			logs.forEach(log -> {
				try {
					logFileStream.write(log.getBytes());
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}