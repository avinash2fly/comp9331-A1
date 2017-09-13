import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
			if(Utils.lastPacket!=null) {
				sequence = Utils.lastPacket.getHeader().getSequence()+1;
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
		//receiver_host_ip
		String r_host = args[0];
		sender.ipAddress = r_host;
		//receiver_port
		int r_port = Integer.parseInt(args[1]);
		sender.port = r_port;
		//file.txt
		String file_Name = args[2];
		
		//MWS
		int byte_MWS = Integer.parseInt(args[3]);
		 
		//MSS
		int MSS = Integer.parseInt(args[4]);
		
		int MWS =  byte_MWS/MSS;
		//timeout
		int timeout = Integer.parseInt(args[5]);
		Utils.delay = timeout;
		//pdrop
		double pdrop  = Double.parseDouble(args[6]);
		Utils.drop = pdrop;
		//seed
		int seed  = Integer.parseInt(args[7]);
		try {
		//Establish connection
		sender.connect(0);
		Utils.START_TIME = System.currentTimeMillis(); 
		
		if(Utils.fw==null) {
			Utils.fw = new FileWriter("Sender_log.txt");
			Utils.bw = new BufferedWriter(Utils.fw);
		}
		//ReceiveAck
		ReceiverAck receiverAck = new ReceiverAck(sender.senderSocket,sender.ipAddress,sender.port);
		Thread receiveThread = new Thread(receiverAck);
		receiveThread.start();
		
		// HandShake
		sender.doHandshake();
		File file = new File(file_Name);
		if(!file.exists()) {
			System.out.println(file.getPath());
			return;
		}
		//SendData;
		
		Utils.random= new Random(seed);
		SendFile sendFile = new SendFile(sender.senderSocket,file , MWS, MSS, sender.ipAddress, sender.port);
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
