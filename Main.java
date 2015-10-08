import java.io.IOException;
import java.net.InetAddress;

import edu.utulsa.unet.UDPSocket;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		UDPSocket sock = new UDPSocket(9001);
		
		RIPSendingSocket win = new RIPSendingSocket(sock, InetAddress.getByName("localhost"), 9002);
		win.startThreads();
		
		for (int i = 0; i < 100; i++) {
			String data = "Hello" + i;
			win.sendData(data.getBytes());
		}
		win.finish();

		System.out.println("ending");
		//sock.close();
	}

}
