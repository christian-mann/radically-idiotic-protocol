
public class ACKReceiver {

	private RIPSocket window;

	public ACKReceiver(RIPSocket window) {
		this.window = window;
	}

	public void receiveAckPacket(RIPPacket rip_pack) {
		if (! rip_pack.isAck()) {
			throw new RuntimeException("ACKReceiver.receiveAckPacket received a non-ack packet");
		}

		int seq = rip_pack.getSEQ();

		PacketInfo info = window.getPacketInfoBySEQ(seq);
		info.setAcked(true);
		info.clearTimeout();
	}
}
