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
		
		
		Registry registry = LocateRegistry.createRegistry(2964);
		registry.bind("MTL", mtl);
		registry.bind("LVL", lvl);
		registry.bind("DDL", ddl);
				
		System.out.println("Server Started");
	}

}
