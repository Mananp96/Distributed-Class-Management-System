import java.io.IOException;

import rudp.UDPClient;

public class ClientUDPTest {

	public static void main(String[] args) {
		
		for(int i=1;i<=100;i++) {
			UDPClient client = new UDPClient("127.0.0.1", 9191);
			try {
				System.out.println(client.sendMessage("Hello"+i));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
