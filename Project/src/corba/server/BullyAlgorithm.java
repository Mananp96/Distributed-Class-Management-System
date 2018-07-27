package corba.server;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import dcms.LoggerFactory;
import rudp.UDPClient;

public class BullyAlgorithm extends TimerTask {

	private HashMap<String, ArrayList<Region>> Servers;
	private Region MTLLeader;
	private Region LVLLeader;
	private Region DDOLeader;

	public BullyAlgorithm(Region mtlLeader, Region lvlLeader, Region ddoLeader)
			throws FileNotFoundException, IOException, ParseException {

		initialServersList();

		this.MTLLeader = mtlLeader;
		this.LVLLeader = lvlLeader;
		this.DDOLeader = ddoLeader;

		Timer timer = new Timer();
		timer.schedule(this, 5000, 3000);
	}

	public void run() {
		LoggerFactory.LogServer("bully algorithm triggered");

		final CountDownLatch latch = new CountDownLatch(3);
		new Thread(new Runnable() {

			@Override
			public void run() {
				runRegionBully("MTL");
				latch.countDown();
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				runRegionBully("LVL");
				latch.countDown();
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				runRegionBully("DDO");
				latch.countDown();
			}
		}).start();

		try {
			latch.await();
		} catch (InterruptedException e) {
			LoggerFactory.LogServer("error happend durring running the bully " + e.toString());
			e.printStackTrace();
		}

		LoggerFactory.LogServer("bully algorithm finished");
	}

	private void runRegionBully(String region) {
		LoggerFactory.LogServer("start to run bully algorithm for " + region);
		ArrayList<Region> servers = getRegionServers(region);
		for (Region s : servers) {
			UDPClient client = new UDPClient(s.Host, s.Port);
			try {
				LoggerFactory.LogServer("Sending health check message to the " + s.Host + ":" + s.Port);
				String response = client.sendMessage("ARE_YOU_ALIVE");
				s.IsAlive = response.equalsIgnoreCase("YES");
				LoggerFactory.LogServer("region " + region + " on " + s.Host + ":" + s.Port + " is alive");
			} catch (IOException e) {
				s.IsAlive = false;
				LoggerFactory.LogServer("region " + region + " on " + s.Host + ":" + s.Port + " is NOT alive");
			}
		}

		Region r;
		if (region.equalsIgnoreCase("MTL"))
			r = this.MTLLeader;
		else if (region.equalsIgnoreCase("LVL"))
			r = this.LVLLeader;
		else
			r = this.DDOLeader;

		for (Region s : servers) {
			if (s.IsAlive) {
				r.Host = s.Host;
				r.ID = s.ID;
				r.Port = s.Port;
				r.IsAlive = s.IsAlive;
				LoggerFactory.LogServer(
						"set leader for the region " + region + " on " + s.Host + ":" + s.Port + " is alive");
				break;
			}
		}

	}

	private ArrayList<Region> getRegionServers(String region) {
		return this.Servers.get(region);
	}

	private void initialServersList() throws FileNotFoundException, IOException, ParseException {
		LoggerFactory.LogServer("Initializing the server list in bully algorithm");
		this.Servers = new HashMap<String, ArrayList<Region>>();
		this.Servers.put("MTL", new ArrayList<Region>());
		this.Servers.put("LVL", new ArrayList<Region>());
		this.Servers.put("DDO", new ArrayList<Region>());
		loadConfigFile();

		sortServerList("MTL");
		sortServerList("LVL");
		sortServerList("DDO");

		LoggerFactory.LogServer("Initialzing the servr list finished");

	}

	private void sortServerList(String region) {
		LoggerFactory.LogServer("Sorting " + region + " list based on the ID");
		ArrayList<Region> servers = getRegionServers(region);
		Collections.sort(servers, new Comparator<Region>() {
			@Override
			public int compare(Region lhs, Region rhs) {
				return lhs.ID < rhs.ID ? -1 : (lhs.ID > rhs.ID) ? 1 : 0;
			}
		});
	}

	private void loadConfigFile() throws FileNotFoundException, IOException, ParseException {
		LoggerFactory.LogServer("Loading config file");
		JSONParser parser = new JSONParser();
		JSONObject config = (JSONObject) parser.parse(new FileReader("resources/config.json"));

		LoggerFactory.LogServer("config file loaded");

		LoggerFactory.LogServer("load leader regions");
		JSONObject leader = (JSONObject) config.get("leader");
		loadServers(new JSONObject[] { (JSONObject) leader.get("MTL"), (JSONObject) leader.get("LVL"),
				(JSONObject) leader.get("DDO") });

		LoggerFactory.LogServer("load slave1 regions");
		JSONObject slave1 = (JSONObject) config.get("slave1");
		loadServers(new JSONObject[] { (JSONObject) slave1.get("MTL"), (JSONObject) slave1.get("LVL"),
				(JSONObject) slave1.get("DDO") });

		LoggerFactory.LogServer("load slave2 regions");
		JSONObject slave2 = (JSONObject) config.get("slave2");
		loadServers(new JSONObject[] { (JSONObject) slave2.get("MTL"), (JSONObject) slave2.get("LVL"),
				(JSONObject) slave2.get("DDO") });

	}

	private void loadServers(JSONObject regions[]) {
		for (final JSONObject region : regions) {
			Region s = new Region();
			s.Region = (String) region.get("region");
			s.Host = (String) region.get("host");
			s.Port = (int) (long)region.get("port");
			s.ID = (int) (long)region.get("id");

			ArrayList<Region> servers = this.Servers.get(s.Region);
			servers.add(s);
		}

	}

}
