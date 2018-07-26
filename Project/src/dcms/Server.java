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
	
	private JSONObject config;
	
	private UDPServer MTLUDPServer;
	
	private UDPServer DDOUDPServer;
	
	private UDPServer LVLUDPServer;
	
	private DCMSServer MTLServer;
	
	private DCMSServer LVLServer;
	
	private DCMSServer DDOServer;
	
	
	public Server(String role) throws FileNotFoundException, IOException, ParseException {
		this.role = role;
		loadConfig("resources/config.json",this.role);
		initilizeServers();
		addDefaultData();
	}
	
	public void loadConfig(String configPath, String role) throws FileNotFoundException, IOException, ParseException {
		JSONParser parser = new JSONParser();
		
		JSONObject config = (JSONObject)parser.parse(new FileReader(configPath));
		this.config = (JSONObject) config.get(role);
	}
	
	private void initilizeServers() throws IOException {
		int MTLServerPort = (int) ((JSONObject)this.config.get("MTL")).get("port");
		int DDOServerPort = (int) ((JSONObject)this.config.get("DDO")).get("port");
		int LVLServerPort = (int) ((JSONObject)this.config.get("LVL")).get("port");
		this.MTLUDPServer = new UDPServer(MTLServerPort, new UDPServerListener() {
			
			@Override
			public String respondRequest(String requestData, InetAddress clientHost) {
				return handleRequest("MTL", requestData, clientHost);
			}
		});
		
		this.DDOUDPServer = new UDPServer(DDOServerPort, new UDPServerListener() {
			
			@Override
			public String respondRequest(String requestData, InetAddress clientHost) {
				return handleRequest("DDO", requestData, clientHost);
			}
		});
		
		this.LVLUDPServer = new UDPServer(LVLServerPort, new UDPServerListener() {
			
			@Override
			public String respondRequest(String requestData, InetAddress clientHost) {
				return handleRequest("LVL", requestData, clientHost);
			}
		});
		
		
		this.MTLServer = new DCMSServer("MTL", new JSONObject[] {(JSONObject)this.config.get("LVL"),(JSONObject)this.config.get("DDO")});
		
		this.LVLServer = new DCMSServer("LVL", new JSONObject[] {(JSONObject)this.config.get("MTL"),(JSONObject)this.config.get("DDO")});
		
		this.DDOServer = new DCMSServer("DDO", new JSONObject[] {(JSONObject)this.config.get("MTL"),(JSONObject)this.config.get("LVL")});
	}
	
	public void addDefaultData() {
		
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
		this.DDOUDPServer.killServer();;
	}
	
	
	public String handleRequest(String region, String requestData, InetAddress clientHost) {
		DCMSServer regionServer = null;
		
		switch(region) {
			case "MTL":
				regionServer = this.MTLServer;
				break;
			case "LVL":
				regionServer = this.LVLServer;
				break;
			case "DDO":
				regionServer = this.DDOServer;
				break;	
			
		}
		
		String result = null;
		
		if(requestData.startsWith("CREATETR")) {
			
			String[] data = requestData.split("|");
			result = regionServer.createTRecord(data[1], data[3], data[4], data[5], data[6], data[7], data[8], data[2]);
			
		} else if(requestData.startsWith("CREATESR")) {
			
			String[] data = requestData.split("|");
			result = regionServer.createSRecord(data[1], data[3], data[4], data[5].split(","), data[6], data[7], data[2]);
			
		} else if(requestData.startsWith("RECORDCOUNT")) {
			
			String[] data = requestData.split("|");
			result = regionServer.getRecordCount(data[1]);
			
		} else if(requestData.startsWith("EDITRECORD")) {
			
			String[] data = requestData.split("|");
			
			
		} else if(requestData.startsWith("TRANSFERRECORD")) {
			
			
			
		} else if(requestData.startsWith("GET_RECORD_COUNT")) {
			result = regionServer.getRecordCount(region+"_SERVER");
		}
		
		return result;
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
