import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.utulsa.unet.UDPSocket;

class RIPSendingSocket {

	int next_seq;

	int window_size = 10; // bytes

	ArrayList<PacketInfo> packet_history;
	BlockingQueue<PacketInfo> send_queue;
	PacketSender sender;
	PacketRequeuer requeuer;
	ACKReceiver ackReceiver;
	UDPSocket sock;
	InetAddress targetAddress;
	int targetUDPPort;
	
	ArrayList<Thread> threads = new ArrayList<>();

	public RIPSendingSocket(UDPSocket sock, InetAddress target, int port) {
		this.sock = sock;
		this.targetAddress = target;
		this.targetUDPPort = port;
		try {
			this.window_size = sock.getSendBufferSize();
			System.out.println("DEBUG: Setting window size to" + 10);
			this.window_size = 256;
			System.out.println("Set window_size to" + this.window_size);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		this.packet_history = new ArrayList<>();
		this.send_queue = new ArrayBlockingQueue<>(100);
		this.sender = new PacketSender(sock, this);
		this.requeuer = new PacketRequeuer(this);
		this.ackReceiver = new ACKReceiver(this);
		
		this.next_seq = 0; // currently defined to start at 0
	}

	public synchronized void sendData(byte[] data) throws InterruptedException, SocketException {

		// split data into chunks
		int chunk_size = Math.min(this.window_size-1, this.sock.getSendBufferSize() - RIPPacket.headerLength());
		byte[][] chunks = RIPSendingSocket.chunkArray(data, chunk_size);

		for (byte[] chunk : chunks) {
			RIPPacket ripPacket = new RIPPacket(chunk);
			ripPacket.sequenceNumber = this.next_seq;
			ripPacket.windowSize = this.window_size;
			
			while (this.lastAckReceived() + this.window_size < this.next_seq + chunk.length) {
				Thread.sleep(100);
			}
			this.next_seq += chunk.length;
			
			PacketInfo pi = new PacketInfo(ripPacket);
			this.packet_history.add(pi);
			send_queue.put(pi);
		}
	}

    public static byte[][] chunkArray(byte[] array, int chunkSize) {
        int numOfChunks = (int)Math.ceil((double)array.length / chunkSize);
        byte[][] output = new byte[numOfChunks][];

        for(int i = 0; i < numOfChunks; ++i) {
            int start = i * chunkSize;
            int length = Math.min(array.length - start, chunkSize);

            byte[] temp = new byte[length];
            System.arraycopy(array, start, temp, 0, length);
            output[i] = temp;
        }

        return output;
    }

	public PacketInfo getPacketInfoBySEQ(int seq) {
		for (PacketInfo info : packet_history) {
			if (info.getPacket().getSEQ() == seq) {
				return info;
			}
		}
		return null;
	}

	int lastAckReceived() {
		int lar = -1;
		for (PacketInfo info : this.packet_history) {
			if (info.isAcked()) {
				lar = info.getPacket().getSEQ() + info.getPacket().payload.length;
			} else {
				break;
			}
		}
		return lar;
	}
	
	public void startThreads() {
		this.threads.add(new Thread(this.sender));
		this.threads.add(new Thread(this.requeuer));
		this.threads.add(new Thread(this.ackReceiver));
		for (Thread t : this.threads) {
			t.start();
		}
	}

	public synchronized void finish() throws InterruptedException {
		// sleep (busy-wait) until send queue is empty and re-send queue is empty
		while (true) {
			if (send_queue.isEmpty() && !this.requeuer.isWaiting()) {
				break;
			} else {
				Thread.sleep(100);
			}
		}
		
		RIPPacket finPacket = new RIPPacket(new byte[] {});
		finPacket.sequenceNumber = this.next_seq;
		finPacket.windowSize = this.window_size;
		finPacket.fin = true;
		
		this.next_seq += 1;
		
		PacketInfo pi = new PacketInfo(finPacket);
		this.packet_history.add(pi);
		send_queue.put(pi);
		
		// sleep (busy-wait) until send queue is empty and re-send queue is empty
		while (true) {
			if (send_queue.isEmpty() && !this.requeuer.isWaiting()) {
				break;
			} else {
				Thread.sleep(100);
			}
		}
		
		for (Thread t : this.threads) {
			t.interrupt();
		}
	}

}
