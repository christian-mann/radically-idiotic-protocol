import java.net.DatagramPacket;
import edu.utulsa.unet.UDPSocket; //import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

public class udpreceiver {
	static final int PORT = 9002;
	public static void main(String[] args)
	{
		try
		{
			byte [] buffer = new byte[100];
			UDPSocket socket = new UDPSocket(PORT);
			DatagramPacket packet = new DatagramPacket(buffer,buffer.length);
			while (true) {
				socket.receive(packet);
				InetAddress client = packet.getAddress();
				System.out.println(" Received'"+new String(buffer)+"' from " 
	+packet.getAddress().getHostAddress()+" with sender port "+packet.getPort());
				System.out.println("Bytes: " + Arrays.toString(buffer));
			}
		}
		catch(Exception e){ e.printStackTrace(); }
	}
}
