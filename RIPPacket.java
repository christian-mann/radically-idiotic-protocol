import java.nio.ByteBuffer;
import java.util.Arrays;

public class RIPPacket {
	
	int sequenceNumber;
	boolean ack;
	boolean fin;
	int checksum;
	int windowSize;
	byte[] payload;
	
	
	public RIPPacket(byte[] data) {
		payload = data;
	}

	@Override
	public String toString() {
		return "RIPPacket [sequenceNumber=" + sequenceNumber + ", ack=" + ack + ", fin=" + fin + ", checksum="
				+ checksum + ", windowSize=" + windowSize + ", payload=" + Arrays.toString(payload) + "]";
	}

	public RIPPacket() {
	}

	public boolean isAck() {
		return ack;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public int getSEQ() {
		return sequenceNumber;
	}

	public ByteBuffer asByteBuffer() {
		ByteBuffer buf = ByteBuffer.allocate(4*4 + 2 + payload.length);
		buf.put(ack ? (byte) 1 : (byte)0);
		buf.put(fin ? (byte) 1 : (byte)0);
		buf.putInt(sequenceNumber);
		buf.putInt(windowSize);
		buf.putInt(checksum);
		buf.putInt(payload.length);
		buf.put(payload);
		return buf;
	}
	
	public static RIPPacket parseByteBuffer(ByteBuffer bb) {
		RIPPacket pack = new RIPPacket();
		pack.ack = (bb.get() > 0);
		pack.fin = (bb.get() > 0);
		pack.sequenceNumber = bb.getInt();
		pack.windowSize = bb.getInt();
		pack.checksum = bb.getInt();
		int payload_len = bb.getInt();
		pack.payload = new byte[payload_len];
		bb.get(pack.payload, 0, payload_len);
		return pack;
	}
	
	public RIPPacket makeACKPacket() {
		RIPPacket pack = new RIPPacket();
		pack.ack = true;
		pack.sequenceNumber = this.sequenceNumber;
		pack.fin = this.fin;
		pack.payload = new byte[0];
		return pack;
	}
	
	public static int headerLength() {
		return 
			  1 // ack
			+ 1 // fin
			+ 4 // seq
			+ 4 // winSz
			+ 4 // check
			+ 4 // len
			;
	}
}
