import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import edu.utulsa.unet.UDPSocket;

class PacketSender implements Runnable {
	private UDPSocket sock;
	private RIPSendingSocket window;

	//public PacketSender(UDPSocket sock, BlockingQueue<PacketInfo> queue, PacketReQueuer req) {
	public PacketSender(UDPSocket sock,  RIPSendingSocket window) {
		this.sock = sock;
		this.window = window;
	}

	public void run() {
		try {
			while (!Thread.interrupted()) {
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
				
				if (! packetInfo.isAcked()) {
					// If it's already ACKed, then don't start a requeuer
					packetInfo.setTimeout(50); // millis
		
					window.requeuer.timeoutRequeue(
						packetInfo
					);
				}
			}
			System.out.println("PacketSender interrupted");
		} catch (InterruptedException ex) {
			System.out.println("PacketSender interrupted");
		} catch (IOException ex) {
			System.err.println("IOException in PacketSender");
			ex.printStackTrace();
		}
	}
}
