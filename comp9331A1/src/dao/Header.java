/**
 * 
 */
package dao;

import java.io.Serializable;

/**
 * @author avinashgupta
 *
 */
public class Header implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8684713774172285529L;
	private int sequence;
	private int ack;
	private PacketType flag;
	private int repeat=0;
	
	public int getRepeat() {
		return repeat;
	}
	public void setRepeat() {
		this.repeat++;
	}
	public int getSequence() {
		return sequence;
	}
	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	public int getAck() {
		return ack;
	}
	public void setAck(int ack) {
		this.ack = ack;
	}
	public PacketType getFlag() {
		return flag;
	}
	public void setFlag(PacketType flag) {
		this.flag = flag;
	}
	
	
}
