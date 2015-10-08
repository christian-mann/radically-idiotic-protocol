import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;

public class ACKReceiver implements Runnable {

	private RIPSendingSocket window;

	public ACKReceiver(RIPSendingSocket window) {
		this.window = window;
	}

	public void receiveAckPacket(RIPPacket rip_pack) {
		if (! rip_pack.isAck()) {
			throw new RuntimeException("ACKReceiver.receiveAckPacket received a non-ack packet");
		}

		int seq = rip_pack.getSEQ();
		System.out.println("Received ACK for seq " + seq);

		PacketInfo info = window.getPacketInfoBySEQ(seq);
		info.setAcked(true);
		info.clearTimeout();
	}

	@Override
	public void run() {
		// listen for acks on the socket
		while ( ! Thread.interrupted() ) {
			byte[] data = new byte[200];
			DatagramPacket dgPacket = new DatagramPacket(data, data.length);
			boolean receivedPacket = true;
			try {
				window.sock.setSoTimeout(1000);
				window.sock.receive(dgPacket);
				receivedPacket = true;
				window.sock.setSoTimeout(0);
			} catch (SocketTimeoutException e) {
				receivedPacket = false;
			} catch (IOException e) {
				receivedPacket = false;
				e.printStackTrace();
			}
			
			if (receivedPacket) {
				RIPPacket ripPacket = RIPPacket.parseByteBuffer(
					ByteBuffer.wrap(data, 0, dgPacket.getLength())
				);
				
				if (ripPacket.isAck()) {
					this.receiveAckPacket(ripPacket);
				} else {
					System.out.println("Received non-ack packet " + ripPacket.toString());
				}
			}
		}
		System.out.println("ACKReceiver exiting");
	}
}
