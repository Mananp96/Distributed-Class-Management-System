import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.CosNaming.NamingContextPackage.CannotProceed;
import org.omg.CosNaming.NamingContextPackage.NotFound;

import FrontEndApp.FrontEnd;
import FrontEndApp.FrontEndHelper;

public class TestBully {

	
	
	public static void main(String[] args) {

		String orbInitStr = "-ORBInitialPort localhost -ORBInitialHost 1050";
		String[] orbInitArr = orbInitStr.split(" ");
		ORB orb = ORB.init(orbInitArr, null);
		org.omg.CORBA.Object objRef;
		FrontEnd frontEnd = null;
		try {
			objRef = orb.resolve_initial_references("NameService");
			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
			frontEnd = (FrontEnd) FrontEndHelper.narrow(ncRef.resolve_str("fEnd"));
			Thread.sleep(1500);
			for(int i=1;i<=100;i++) {
				String result = frontEnd.createSRecord("Fname"+1, "Lname"+1, new String[] {"courseRegistered"}, "status", "statusDate", "MTL0001");
				System.out.println(result);
				Thread.sleep(100);
			}
			
		} catch (InvalidName e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFound | CannotProceed | org.omg.CosNaming.NamingContextPackage.InvalidName e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
