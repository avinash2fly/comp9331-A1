import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Random;

import dao.Packet;
import dao.PacketType;
import utils.ReceiverAck;
import utils.SendFile;
import utils.Utils;


/**
 * 
 */


/**
 * @author avinashgupta
 *
 */
public class sender {

	/**
	 * @param args
	 */
	private DatagramSocket senderSocket;
	Utils utils = new Utils();
	String ipAddress = "127.0.0.1";
	int port = 9876;
	byte[] receiveData = new byte[1024];
	
	
	public void connect(int timer) throws SocketException{	
		
		senderSocket = utils.getDatagramSocket(timer);
		
	}
	public void doHandshake() throws Exception {
		//DatagramPacket receivePacket=null;
		
		PacketType type = PacketType.SYNC;
		boolean handshake=false;
		int seq=Utils.sync;
		seq = 122;
		int sequence=0;
		while(!handshake) {
			if(type==PacketType.ACK)
				handshake=true;
			Packet packet = new Packet();
			if(Utils.receiveMap.get(0)!=null) {
				sequence = Utils.receiveMap.get(0).getHeader().getSequence()+1;
			}
			packet.setHeader(seq++, sequence, type);
			DatagramPacket packet2 = utils.getDatagramPacket(packet, ipAddress, port);
			Utils.sendSynPacket(senderSocket, packet2,PacketType.SYNC);
			//senderSocket.send(packet2);
			while(!Utils.ishandShake) {
				Thread.sleep(100L);
			}
				type = PacketType.ACK;
			
				
		}
		Utils.sync= seq;
		
	}
	
	public static void main(String[] args) {
		
		sender sender = new sender();
		try {
		//Establish connection
		sender.connect(0);
		Utils.START_TIME = System.currentTimeMillis(); 
		Utils.delay = 1000;
		//ReceiveAck
		ReceiverAck receiverAck = new ReceiverAck(sender.senderSocket);
		Thread receiveThread = new Thread(receiverAck);
		receiveThread.start();
		
		// HandShake
		sender.doHandshake();
		File file = new File("text1.txt");
		if(!file.exists()) {
			System.out.println(file.getPath());
			return;
		}
		//SendData;
		Utils.drop = 0.2;
		Utils.random= new Random();
		SendFile sendFile = new SendFile(sender.senderSocket,file , 4, 10, sender.ipAddress, sender.port);
		Thread sendThread = new Thread(sendFile);
		sendThread.start();
		
		
		
		
		
		//Terminate Connection
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		
	}

}
