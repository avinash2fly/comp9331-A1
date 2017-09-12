package dao;

public enum PacketType {
	SYNC("S"), ACK("A"), SYN_ACK("SA"), FIN("F"),
    FIN_ACK("FA"),  DATA("D");
	
	private final String name;    
	
	private PacketType(String val) {
		// TODO Auto-generated constructor stub
		name = val;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return name;
	}
    
    

}
