/**
 * 
 */
package utils;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import dao.Packet;
import dao.PacketType;

/**
 * @author avinashgupta
 *
 */
public class Utils {
	public static Thread sendThred = null;
	public static boolean ishandShake = false;
	public static boolean isFinShake = false;
	public static Map<Integer, Packet> senderMap = new TreeMap<Integer, Packet>();
	public static Packet lastPacket = null;
	//public static Map<Integer, Packet> receiveMap = new TreeMap<Integer, Packet>();
	public static long START_TIME;
	public static double drop;
	public static Random random  = null;
	public static int delay;
	public static int sync;
	public static ArrayList<Timer> timers = new ArrayList<Timer>();
	public static FileWriter fw = null;
	public static BufferedWriter bw = null;
	public static int dropCount=0;
	public static int delayCount=0;
	public static int AVERAGE_DELAY = 100;
	public static int duplicateAck=0;
	public static int reTransmitted = 0;
	
	
	public DatagramSocket getDatagramSocket(int timeout) throws SocketException {
		DatagramSocket socket = new DatagramSocket();
		if(timeout>0) {
			socket.setSoTimeout(timeout);
		}
		return socket;
		
	}
	
	public DatagramPacket getDatagramPacket(Packet sendData, String address,int port) throws IOException {
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		ObjectOutputStream os = new ObjectOutputStream(outputStream);
		os.writeObject(sendData);
		byte[] data = outputStream.toByteArray();
		//int port = Integer.parseInt("8080");
		InetAddress IPAddress = InetAddress.getByName(address);
		
		DatagramPacket sendPacket = new DatagramPacket(data, data.length, IPAddress, port);
		return sendPacket;
	}

	public Packet getPacketByStream(DatagramPacket receivePacket) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		byte[] data = receivePacket.getData();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		ObjectInputStream is = new ObjectInputStream(in);
		Packet packet= null;
		try {
			packet = (Packet) is.readObject();
		} catch (ClassNotFoundException e) {
			System.err.println("Error while converting stream to object");
			throw e;
		}
		return packet;
	}
	
	
	public static void sendSynPacket(DatagramSocket socket, DatagramPacket packet,PacketType type) throws IOException, ClassNotFoundException {
		Utils utils =  new Utils();
		Packet packet1  =  utils.getPacketByStream(packet);
		//int seq = packet1.getHeader().getSequence();
			socket.send(packet);
			long time_ellapsed = System.currentTimeMillis() - START_TIME;
			String packetType = packet1.getHeader().getFlag().toString();
			//int bytesSent = packet1.getData().getDataByte().length;
			standardPrint("send", time_ellapsed, packetType, packet1.getHeader().getSequence(),0,packet1.getHeader().getAck() );
			TimerTask task = new TimerTask() {
		        public void run() {
		           try {
		        	   if(!ishandShake)
		        		   sendSynPacket(socket, packet,type);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        }
		    };
		    
		    TimerTask task2 = new TimerTask() {
		        public void run() {
		           try {
		        	   if(!isFinShake)
		        		   sendSynPacket(socket, packet,type);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        }
		    };
		    
		    Timer timer = new Timer("Timer");
		     
		    //long delay = 2000L;
		    if(type==PacketType.SYNC)
		    		timer.schedule(task, delay);
		    else
		    		timer.schedule(task2, delay);
		    timers.add(timer);
		//}
	}
	static public synchronized void standardPrint(String process, long time_ellapsed, String packetType, int sync_no, int bytesSent, int ack_no) throws IOException{
		String timeF = String.format("%.3f", time_ellapsed/1000f);
		String out = process + "\t\t" + timeF + "\t"+packetType+"\t"+sync_no+"\t"+bytesSent+"\t"+ack_no+'\n';
		bw.write(out);
		System.out.print(out);
		
	}
	
	public static void sendPacket(DatagramSocket socket, DatagramPacket packet) throws IOException, ClassNotFoundException, InterruptedException {
		
		Utils utils =  new Utils();
		Packet packet1  =  utils.getPacketByStream(packet);
		int seq = packet1.getHeader().getSequence();
		packet1.getHeader().setRepeat();
		if(packet1.getHeader().getRepeat()>1) {
			reTransmitted++;
		}
		String process = "snd";
		//if(senderMap.get(seq)==null) {
			senderMap.put(packet1.getHeader().getSequence(), packet1);
			double randomx = random.nextDouble();
			if(randomx<drop) {
				long time_ellapsed = System.currentTimeMillis() - START_TIME;
				String packetType = packet1.getHeader().getFlag().toString();
				int bytesSent = packet1.getData().getDataByte().length;
				process="drop";
				dropCount++;
				standardPrint(process, time_ellapsed, packetType, packet1.getHeader().getSequence(),bytesSent,packet1.getHeader().getAck() );
			}
			else {
				if(randomx<(2*AVERAGE_DELAY)) {
					Thread.sleep((int) (randomx * 2 * AVERAGE_DELAY));
					delayCount++;
				}
				long time_ellapsed = System.currentTimeMillis() - START_TIME;
				String packetType = packet1.getHeader().getFlag().toString();
				int bytesSent = packet1.getData().getDataByte().length;
				standardPrint(process, time_ellapsed, packetType, packet1.getHeader().getSequence(),bytesSent,packet1.getHeader().getAck() );
				socket.send(packet);
				
			}
			
			TimerTask task = new TimerTask() {
		        public void run() {
		           try {
		        	   if(senderMap.containsKey(seq))
					sendPacket(socket, packet);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		        }
		    };
		    Timer timer = new Timer("Timer");
		     
		    //long delay = 2000L;
		    timer.schedule(task, delay);
		    timers.add(timer);
		//}
	}
	

	

}
