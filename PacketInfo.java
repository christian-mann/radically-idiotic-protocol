
class PacketInfo {
	private int send_count;
	private boolean acked;

	private byte[] payload;

	// if currentTime > timeout and send_count > 0 and not acked, then packet should be re-sent
	private long timeout;

	public PacketInfo(byte[] data) {
		this.payload = data;
		this.send_count = 0;
		this.acked = false;
		this.timeout = 0;
	}

	public boolean shouldResend() {
		long currentTime = System.currentTimeMillis();
		return currentTime > this.timeout &&
			this.send_count > 0 &&
			this.acked == false;
	}

	public void setTimeout(long millis) {
		long currentTime = System.currentTimeMillis();
		this.timeout = currentTime + millis;
	}

	public void isTimedOut() {
		return System.currentTimeMillis() > this.timeout;
	}
}
