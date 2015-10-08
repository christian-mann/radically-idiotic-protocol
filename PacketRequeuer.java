import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class PacketRequeuer implements Runnable {
	private RIPSendingSocket window;
	
	private PriorityBlockingQueue<PacketInfo> packets;
	
	

	public PacketRequeuer(RIPSendingSocket window) {
		this.window = window;
		this.packets = new PriorityBlockingQueue<PacketInfo>(100, new Comparator<PacketInfo>() {
			@Override
			public int compare(PacketInfo p1, PacketInfo p2) {
				// compare based on timeout timestamps
				return p1.timeout < p2.timeout ? -1 : 1;
			}
		});
	}

	public void timeoutRequeue(final PacketInfo info) {
		packets.add(info);
	}

	@Override
	public void run() {
		try {
			while (true) {
				// obtain the top packet in the priority queue
				PacketInfo nextPacket = this.packets.take();
				if (nextPacket.isAcked()) {
					// do nothing
				} else if (nextPacket.isTimedOut()) {
					// this packet has timed out
					// re send it
					window.send_queue.put(nextPacket);
				} else {
					// put it back
					this.packets.add(nextPacket);
				}
			}
		} catch (InterruptedException e) {
			System.out.println("Requeuer interrupted");
		}
	}
	
	public synchronized boolean isWaiting() {
		if (this.packets.isEmpty()) {
			return false;
		} else {
			PacketInfo topPacket = this.packets.peek();
			if (topPacket != null) {
//				System.out.printf("Waiting on packet with timestamp %d, expires in %d ms\r",
//					topPacket.timeout,
//					topPacket.timeout - System.currentTimeMillis()
//				);
			}
			return true;
		}
	}
}
