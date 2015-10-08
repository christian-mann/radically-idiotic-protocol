import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

import edu.utulsa.unet.UDPSocket;

public class RIPReceivingSocket {

	UDPSocket sock;

	SortedMap<Integer, PacketInfo> packetHistory = new TreeMap<Integer, PacketInfo>();
	int lastAckSent = -1;
	int windowSize;
	int nextDeliverableSeq;

	InetAddress srcAddress;
	Integer srcPort; // nullable, but 0 would work just as well since it's not a valid port

	private volatile boolean shouldListen;

	public RIPReceivingSocket(UDPSocket sock) {
		this.sock = sock;

		try {
			this.windowSize = sock.getReceiveBufferSize();
		} catch (SocketException e) {
			this.windowSize = 100;
		}
	}

	public void listenForever() {
		byte[] buffer = new byte[200];
		DatagramPacket dgPacket = new DatagramPacket(buffer, buffer.length);
		this.shouldListen = true;
		while (this.shouldListen) {
			try {
				this.sock.receive(dgPacket);
			} catch (IOException e) {
				e.printStackTrace();
			}

			int sender_port = dgPacket.getPort();
			InetAddress sender_addr = dgPacket.getAddress();

			// verify sender port and address are correct
			if (this.srcPort == null) {
				this.srcPort = sender_port;
			}

			if (! this.srcPort.equals(sender_port)) {
				// ignore packet
				System.err.println("Received packet from unknown source port " + sender_port + "; expected " + this.srcPort);
				continue;
			}

			if (this.srcAddress == null) {
				this.srcAddress = sender_addr;
			}

			if (! this.srcAddress.equals(sender_addr)) {
				System.err.println("Received packet from unknown source addr " + sender_port + "; expected " + this.srcAddress);
				continue;
			}

			RIPPacket ripPacket = RIPPacket.parseByteBuffer(ByteBuffer.wrap(buffer));
			System.out.println("Received packet: " + ripPacket);

			this.handlePacket(ripPacket);
		}
	}

	private void handlePacket(RIPPacket ripPacket) {

		int seq = ripPacket.sequenceNumber;
		System.out.println("Received packet with sequence number " + seq);
		// check checksum (not implemented yet)

		if (seq <= this.lastAckSent) {
			// we've (probably) seen this packet
			this.sendAck(ripPacket);
		}	

		else if (seq > this.lastAckSent && seq <= this.largestAcceptableFrame()) {
			if (this.packetHistory.containsKey(seq)) {
				// we've seen this packet
				this.sendAck(ripPacket);
			} else {
				// we have not yet seen this packet
				this.sendAck(ripPacket);
				PacketInfo pi = new PacketInfo(ripPacket);
				this.packetHistory.put(seq, pi);
				// try to deliver any relevant packets
				this.collatePackets();
			}
		}

//		else if (seq > this.largestAcceptableFrame()) {
//			// do nothing, send a NACK, whatever
//		}

		else {
			// this should not happen
			throw new RuntimeException("Programmer error. SEQ unhandled.");
		}

	}

	private void collatePackets() {
		System.out.println("collatePackets()");
		System.out.println("nextDeliverableSeq = " + this.nextDeliverableSeq);
		for (Iterator<PacketInfo> it = this.packetHistory.values().iterator(); it.hasNext(); ) {
			PacketInfo pi = it.next();
			RIPPacket ripPack = pi.getPacket();
			System.out.println("ripPack.sequenceNumber = " + ripPack.sequenceNumber);
			if (ripPack.sequenceNumber < this.nextDeliverableSeq) {
				// this packet is worthless
				it.remove();
			} else if (ripPack.sequenceNumber == this.nextDeliverableSeq) {
				// this packet is deliverable
				byte[] data = ripPack.payload;
				this.deliverData(data);
				this.nextDeliverableSeq = ripPack.sequenceNumber + ripPack.payload.length;
				
				// is this a FIN packet?
				if (ripPack.fin) {
					System.out.println("Received FIN, stopping listening");
					this.stopListening();
					this.nextDeliverableSeq += 1;
				}
				it.remove();
			} else {
				break;
			}
		}
	}

	protected void sendAck(RIPPacket origPacket) {
		int seq = origPacket.getSEQ();
		System.out.println("Sending ACK for packet " + seq);
		RIPPacket rip_ack_packet = origPacket.makeACKPacket();
		ByteBuffer udp_data = rip_ack_packet.asByteBuffer();
		DatagramPacket udp_packet = new DatagramPacket(
				udp_data.array(),
				udp_data.array().length,
				this.srcAddress,
				this.srcPort
		);
		try {
			this.sock.send(udp_packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected void sendFin(RIPPacket origPacket) {
		int seq = origPacket.getSEQ();
		System.out.println("Sending FIN with seq " + seq);
		RIPPacket rip_ack_packet = origPacket.makeACKPacket();
		ByteBuffer udp_data = rip_ack_packet.asByteBuffer();
		DatagramPacket udp_packet = new DatagramPacket(
				udp_data.array(),
				udp_data.array().length,
				this.srcAddress,
				this.srcPort
		);
		try {
			this.sock.send(udp_packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected int largestAcceptableFrame() {
		return this.nextDeliverableSeq + this.windowSize;
	}
	
	protected void stopListening() {
		this.shouldListen = false;
	}

	protected void deliverData(byte[] data) {
		System.out.println("Delivering data " + Arrays.toString(data));
	}
}