import java.io.IOException;
import java.net.InetAddress;

import edu.utulsa.unet.UDPSocket;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		UDPSocket sock = new UDPSocket(9001);
		
		RIPSendingSocket win = new RIPSendingSocket(sock, InetAddress.getByName("localhost"), 9002);
		win.startThreads();
		
		for (int i = 0; i < 10; i++) {
			String data = "";
			for (int j = 0; j < 200; j++) {
				data += j;
			}
			data += "\n";
			win.sendData(data.getBytes());
		}
		win.finish();

		System.out.println("ending");
		//sock.close();
	}

}
