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
	private final MssgType mssgType;
	private final int propNo;
	private final Set<Integer> propVals;
	private int isAck;
	
	enum MssgType{
		PROPOSAL,
		ACK,
		NACK
	}
	
	public Message(int slot, MssgType mssgType, int propNo, Set<Integer> propVals){
		this.slot = slot;
		this.mssgType = mssgType;
		this.propNo = propNo;
		this.propVals = propVals;
		this.isAck = 0;
	}
	
	Message(int slot, MssgType mssgType, int propNo, Set<Integer> propVals, int isAck){
		this.slot = slot;
		this.mssgType = mssgType;
		this.propNo = propNo;
		this.propVals = propVals;
		this.isAck = isAck;
	}
	
	public Message(Message message) {
		this.slot = message.slot;
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
				Objects.equals(mssgType, m.mssgType) &&
				Objects.equals(propNo, m.propNo) &&
				Objects.equals(propVals, m.propVals));
	}
	
}