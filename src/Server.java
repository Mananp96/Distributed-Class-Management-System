import java.io.FileReader;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


/**
 * @author Mihir
 *
 */
public class Server {

    private CenterServerInterface mtl;
    private CenterServerInterface lvl;
    private CenterServerInterface ddo;



    /**
     * @param args
     */
	public static void main(String[] args) throws Exception {
		Server server = new Server();
		server.runServer();
	}

    private void runServer() throws Exception {

        this.mtl = new CenterServer("MTL");
        this.lvl = new CenterServer("LVL");
        this.ddo = new CenterServer("DDO");

        this.addManagersToServer();

        Registry registry = LocateRegistry.createRegistry(2964);
        registry.bind("MTL", this.mtl);
        registry.bind("LVL", this.lvl);
        registry.bind("DDL", this.ddo);

        System.out.println("Server Started");
    }

    private  void addManagersToServer() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(new FileReader("resources/managerData.json"));
        for (Object object:jsonArray){
            JSONObject manager  = (JSONObject) object;
            if(manager.get("managerID").toString().substring(0,3).equalsIgnoreCase("MTL")){
                ((CenterServer) this.mtl).addManagerToList(
                        new Manager(
                                manager.get("managerID").toString(),
                                manager.get("firstName").toString(),
                                manager.get("lastName").toString()));
            }
            else if(manager.get("managerID").toString().substring(0,3).equalsIgnoreCase("LVL")){
                ((CenterServer) this.lvl).addManagerToList(
                        new Manager(
                                manager.get("managerID").toString(),
                                manager.get("firstName").toString(),
                                manager.get("lastName").toString()));
            }
            else if(manager.get("managerID").toString().substring(0,3).equalsIgnoreCase("DDO")){
                ((CenterServer) this.ddo).addManagerToList(
                        new Manager(
                                manager.get("managerID").toString(),
                                manager.get("firstName").toString(),
                                manager.get("lastName").toString()));
            }
        }
    }

}
