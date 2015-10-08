
class PacketInfo {
	int send_count;
	private boolean acked;
	
	private RIPPacket packet;

	// if currentTime > timeout and send_count > 0 and not acked, then packet should be re-sent
	public long timeout;

	public PacketInfo(byte[] data) {
		this(new RIPPacket(data));
	}
	
	public PacketInfo(RIPPacket pack) {
		this.packet = pack;
		this.send_count = 0;
		this.setAcked(false);
		this.timeout = 0;
	}

	public boolean shouldResend() {
		long currentTime = System.currentTimeMillis();
		return currentTime > this.timeout &&
			this.send_count > 0 &&
			this.isAcked() == false;
	}

	public void setTimeout(long millis) {
		long currentTime = System.currentTimeMillis();
		this.timeout = currentTime + millis;
	}

	public boolean isTimedOut() {
		return System.currentTimeMillis() >= this.timeout;
	}

	public boolean isAcked() {
		return acked;
	}

	public RIPPacket getPacket() {
		return packet;
	}

	public void setAcked(boolean acked) {
		this.acked = acked;
	}

	public void clearTimeout() {
		this.timeout = 0;
	}
}
