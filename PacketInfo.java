
class PacketInfo {
	int send_count;
	private boolean acked;
	
	private RIPPacket packet;

	// if currentTime > timeout and send_count > 0 and not acked, then packet should be re-sent
	private long timeout;

	public PacketInfo(byte[] data) {
		this.packet = new RIPPacket(data);
		this.send_count = 0;
		this.setAcked(true);
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
		System.out.println("current time = " + System.currentTimeMillis());
		System.out.println("timeout = " + this.timeout);
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
