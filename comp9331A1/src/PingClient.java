/**
 * 
 */
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.CharBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import dao.Packet;
import dao.PacketType;
import utils.Utils;

/**
 * @author avinash
 *
 */
public class PingClient {

	/**
	 * @param args
	 */
	private long min=0,max=0;
	public long getMin() {
		return min;
	}
	public void setMin(long min) {
		if(this.min==0) {this.min=min;return;}
		if(this.min > min)this.min=min;
	}
	public long getMax() {
		return max;
	}
	public void setMax(long max) {
		if(this.max < max)this.max = max;
	}
	public long getAvg() {
		return (max+min)/2;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PingClient client = new PingClient();
		int send=0,receive=0;
//		if(args.length<2){
//			System.out.println("Require IP/host and port");
//			return;
//		}
		try {
			int i=0;
			DatagramSocket clientSocket = new DatagramSocket();
			clientSocket.setSoTimeout(1000);
			DatagramPacket sendPacket = null;
			DatagramPacket receivePacket = null;
			//String ipAddr = args[0]; 
			int port = Integer.parseInt("9876");
			InetAddress IPAddress = InetAddress.getByName("localhost");
			byte[] sendData = new byte[1024];
			byte[] receiveData = new byte[1024];
			String sentence;
			long start,end,total;
			FileInputStream fstream = new FileInputStream("text1.txt");
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            int byteSize=20;
            char[] cbuf = new char[byteSize];
            
			while(br.read(cbuf)!=-1) {
				//DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
				//Date date = new Date();
				sentence = new String(cbuf);
				sendData = sentence.getBytes();
				//sendPacket  = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				Utils utils = new Utils();
				Packet packet = new Packet();
				packet.setHeader(1, 1, PacketType.DATA);
				packet.setData(cbuf);
				sendPacket = utils.getDatagramPacket(packet, "127.0.0.1", port);
				start=System.currentTimeMillis();
				//clientSocket.send(sendPacket);
				Utils.sendPacket(clientSocket, sendPacket);
				send++;
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					
					clientSocket.receive(receivePacket);
					receive++;
					end=System.currentTimeMillis();
					total=end-start;
					System.out.println("ping to "+IPAddress.getHostAddress()+", seq = "+i+", rtt = "+total+" ms");
					client.setMin(total);
					client.setMax(total);
				}
				catch (Exception e) {
					end=System.currentTimeMillis();
					total=end-start;
					System.out.println("ping to "+IPAddress.getHostAddress()+", seq = "+i+", is lost");
				}
				i++;
				long delay = 1000L - total;
				delay = (delay<0)?0:delay;
				Thread.sleep(delay);
				start=0;end=0;total=0;
				break;
			}
			//clientSocket.close();
		int lost = send-receive;
		double lostPercent = ((double)lost/(double)send)*100;
		System.out.println("Packets: Sent = "+send+", Received = "+receive+", Lost = "+lost+" ("+lostPercent+"% loss),");
		System.out.println("Minimum = "+client.getMin()+"ms, Maximum = "+client.getMax()+"ms, Average = "+client.getAvg()+"ms");


		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

}
