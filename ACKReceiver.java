
public class ACKReceiver {

	public ACKReceiver(PacketWindow window) {
		this.window = window;
	}

	public receiveAckPacket(RIPPacket rip_pack) {
		if (! rip_pack.isAck()) {
			throw new Exception("ACKReceiver.receiveAckPacket received a non-ack packet");
		}

		int seq = rip_pack.getSEQ();

		PacketInfo info = window.getPacketInfoBySEQ(seq);
		info.acked = true;
		info.clearTimeout();
	}
}
