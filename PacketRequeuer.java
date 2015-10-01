import java.util.HashMap;
import java.util.Map;

public class PacketRequeuer {
	private RIPSocket window;

	private Map<Integer, Thread> timeouts;

	public PacketRequeuer(RIPSocket window) {
		this.window = window;
		this.timeouts = new HashMap<>();
	}

	public void timeoutRequeue(final PacketInfo info, long millis) {
		Timeout t = new Timeout(new Runnable() {
			public void run() {
				if (info.isAcked()) {
					return;
				}

				if (! info.isTimedOut()) {
					throw new RuntimeException("wtf, packet is not timed out yet");
				}

				if (info.send_count > 10) {
					throw new RuntimeException("tried 10 times, abandoning this packet");
				}

				window.send_queue.offer(info);
			}
		}, millis);

		Thread thd = new Thread(t);
		this.timeouts.put(info.getPacket().getSEQ(), thd);
		thd.start();
	}

	public void clearTimeout(int seq) {
		Thread t = timeouts.get(seq);
		t.interrupt();
	}

	class Timeout implements Runnable {
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
