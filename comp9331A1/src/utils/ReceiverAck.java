/**
 * 
 */
package utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.Map.Entry;

import dao.Packet;
import dao.PacketType;

/**
 * @author avinashgupta
 *
 */
public class ReceiverAck implements Runnable {

	
	DatagramSocket socket;
	DatagramPacket packet;
	Utils utils=null;
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public ReceiverAck() {
		
		utils = new Utils();
		// TODO Auto-generated constructor stub
		
		
	}
	
	public ReceiverAck(DatagramSocket socket) {
		// TODO Auto-generated constructor stub
		this();
		//super();
		this.socket = socket;
		
	}
	public void receive() throws IOException, ClassNotFoundException{
	
		byte[] incomingData = new byte[1024];
		packet = new DatagramPacket(incomingData, incomingData.length);
		
		while(true) {
			socket.receive(packet);
			Packet dataPacket = utils.getPacketByStream(packet);
			long time_ellapsed = System.currentTimeMillis() - Utils.START_TIME;
			String packetType = dataPacket.getHeader().getFlag().toString();
			int seq_no = dataPacket.getHeader().getSequence();
			//int byteSent = dataPacket.getData().getDataByte().length;
			int ack_no  =dataPacket.getHeader().getAck();
			Utils.receiveMap.put(0, dataPacket);
			Utils.standardPrint("rcv", time_ellapsed, packetType, seq_no, 0, ack_no);
			
			if(dataPacket.getHeader().getFlag()==PacketType.ACK) {
				int ack = dataPacket.getHeader().getAck();
				synchronized (Utils.senderMap) {
					Iterator<Entry<Integer, Packet>> it = Utils.senderMap.entrySet().iterator();
					while (it.hasNext())
					{ 
						Entry<Integer, Packet> item = it.next();
						if(item.getKey() <ack) {
							it.remove();
						}
					}
					
				}
			}
			else if (dataPacket.getHeader().getFlag()==PacketType.SYN_ACK) {
				Utils.ishandShake=true;
			}
			else if(dataPacket.getHeader().getFlag()==PacketType.FIN_ACK) {
				Utils.isFinShake=true;
				break;
			}
			
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			receive();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
