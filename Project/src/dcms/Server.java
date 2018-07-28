/**
 * 
 */
package dcms;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import rudp.UDPClient;
import rudp.UDPServer;
import rudp.UDPServerListener;

/**
 * @author Mihir
 *
 */
public class Server {
	
	
	private String role;
	
	private JSONObject config;
	
	private JSONObject appConfig;
	
	private UDPServer MTLUDPServer;
	
	private UDPServer DDOUDPServer;
	
	private UDPServer LVLUDPServer;
	
	private DCMSServer MTLServer;
	
	private DCMSServer LVLServer;
	
	private DCMSServer DDOServer;
	
	
	public Server(JSONObject appConfig,JSONObject config) throws IOException {
		this.config = config;
		this.appConfig = appConfig;
		initilizeServers();
		addDefaultData();
	}
	

	private void initilizeServers() throws IOException {
		int MTLServerPort = (int) ((long) ((JSONObject)this.config.get("MTL")).get("port"));
		int DDOServerPort = (int)((long) ((JSONObject)this.config.get("DDO")).get("port"));
		int LVLServerPort = (int)((long) ((JSONObject)this.config.get("LVL")).get("port"));
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
		JSONParser parser = new JSONParser();
		JSONArray config;
		try {
			config = (JSONArray)parser.parse(new FileReader("resources/studentData.json"));
			for(int i=0;i<config.size();i++) {
				JSONObject record = (JSONObject) config.get(i);
				String region = (String)record.get("region");
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
				
				String recordId = (String)record.get("id");
				String firstName = (String)record.get("firstName");
				String lastName = (String)record.get("lastName");
				String[] courseRegistered = ((JSONArray)record.get("coursesRegistered")).toString().replace("},{", " ,").split(" ");
				String status = (String)record.get("status");
				String statusDate = (String)record.get("statusDate");
				regionServer.createSRecord(recordId, firstName, lastName, courseRegistered, status, statusDate, "default");
			}
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			config = (JSONArray)parser.parse(new FileReader("resources/teacherData.json"));
			for(int i=0;i<config.size();i++) {
				JSONObject record = (JSONObject) config.get(i);
				String region = (String)record.get("location");
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
				
				String recordId = (String)record.get("id");
				String firstName = (String) record.get("firstName");
				String lastName = (String) record.get("lastName");
				String address = (String) record.get("address");
				String phone = (String) record.get("phone");
				String specialization = (String) record.get("specialization");
				
				regionServer.createTRecord(recordId, firstName, lastName, address, phone, specialization, region, "default");
				
			}
		} catch (IOException | ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		System.out.println("MTL Server stopped");
	}
	
	public void stopLVLRegion() {
		this.LVLUDPServer.killServer();
		System.out.println("LVL Server stopped");
	}
	
	public void stopDDORegion() {
		this.DDOUDPServer.killServer();
		System.out.println("DDO Server stopped");
	}
	
	
	public String handleRequest(final String region, final String requestData, InetAddress clientHost) {
		DCMSServer regionServer = null;
		JSONObject regionConfig = null;
		switch(region) {
			case "MTL":
				regionServer = this.MTLServer;
				regionConfig = (JSONObject)this.config.get("MTL");
				break;
			case "LVL":
				regionServer = this.LVLServer;
				regionConfig = (JSONObject)this.config.get("LVL");
				break;
			case "DDO":
				regionServer = this.DDOServer;
				regionConfig = (JSONObject)this.config.get("DDO");
				break;	
			
		}
		
		boolean isLeader = false;
		if(((String)regionConfig.get("status")).equals("leader")) {
			isLeader = true;
		}
		
		String result = null;
		
		if(requestData.startsWith("CREATETR")) {
			
			String[] data = requestData.toString().split("\\|");
			result = regionServer.createTRecord(data[1], data[3], data[4], data[5], data[6], data[7], data[8], data[2]);
			if(isLeader) {
				new Thread(new Runnable() {
					
					@Override
					public void run() {
						syncRequest(requestData, region);
					}
				}).start();
			}
			
		} else if(requestData.startsWith("CREATESR")) {
			
			String[] data = requestData.split("\\|");
			result = regionServer.createSRecord(data[1], data[3], data[4], data[5].split(","), data[6], data[7], data[2]);
			
		} else if(requestData.startsWith("RECORDCOUNT")) {
			
			String[] data = requestData.split("\\|");
			result = regionServer.getRecordCount(data[1]);
			
		} else if(requestData.startsWith("EDITRECORD")) {
			
			String[] data = requestData.split("\\|");
			result = regionServer.editRecords(data[2], data[3], data[4], data[1]);
			
		} else if(requestData.startsWith("TRANSFERRECORD")) {
			
			String[] data = requestData.split("\\|");
			result = regionServer.transferRecord(data[1], data[2], data[3]);
			
		} else if(requestData.startsWith("GET_RECORD_COUNT")) {
			
			result = regionServer.getRecordCount(region+"_SERVER");
			
		} else if(requestData.startsWith("TRANSFER_TEACHER")) {
			
			result = regionServer.processRecordTransferRequest(requestData, "Teacher");
			
		} else if(requestData.startsWith("ADD_TEACHER")) {
			
			result = regionServer.processAddTransferRequest(requestData, "Teacher");
			
		} else if(requestData.startsWith("TRANSFER_STUDENT")) {
			
			result = regionServer.processRecordTransferRequest(requestData, "Student");
			
		} else if(requestData.startsWith("ADD_STUDENT")) {
			
			result = regionServer.processAddTransferRequest(requestData, "Student");
			
		} else if(requestData.startsWith("ARE_YOU_ALIVE")) {
			
			result = "YES";
			
		} else if(requestData.startsWith("FAIL")) {
			
			switch(region) {
				case "MTL":
					this.stopMTLRegion();
					break;
				case "LVL":
					this.stopLVLRegion();
					break;
				case "DDO":
					this.stopDDORegion();
					break;	
				
			}

			result = "DONE";
		} else if(requestData.startsWith("OLD_REGION")) {
			String[] data = requestData.split("\\|");
			Set keys = this.appConfig.keySet();
			Iterator iterator = keys.iterator();
			while(iterator.hasNext()) {
				JSONObject currentConfig = (JSONObject)this.appConfig.get(((String)iterator.next()));
				JSONObject regionConfig_temp = (JSONObject)currentConfig.get(data[1]);
				String host = (String)regionConfig_temp.get("host");
				String port = (int)(long)regionConfig_temp.get("port") + "";
				
				if(host.equalsIgnoreCase(data[2]) && port.equalsIgnoreCase(data[3])) {
					regionConfig_temp.put("status", "down");
				}
			}
			result = "OK";
		} else if(requestData.startsWith("CURRENT_STATUS")) {
			String[] data = requestData.split("\\|");
			
			JSONObject newMtlLeader = new JSONObject();
			newMtlLeader.put("host", data[2]);
			newMtlLeader.put("port", Long.parseLong(data[3]));
			newMtlLeader.put("region", data[1]);
			newMtlLeader.put("id", data[4]);
			newMtlLeader.put("status", "leader");
			
			JSONObject newLVLLeader = new JSONObject();
			newLVLLeader.put("host", data[6]);
			newLVLLeader.put("port", Long.parseLong(data[7]));
			newLVLLeader.put("region", data[5]);
			newLVLLeader.put("id", data[8]);
			newLVLLeader.put("status", "leader");
			
			JSONObject newDDOLeader = new JSONObject();
			newDDOLeader.put("host", data[10]);
			newDDOLeader.put("port", Long.parseLong(data[11]));
			newDDOLeader.put("region", data[9]);
			newDDOLeader.put("id", data[12]);
			newDDOLeader.put("status", "leader");
			
			switch(region) {
				case "MTL":
					regionServer.setRegions(new JSONObject[] {newLVLLeader,newDDOLeader});
					break;
				case "LVL":
					regionServer.setRegions(new JSONObject[] {newMtlLeader,newDDOLeader});
					break;
				case "DDO":
					regionServer.setRegions(new JSONObject[] {newMtlLeader,newLVLLeader});
					break;	
				
			}
			
			result = "OK";
		} else if(requestData.startsWith("STATUS")) {
			/*
			result = "";
			Set keys = this.appConfig.keySet();
			Iterator iterator = keys.iterator();
			while(iterator.hasNext()) {
				String server = ((String)iterator.next());
				JSONObject currentConfig = (JSONObject)this.appConfig.get(server);
				Set regionKeys = currentConfig.keySet();
				Iterator regionIterator = regionKeys.iterator();
				while(regionIterator.hasNext()) {
					boolean status = false;
					String regionStr = (String)regionIterator.next();
					
					JSONObject regionConfig1 = (JSONObject)currentConfig.get(regionStr);
					if(!((String)regionConfig1.get("status")).equals("down")) {
						String host = (String)regionConfig1.get("host");
						int port = ((int)(long)regionConfig1.get("port"));
						
						String regionHost = (String)regionConfig.get("host");
						int regionPort = ((int)(long)regionConfig.get("port"));
						
						if(!((host.equalsIgnoreCase(regionHost)) && (port == regionPort))) {
							
						
							UDPClient client = new UDPClient(host, port);
							
							try {
								
								result += server+" status: "+client.sendMessage("RECORDCOUNT|MTL0001")+"\n";
								status = true;
								
							} catch (IOException e) {
								e.printStackTrace();
							}
						
						}
						
					}
					
					if(status) {
						break;
					}
				}
			}
			
			*/
		}
		
		return result;
	}
	
	
	private void syncRequest(String requestData, String region) {
		LoggerFactory.Log(region, "SENDING request to sync to every slave server "+requestData);
		Set keys = this.appConfig.keySet();
		Iterator iterator = keys.iterator();
		while(iterator.hasNext()) {
			JSONObject currentConfig = (JSONObject)this.appConfig.get(((String)iterator.next()));
			Set regionKeys = currentConfig.keySet();
			Iterator regionIterator = regionKeys.iterator();
			while(regionIterator.hasNext()) {
				String regionStr = (String)regionIterator.next();
				if(!regionStr.equals(region)) {
					JSONObject regionConfig = (JSONObject)currentConfig.get(regionStr);
					if(((String)regionConfig.get("status")).equals("slave")) {
						String host = (String)regionConfig.get("host");
						int port = ((int)(long)regionConfig.get("port"));
						UDPClient client = new UDPClient(host, port);
						
						try {
							String response;
							do
							{
								response = client.sendMessage(requestData);
							}while(! response.startsWith("SUCCESS"));
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	

	public static void main(String[] args) {
		try {
			
			JSONParser parser = new JSONParser();
			
			JSONObject config = (JSONObject)parser.parse(new FileReader("resources/config.json"));
			
			Set keys = config.keySet();
			Iterator iterator = keys.iterator();
			while(iterator.hasNext()) {
				String key = (String)iterator.next();
				JSONObject currentConfig = (JSONObject)config.get(key);
				Server server = new Server(config,currentConfig);
				server.startMTLRegion();
				System.out.println(key+" Server MTL Region started.");
				
				server.startDDORegion();
				System.out.println(key+" Server DDO Region started.");
				
				server.startLVLRegion();
				System.out.println(key+" Server LVL Region started.");
			}
		
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}
	}

}
