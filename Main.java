import java.io.IOException;
import java.net.InetAddress;

import edu.utulsa.unet.UDPSocket;

public class Main {

	public static void main(String[] args) throws IOException, InterruptedException {
		UDPSocket sock = new UDPSocket(9001);
		
		RIPSocket win = new RIPSocket(sock, InetAddress.getByName("localhost"), 9002);
		win.startThreads();
		
		String data = "";
		for (int i = 0; i < 100; i++) {
			data += "Hello" + i;
		}
		win.sendData(data.getBytes());

		//sock.close();
	}

}
