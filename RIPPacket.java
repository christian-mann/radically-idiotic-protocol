import java.net.DatagramPacket;
import java.nio.ByteBuffer;

public class RIPPacket {
	
	int sequenceNumber;
	boolean ack;
	int checksum;
	int windowSize;
	byte[] payload;
	
	
	public RIPPacket(byte[] data) {
		payload = data;
	}

	public boolean isAck() {
		return ack;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	
	DatagramPacket getPacket() {
		return null;
	}

	public int getSEQ() {
		return sequenceNumber;
	}

	public ByteBuffer asByteBuffer() {
		ByteBuffer buf = ByteBuffer.allocate(4*4 + 1 + payload.length);
		buf.put(ack ? (byte) 1 : (byte)0);
		buf.putInt(sequenceNumber);
		buf.putInt(windowSize);
		buf.putInt(checksum);
		buf.putInt(payload.length);
		buf.put(payload);
		return buf;
	}
}
