/**
 * 
 */
package utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;

import dao.Packet;
import dao.PacketType;

/**
 * @author avinashgupta
 *
 */
public class ReceiverAck implements Runnable {

	
	DatagramSocket socket;
	DatagramPacket packet;
	String host;
	int port;
	Utils utils=null;
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public ReceiverAck() {
		
		utils = new Utils();
		// TODO Auto-generated constructor stub
		
		
	}
	
	public ReceiverAck(DatagramSocket socket,String host, int port) {
		// TODO Auto-generated constructor stub
		this();
		//super();
		this.socket = socket;
		this.host=host;
		this.port=port;
		
	}
	public void receive() throws IOException, ClassNotFoundException{
	
		byte[] incomingData = new byte[1024];
		packet = new DatagramPacket(incomingData, incomingData.length);
		int lastAck=-1;
		int ackCount=0;
		while(true) {
			socket.receive(packet);
			Packet dataPacket = utils.getPacketByStream(packet);
			long time_ellapsed = System.currentTimeMillis() - Utils.START_TIME;
			String packetType = dataPacket.getHeader().getFlag().toString();
			int seq_no = dataPacket.getHeader().getSequence();
			//int byteSent = dataPacket.getData().getDataByte().length;
			int ack_no  =dataPacket.getHeader().getAck();
			Utils.receiveMap.put(0, dataPacket);
			if(lastAck!=dataPacket.getHeader().getAck())
			{
				lastAck = dataPacket.getHeader().getAck();
				ackCount=1;
			}
			else {
				ackCount++;
			}
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
			if(ackCount==3) {
				System.out.println("fast transmitstart");
				
				removeTimerAndReSend(ack_no);
				ackCount=0;
				System.out.println("fast transmitstart end");
			}
			
		}
	}
	
	private void removeTimerAndReSend(int ack) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		synchronized (Utils.timers) {
			for(Timer time : Utils.timers) {
				time.cancel();
				time.purge();
			}
			Iterator<Timer> it = Utils.timers.iterator();
			while (it.hasNext())
			{ 
				it.next();
				it.remove();
			}
			
		}
		synchronized (Utils.senderMap) {
			Iterator<Entry<Integer, Packet>> it = Utils.senderMap.entrySet().iterator();
			while (it.hasNext())
			{ 
				Entry<Integer, Packet> item = it.next();
				if(item.getKey() >=ack) {
					DatagramPacket packet = utils.getDatagramPacket(item.getValue(), host, port);
					Utils.sendPacket(socket, packet);
				}
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
