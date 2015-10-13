import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;

import edu.utulsa.unet.RReceiveUDPI;
import edu.utulsa.unet.UDPSocket;

public class RReceiveUDP implements RReceiveUDPI {
	
	private int mode = 0;
	private long modeParameter = 256;
	private String filename;
	private int localPort = 12987;
	
	
	public String getFilename() {
		return filename;
	}
	public int getLocalPort() {
		return localPort;
	}
	public int getMode() {
		return mode;
	}
	public long getModeParameter() {
		return modeParameter;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public boolean setLocalPort(int localPort) {
		this.localPort = localPort;
		return true;
	}
	public boolean setMode(int mode) {
		this.mode = mode;
		return true;
	}
	public boolean setModeParameter(long modeParameter) {
		this.modeParameter = modeParameter;
		return true;
	}
	
	@Override
	public boolean receiveFile() {
		UDPSocket sock;
		try {
			sock = new UDPSocket(this.localPort);
		} catch (SocketException e1) {
			e1.printStackTrace();
			return false;
		}
		
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(this.filename);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}
		
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
		return true;
	}
	
	public static void main(String[] args) {
		RReceiveUDP rcv = new RReceiveUDP();
		rcv.setMode(1);
		rcv.setModeParameter(512);
		rcv.setFilename("received.txt");
		rcv.setLocalPort(32456);
		rcv.receiveFile();
	}
	

}
