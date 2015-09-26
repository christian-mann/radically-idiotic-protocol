import java.util.ArrayList;

class PacketWindow extends ArrayList<PacketInfo> {

	int last_ack_received;
	int last_frame_sent;

	int last_frame_received;
	int largest_acceptable_frame;

	int window_size = 100; // bytes

	BlockingQueue<PacketInfo> send_queue;
	PacketSender sender;
	PacketReQueuer requeuer;
	ACKReceiver acker;

	public PacketWindow(UDPSocket sock) {
		this.send_queue = new BlockingQueue<PacketInfo>();
		this.sender = new PacketSender(sock, this);
		this.requeuer = new PacketReQueuer(this);
		this.acker = new ACKReceiver(this);

		new Thread(this.sender).start();
		new Thread(this.requeuer).start();
		new Thread(this.acker).start();
	}

	public static void sendData(byte[] data) {
		if (data.length > sock.getSendBufferSize() - 200) {
			throw new NotImplementedException("Send a smaller packet >:(");
		}

		// split data into chunks
		byte[][] chunks = this.chunkArray(data);

		for (byte[] chunk : chunks) {
			send_queue.put(new PacketInfo(chunk));
		}
	}

    public static int[][] chunkArray(byte[] array, int chunkSize) {
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

}
