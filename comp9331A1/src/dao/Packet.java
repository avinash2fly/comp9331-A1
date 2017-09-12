/**
 * 
 */
package dao;

import java.io.Serializable;

/**
 * @author avinashgupta
 *
 */
public class Packet implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5754656501280246164L;
	private Header header;
	private Data data;

	
	
	public Header getHeader() {
		return header;
	}
	public void setHeader(int seq, int ack, PacketType flag) {
		
		Header header = new Header();
		header.setAck(ack);
		header.setSequence(seq);
		header.setFlag(flag);
		
		this.header = header;
	}
	public Data getData() {
		return data;
	}
	public void setData(String data) {
		
		Data data1 = new Data();
		data1.setData(data);
		this.data=data1;
		
		
	}
	
public void setData(char[] data) {
		
		Data data1 = new Data();
		data1.setData(new String(data));
		this.data=data1;
		
		
	}

	
}
