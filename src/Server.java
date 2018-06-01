import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * 
 */

/**
 * @author Mihir
 *
 */
public class Server {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
			
		CenterServerInterface mtl = new CenterServer("MTL");
		CenterServerInterface lvl = new CenterServer("LVL");
		CenterServerInterface ddl = new CenterServer("DDL");

		addManagersToServer();
		
		Registry registry = LocateRegistry.createRegistry(2964);
		registry.bind("MTL", mtl);
		registry.bind("LVL", lvl);
		registry.bind("DDL", ddl);
				
		System.out.println("Server Started");
	}

    private static void addManagersToServer() throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONArray jsonArray = (JSONArray) parser.parse(new FileReader("resources/managerData.json"));
        for (Object object:jsonArray){
            JSONObject manager  = (JSONObject) object;

        }
    }

}
