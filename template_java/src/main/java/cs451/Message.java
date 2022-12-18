package cs451;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public class Message implements Serializable{
	private final int slot;
	private int senderId;
	private int destId;
	private final MssgType mssgType;
	private final int propNo;
	private final Set<Integer> propVals;
	private int isAck;
	
	public enum MssgType{
		PROPOSAL,
		ACK,
		NACK
	}
	
	public Message(int slot, int senderId, int destId, MssgType mssgType, int propNo, Set<Integer> propVals){
		this.slot = slot;
		this.senderId = senderId;
		this.destId = destId;
		this.mssgType = mssgType;
		this.propNo = propNo;
		this.propVals = propVals;
		this.isAck = 0;
	}
	
	public Message(int slot, MssgType mssgType, int propNo, Set<Integer> propVals){
		this.slot = slot;
		this.senderId = -1;
		this.destId = -1;
		this.mssgType = mssgType;
		this.propNo = propNo;
		this.propVals = propVals;
		this.isAck = 0;
	}
	
	Message(int slot, int senderId, int destId, MssgType mssgType, int propNo, Set<Integer> propVals, int isAck){
		this.slot = slot;
		this.senderId = senderId;
		this.destId = destId;
		this.mssgType = mssgType;
		this.propNo = propNo;
		this.propVals = propVals;
		this.isAck = isAck;
	}
	
	public Message(Message message) {
		this.slot = message.slot;
		this.senderId = message.senderId;
		this.destId = message.destId;
		this.mssgType = message.mssgType;
		this.propNo = message.propNo;
		this.propVals = message.propVals;
		this.isAck = message.isAck;
	}

	public byte[] toByteArray() throws IOException {
		byte [] data;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
			     ObjectOutputStream oos = new ObjectOutputStream(bos)) {
			    oos.writeObject(this);
			    oos.flush();
			    data = bos.toByteArray();
			}
		return data;
	}
	
	public static Message fromByteArray(byte[] messageByteArray) throws IOException, ClassNotFoundException {
		Message deserializedMssg;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(messageByteArray);
			     ObjectInputStream ois = new ObjectInputStream(bis)) {
			    deserializedMssg = (Message) ois.readObject();
			}
		return deserializedMssg;
	}
	
	public void ack() {
		isAck = 1;
	}
	
	public boolean isAck() {
		return (isAck != 0);
	}

	public int getSlot() {
		return slot;
	}
	
	public int getSenderId() {
		return senderId;
	}
	public int getDestId() {
		return destId;
	}
	
	public void setSenderId(int senderId) {
		 this.senderId = senderId;
	}
	public void setDestId(int destId) {
		this.destId = destId;
	}	
	
	public MssgType getMssgType() {
		return mssgType;
	}
	public int getPropNo() {
		return propNo;
	}
	public Set<Integer> getPropVals() {
		return propVals;
	}	
	
	@Override
	public boolean equals(Object o) {
		if (this==o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Message m = (Message) o;
		return (Objects.equals(slot, m.slot) &&
				Objects.equals(senderId, m.senderId) &&
				Objects.equals(destId, m.destId) &&
				Objects.equals(mssgType, m.mssgType) &&
				Objects.equals(propNo, m.propNo) &&
				Objects.equals(propVals, m.propVals));
	}
	
}