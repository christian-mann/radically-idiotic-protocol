
class PacketSender implements Runnable {
	private UDPSocket sock;
	private BlockingQueue<PacketInfo> queue;

	//public PacketSender(UDPSocket sock, BlockingQueue<PacketInfo> queue, PacketReQueuer req) {
	public PacketSender(UDPSocket sock, PacketWindow window) {
		this.sock = sock;
		this.window = window;
	}

	public void run() {
		while (true) {
			PacketInfo packetInfo = window.send_queue.take();

			DatagramPacket rip_packet = packetInfo.makePacket();
			this.sock.send(rip_packet);
			packetInfo.send_count += 1;
			packetInfo.setTimeout(1000); // millis

			window.requeuer.timeoutRequeue(
				packetInfo,
				1000
			);
		}
	}
}
