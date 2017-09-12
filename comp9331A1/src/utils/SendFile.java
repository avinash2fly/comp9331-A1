/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import dao.Packet;
import dao.PacketType;

/**
 * @author avinashgupta
 *
 */
public class SendFile implements Runnable{
	
	DatagramSocket socket;
	File file;
	int window;
	int dataSize;
	String host;
	int port;
	Utils utils = null;
	//byte[] receiveData = new byte[1024];
	
	public SendFile() {
		// TODO Auto-generated constructor stub
		
	}
	public SendFile(DatagramSocket dSocket,File file,int window, int dataSize, String host, int port) {
		// TODO Auto-generated constructor stub
		super();
		socket=dSocket;
		this.file=file;
		this.window=window;
		this.dataSize=dataSize;
		this.host=host;
		this.port=port;
		utils = new Utils();
		
		
	}
	
	public int getNextSequence(int current, Packet packet){
		if(packet.getData()==null) {
			return current+1;
		}
		return current+packet.getData().getDataByte().length;
	}
	public void send() throws IOException, InterruptedException, ClassNotFoundException {
		
		// TODO Auto-generated method stub
		FileInputStream fstream = new FileInputStream(file);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        char[] cbuf = new char[dataSize];
        int packetNum = Utils.sync;
        int ack=0;
        boolean started=true;
        while(br.read(cbuf)!=-1) {
        		//packetNum++;
        		Packet packet = new Packet();
			packet.setData(cbuf);
			if(!Utils.receiveMap.isEmpty()) {
				ack = Utils.receiveMap.get(0).getHeader().getSequence();
				
			}
			if(started) {
				packetNum = Utils.receiveMap.get(0).getHeader().getAck();
				started=false;
			}
			packet.setHeader(packetNum, ack, PacketType.DATA);
			packetNum = getNextSequence(packetNum, packet);
        	 	DatagramPacket dataPacket = utils.getDatagramPacket(packet, host, port);
        	 	Utils.sendPacket(socket, dataPacket);
        	 	Thread.sleep(100L);
        	 	cbuf = new char[dataSize];
        	 	//socket.send(dataPacket);
        	 	while(Utils.senderMap.size()==window ) {
        	 		//Utils.sendThred = Thread.currentThread();
        	 		//System.out.println("Someone notified");
        	 		Thread.sleep(100L);
        	 		//System.out.println(Utils.senderMap.size());
        	 	}
        	 			
        	 	//System.out.println("Ack Received");
        	 	
        }
        while(!Utils.senderMap.isEmpty()) {
        	Thread.sleep(5000L);
        }
        SendFIN();

	}
	
	
	
	private void SendFIN() throws IOException, ClassNotFoundException, InterruptedException {
		PacketType type = PacketType.FIN;
		boolean handshake=false;
		int seq=0;
		while(!handshake) {
			if(type==PacketType.ACK)
				handshake=true;
			Packet packet = new Packet();
			packet.setHeader(seq++, 0, type);
			DatagramPacket packet2 = utils.getDatagramPacket(packet, host, port);
			Utils.sendSynPacket(socket, packet2,PacketType.FIN);
			Thread.sleep(10L);
			//socket.send(packet2);
			while(!Utils.isFinShake) {
				Thread.sleep(100L);
			}
				type = PacketType.ACK;
			
				
		}
		
	}
	private synchronized  void waitThread() throws InterruptedException {
		// TODO Auto-generated method stub
		wait();
		
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			
			send();
		} catch (IOException | InterruptedException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private void start() {
		// TODO Auto-generated method stub

	}

}
