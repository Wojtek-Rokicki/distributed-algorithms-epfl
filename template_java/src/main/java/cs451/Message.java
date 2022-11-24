package cs451;

import java.nio.ByteBuffer;
import java.util.Objects;

public class Message{
	private final int seqNo;
	private final int senderId;
	private final int destId;
	private int isAck;
	
	public Message(int seqNo, int senderId, int destId){
		this.seqNo = seqNo;
		this.senderId = senderId;
		this.destId = destId;
		this.isAck = 0;
	}
	
	Message(int seqNo, int senderId, int destId, int isAck){
		this.seqNo = seqNo;
		this.senderId = senderId;
		this.destId = destId;
		this.isAck = isAck;
	}
	
	public byte[] toByteArray() {
		ByteBuffer byteBuffer = ByteBuffer.allocate(16); 
		byteBuffer.putInt(seqNo); 
		byteBuffer.putInt(senderId); 
		byteBuffer.putInt(destId); 
		byteBuffer.putInt(isAck);
		return byteBuffer.array();
	}
	
	public static Message fromByteArray(byte[] messageByteArray) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(messageByteArray);
		int seqNo = byteBuffer.getInt();
		int senderId = byteBuffer.getInt();
		int destId = byteBuffer.getInt();
		int isAck = byteBuffer.getInt();
		return new Message(seqNo, senderId, destId, isAck);
	}
	
	public void ack() {
		isAck = 1;
	}
	
	public boolean isAck() {
		return (isAck != 0);
	}

	public int getSeqNo() {
		return seqNo;
	}
	public int getSenderId() {
		return senderId;
	}
	public int getDestId() {
		return destId;
	}	
	
	@Override
	public boolean equals(Object o) {
		if (this==o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Message m = (Message) o;
		return (Objects.equals(seqNo, m.seqNo) &&
				Objects.equals(senderId, m.senderId) &&
				Objects.equals(destId, m.destId));
	}
	
}