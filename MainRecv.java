import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import edu.utulsa.unet.UDPSocket;

public class MainRecv {

	public static void main(String[] args) throws IOException, InterruptedException {
		UDPSocket sock = new UDPSocket(32456);
		
		FileOutputStream fos = new FileOutputStream("received.txt");
		
		RIPReceivingSocket rip_sock = new RIPReceivingSocket(sock) {
			@Override
			protected void deliverData(byte[] data) {
				System.out.println("deliverData(" + Arrays.toString(data) + ")");
				
				try {
					fos.write(data);
				} catch (IOException e) {
					this.stopListening();
				}
			}
		};
		rip_sock.listenForever();
		
		System.out.println("Finished listening");
		fos.close();
	}

}
