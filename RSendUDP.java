import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketException;

import edu.utulsa.unet.RSendUDPI;
import edu.utulsa.unet.UDPSocket;

public class RSendUDP implements RSendUDPI {

	private String filename;
	private int localPort = 12987;
	private int mode = 0;
	private int modeParameter = 256;
	private InetSocketAddress receiver = new InetSocketAddress("localhost", 12987);
	private long timeout;

	@Override
	public boolean sendFile() {
		UDPSocket udpSock;
		try {
			udpSock = new UDPSocket(this.localPort);
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		}
		
		RIPSendingSocket win = new RIPSendingSocket(udpSock, this.receiver.getAddress(), this.receiver.getPort());
		win.window_size = this.modeParameter;
		win.startThreads();
		
		byte[] data = new byte[1000];
		FileInputStream fin;
		try {
			fin = new FileInputStream(filename);
			while (fin.available() > 0) {
				int amt = fin.read(data);
				byte[] actualData = new byte[amt];
				System.arraycopy(data, 0, actualData, 0, amt);
				win.sendData(actualData);
			}
			win.finish();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (SocketException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
	
		System.out.println("ending");
		//sock.close();
		
		return true;
	}

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

	public InetSocketAddress getReceiver() {
		return receiver;
	}

	public long getTimeout() {
		return timeout;
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
		this.modeParameter = (int) modeParameter;
		return true;
	}


	public boolean setReceiver(InetSocketAddress receiver) {
		this.receiver = receiver;
		return true;
	}

	public boolean setTimeout(long timeout) {
		this.timeout = timeout;
		return true;
	}
	
	public static void main(String[] args) {
		RSendUDP snd = new RSendUDP();
		snd.setMode(1);
		snd.setModeParameter(256);
		snd.setTimeout(3000);
		snd.setFilename("send.txt");
		snd.setLocalPort(23456);
		snd.setReceiver(new InetSocketAddress("localhost", 32456));
		snd.sendFile();
	}
}
