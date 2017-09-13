import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;

import dao.Packet;
import dao.PacketType;
import utils.Utils;

public class receiver {
	DatagramSocket socket = null;
	Utils utils = new Utils();
	public static NavigableMap<Integer,Packet> receiverMap = new TreeMap<Integer, Packet>();
	//FileOutputStream fos = new FileOutputStream("fileR.txt");
	FileWriter fw = null;
	BufferedWriter bw = null;
	String file_Name="";
	int port=0;
	

public receiver(String args, String args2) {
	// TODO Auto-generated constructor stub
	this.port = Integer.parseInt(args);
	this.file_Name=args2.trim();
}

	public void createAndListenSocket() {
		try {

			socket = new DatagramSocket(port);
			byte[] incomingData = new byte[1024];
			int sequence=154;
			boolean isBreak=false;
			int nextSeq=0;
			while (true) {
				DatagramPacket incomingPacket = new DatagramPacket(incomingData, incomingData.length);
				socket.receive(incomingPacket);

				Packet dataPacket = null;
				try {
					dataPacket = utils.getPacketByStream(incomingPacket);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(!isBreak && dataPacket.getHeader().getFlag()==PacketType.ACK)
					continue;
				int dataSize;
				if(dataPacket.getData()!=null)
					dataSize = dataPacket.getData().getDataByte().length;
				else
					dataSize=0+1;

				if(receiverMap.isEmpty()) {
					if(dataPacket.getHeader().getAck()!=0) {
						nextSeq=dataPacket.getHeader().getAck();
					}
					else {
						nextSeq = 0;
					}
				}
				else
					sequence = dataPacket.getHeader().getAck();
					
				if(dataPacket.getHeader().getFlag()==PacketType.DATA) {
					System.out.println("start");
				}
				if((receiverMap.isEmpty() || (nextSeq) == dataPacket.getHeader().getSequence())) {
					//int seq = dataPacket.getHeader()
					System.out.println(nextSeq);
					receiverMap.put(dataPacket.getHeader().getSequence()+dataSize, dataPacket);
				}
				nextSeq= receiverMap.lastEntry().getKey();
				if(dataPacket.getData()!=null)
				System.out.println("Student object received = "+dataPacket.getData().getData());
				
				if(dataPacket.getHeader().getFlag()==PacketType.FIN)
					isBreak = true;
				else if(isBreak && dataPacket.getHeader().getFlag()==PacketType.ACK)
					break;

				InetAddress IPAddress = incomingPacket.getAddress();
				int port = incomingPacket.getPort();
				PacketType type = PacketType.ACK;
				if(dataPacket.getHeader().getFlag()==PacketType.SYNC)
					type = PacketType.SYN_ACK;
				else if (dataPacket.getHeader().getFlag()==PacketType.FIN)
					type = PacketType.FIN_ACK;
				Packet packet = new Packet();
				packet.setHeader(sequence, nextSeq, type);
				//packet.setHeader(sequence++, dataPacket.getHeader().getSequence(), PacketType.ACK);
				DatagramPacket packet2 = utils.getDatagramPacket(packet, IPAddress.getHostAddress(), port);
				socket.send(packet2);
				//Thread.sleep(2000);
				//System.exit(0);
				//break;
			}
			fw = new FileWriter(file_Name);
			bw = new BufferedWriter(fw);
			Iterator<Entry<Integer, Packet>> it = receiverMap.entrySet().iterator();
			while (it.hasNext())
			{

				Entry<Integer, Packet> item = it.next();
				if(item.getValue().getHeader().getFlag()==PacketType.DATA) {
					//System.out.println(item.getValue().getData().getData());
					byte[] data = trim(item.getValue().getData().getDataByte());
					bw.write(new String(data));
				}
			}


		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException i) {
			i.printStackTrace();
		}
		finally {
			try {
				if (bw != null)
					bw.close();

				if (fw != null)
					fw.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	
	static byte[] trim(byte[] bytes)
	{
	    int i = bytes.length - 1;
	    while (i >= 0 && bytes[i] == 0)
	    {
	        --i;
	    }

	    return Arrays.copyOf(bytes, i + 1);
	}
	public static void main(String[] args) {
		
		receiver server = new receiver(args[0],args[1]);
		server.createAndListenSocket();
	}
}
