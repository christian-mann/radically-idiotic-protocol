
public class PacketReQueuer extends Runnable {
	private PacketWindow window;

	private Map<Integer, Thread> timeouts;

	public PacketReQueuer(PacketWindow window) {
		this.window = window;
	}

	public void timeoutRequeue(final PacketInfo info, long millis) {
		Timeout t = new Timeout(new Runnable() {
			public void run() {
				if (info.acked) {
					return;
				}

				if (! info.isTimedOut()) {
					throw new Exception("wtf, packet is not timed out yet");
				}

				if (info.send_count > 10) {
					throw new Exception("tried 10 times, abandoning this packet");
				}

				window.send_queue.offer(info);
			}
		}

		Thread thd = new Thread(t);
		this.timeouts.put(info.getSequenceNumber(), thd);
		thd.start();
	}

	public void clearTimeout(int seq) {
		Thread t = timeouts.get(seq);
		t.interrupt();
	}

	class Timeout extends Runnable {
		private Runnable r;
		private long millis;

		public Timeout(Runnable r, long millis) {
			this.r = r;
			this.millis = millis;
		}

		public void run() {
			try {
				Thread.sleep(millis);
				r.run();
			} catch (InterruptedException ex) {
				;
			}
		}
	}
}
