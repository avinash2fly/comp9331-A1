/**
 * 
 */
package dao;

import java.io.Serializable;

/**
 * @author avinashgupta
 *
 */
public class Data  implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -75966826505670846L;
	private byte[] data;
	
	
	public String getData() {
		
		return new String(data);
	}
	
	public byte[] getDataByte() {
			
			return data;
		}

	public void setData(String data) {
		
		this.data = data.getBytes();
	}
	
	
}
