import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import edu.utulsa.unet.UDPSocket;

class RIPSocket {

	int last_ack_received;
	int last_frame_sent;

	int last_frame_received;
	int largest_acceptable_frame;

	int window_size = 100; // bytes

	ArrayList<PacketInfo> packet_history;
	BlockingQueue<PacketInfo> send_queue;
	PacketSender sender;
	PacketRequeuer requeuer;
	ACKReceiver acker;
	private UDPSocket sock;
	InetAddress targetAddress;
	int targetUDPPort;

	public RIPSocket(UDPSocket sock, InetAddress target, int port) {
		this.sock = sock;
		this.targetAddress = target;
		this.targetUDPPort = port;
		
		this.packet_history = new ArrayList<>();
		this.send_queue = new ArrayBlockingQueue<>(100);
		this.sender = new PacketSender(sock, this);
		this.requeuer = new PacketRequeuer(this);
		this.acker = new ACKReceiver(this);
	}

	public void sendData(byte[] data) throws InterruptedException, SocketException {
		if (data.length > sock.getSendBufferSize() - 200) {
			throw new RuntimeException("Send a smaller packet >:[");
		}

		// split data into chunks
		byte[][] chunks = RIPSocket.chunkArray(data, this.window_size);

		for (byte[] chunk : chunks) {
			PacketInfo pi = new PacketInfo(chunk);
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

	public void startThreads() {
		new Thread(this.sender).start();
		//new Thread(this.requeuer).start();
		//new Thread(this.acker).start();
	}

}
