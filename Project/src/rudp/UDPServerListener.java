package rudp;

import java.net.InetAddress;

public interface UDPServerListener {
	
	String respondRequest(String requestData, InetAddress clientHost);

}
