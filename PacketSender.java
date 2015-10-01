import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import edu.utulsa.unet.UDPSocket;

class PacketSender implements Runnable {
	private UDPSocket sock;
	private RIPSocket window;

	//public PacketSender(UDPSocket sock, BlockingQueue<PacketInfo> queue, PacketReQueuer req) {
	public PacketSender(UDPSocket sock,  RIPSocket window) {
		this.sock = sock;
		this.window = window;
	}

	public void run() {
		try {
			while (true) {
				PacketInfo packetInfo = window.send_queue.take(); // blocking
	
				RIPPacket rip_packet = packetInfo.getPacket();
				ByteBuffer bb = rip_packet.asByteBuffer();
				this.sock.send(new DatagramPacket(
					bb.array(),
					bb.array().length,
					this.window.targetAddress,
					this.window.targetUDPPort
				));
				System.out.println("Sending " + Arrays.toString(bb.array()));
				packetInfo.send_count += 1;
				packetInfo.setTimeout(1000); // millis
	
				window.requeuer.timeoutRequeue(
					packetInfo,
					1000
				);
			}
		} catch (InterruptedException ex) {
			;
		} catch (IOException ex) {
			System.err.println("IOException in PacketSender");
			ex.printStackTrace();
		}
	}
}
