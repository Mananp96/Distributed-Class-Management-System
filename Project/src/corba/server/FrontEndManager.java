package corba.server;

import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import FrontEndApp.FrontEnd;
import FrontEndApp.FrontEndHelper;

/**
 * Front End CORBA Server
 * @author Manan Prajapati
 */
public class FrontEndManager {
	private static final String FE_HOST = "localhost";
	private static final int ORB_PORT = 1050;
	public static void main(String args[]) {
		
			try
			{
				//Creating and initializing the ORB
				String orbInitStr = "-ORBInitialPort " + ORB_PORT + " -ORBInitialHost " + FE_HOST;
				String[] orbInitArr = orbInitStr.split(" ");
				ORB orb = ORB.init(orbInitArr, null);
				
				//Getting reference to Root POA and activating POA Manager
				POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
				rootPOA.the_POAManager().activate();
				
				//Creating servant instance and registering it with the ORB
				FrontEndImpl recMgrFE = new FrontEndImpl();
				
				//Making servant instance a CORBA object by registering it with POA
				org.omg.CORBA.Object feObjRef = rootPOA.servant_to_reference(recMgrFE);
				
				//Casting the CORBA reference to a Java reference
				FrontEnd feIfaceObjRef = FrontEndHelper.narrow(feObjRef);
				
				//Getting the root naming context; NameService invokes the transient name service
				org.omg.CORBA.Object rootNameContext = orb.resolve_initial_references("NameService");
			  
				//Casting the CORBA reference to a Java reference
				NamingContextExt nameContextRef = NamingContextExtHelper.narrow(rootNameContext);
			   
				//Binding the Object Reference in Naming
				NameComponent bindPath[] = nameContextRef.to_name("fEnd");
				nameContextRef.rebind(bindPath, feIfaceObjRef);
				
				System.out.println("Front end CORBA server has been started.");
				
				//Waiting for CORBA invocations from clients
				orb.run();
			} 
			catch (Exception e) 
			{
				System.out.println("Exception occurred during Front end CORBA server interaction: " + e.getMessage());
			}
			
			System.out.println("Exiting frontend CORBA server.");
		}
	}