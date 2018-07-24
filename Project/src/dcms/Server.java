/**
 * 
 */
package dcms;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import rudp.UDPServer;
import rudp.UDPServerListener;

/**
 * @author Mihir
 *
 */
public class Server {
	
	
	private String role;
	
	private JSONObject MTLRegion;
	
	private JSONObject DDORegion;
	
	private JSONObject LVLRegion;
	
	private UDPServer MTLUDPServer;
	
	private UDPServer DDOUDPServer;
	
	private UDPServer LVLUDPServer;
	
	public Server(String role) throws FileNotFoundException, IOException, ParseException {
		this.role = role;
		loadConfig("resources/config.json",this.role);
		initilizeServers();
	}
	
	public void loadConfig(String configPath, String role) throws FileNotFoundException, IOException, ParseException {
		JSONParser parser = new JSONParser();
		
		JSONObject config = (JSONObject)parser.parse(new FileReader(configPath));
		JSONObject currentRoleConfig = (JSONObject) config.get(role);
		this.MTLRegion = (JSONObject) currentRoleConfig.get("MTL");
		this.DDORegion = (JSONObject) currentRoleConfig.get("DDO");
		this.LVLRegion = (JSONObject) currentRoleConfig.get("LVL");
		
	}
	
	private void initilizeServers() throws IOException {
		int MTLServerPort = (int) this.MTLRegion.get("port");
		int DDOServerPort = (int) this.DDORegion.get("port");
		int LVLServerPort = (int) this.LVLRegion.get("port");
		this.MTLUDPServer = new UDPServer(MTLServerPort, new UDPServerListener() {
			
			@Override
			public String respondRequest(String requestData, InetAddress clientHost) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		this.DDOUDPServer = new UDPServer(DDOServerPort, new UDPServerListener() {
			
			@Override
			public String respondRequest(String requestData, InetAddress clientHost) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		this.LVLUDPServer = new UDPServer(LVLServerPort, new UDPServerListener() {
			
			@Override
			public String respondRequest(String requestData, InetAddress clientHost) {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}
	
	public void startMTLRegion() {
		this.MTLUDPServer.start();
	}
	
	public void startDDORegion() {
		this.DDOUDPServer.start();
	}
	
	public void startLVLRegion() {
		this.LVLUDPServer.start();
	}
	
	public void stopMTLRegion() {
		this.MTLUDPServer.killServer();
	}
	
	public void stopLVLRegion() {
		this.LVLUDPServer.killServer();
	}
	
	public void stopDDORegion() {
		this.DDOUDPServer.killServer();
	}
	
	
	public String handleRequest(String region, String requestData, InetAddress clientHost) {
		
		
		
		return null;
	}

	public static void main(String[] args) {
		try {
			Server server = new Server("leader");
			server.startMTLRegion();
			System.out.println("Leader Server MTL Region started.");
			
			server.startDDORegion();
			System.out.println("Leader Server DDO Region started.");
			
			server.startLVLRegion();
			System.out.println("Leader Server LVL Region started.");
			
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

}
