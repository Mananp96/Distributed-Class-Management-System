package corba.client;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import FrontEndApp.FrontEnd;
import FrontEndApp.FrontEndHelper;
import corba.server.RequiredValueException;
import dcms.LoggerFactory;

public class ClientManager {

	private static FrontEnd frontEnd;
	private String managerId;
	private Scanner s;

	public static void main(String[] args) throws IOException, RequiredValueException, NotBoundException {

		ClientManager client = new ClientManager();
		client.menu(args);
	}

	/**
	 * Command Line Interface which gives managers options to perform some of CURD operations on Records
	 * Menu choices includes 
	 * 1. Creating a Teacher Record
	 * 2. Creating a Student Record.
	 * 3. Editing a Record.
	 * 4. Transfer of Record.
	 * 5. Total Record counts.
	 * Menu will continue unless manager exits the program.
	 * 
	 * First manager will enter the manager id and according to it, 
	 * respective server object is assigned using CORBA.
	 * 
	 * @param args
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	public void menu(String[] args) throws RemoteException, NotBoundException {
		String region = "";
		while (true) {

			String managerId = userInput("Enter Manager ID:");
			String serverRegion = managerId.substring(0, 3);
			boolean lengthCheck = (managerId.length() == 7);
			boolean regionCheck = (serverRegion.equalsIgnoreCase("MTL") || serverRegion.equalsIgnoreCase("LVL")
					|| serverRegion.equalsIgnoreCase("DDO"));
			boolean idCheck = false;

			try {
				
				if (lengthCheck) {
					String id = managerId.substring(3, 7);
					Integer.parseInt(id);
					idCheck = true;
				}
			} catch (Exception ignored) {
			}

			if (!(lengthCheck && regionCheck && idCheck)) {
				System.out.println("Invalid Manager ID \n Please try again...");
			} 
			else {
				region = serverRegion.toUpperCase();
				this.managerId = managerId;
				LoggerFactory.Log(this.managerId, "Registering manager");
				
				try{
					
					ORB orb = ORB.init(args, null);
					org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
					NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
					frontEnd = (FrontEnd) FrontEndHelper.narrow(ncRef.resolve_str("fEnd"));
					System.out.println("Obtained a handle on server object: " + frontEnd);
					
				} catch (Exception e) {
					System.out.println("ERROR : " + e);
					//e.printStackTrace(System.out);
				}
				break;
			}
		}

		if (!region.isEmpty()) {

			while (true) {
				System.out.println("********************************");
				System.out.println("1. Add Teacher Record");
				System.out.println("2. Add Student Record");
				System.out.println("3. Get record counts");
				System.out.println("4. Edit Record");
				System.out.println("5. Transfer Record");
				System.out.println("6. Exit");
				System.out.println("********************************");
				String choice = userInput("Enter Choice:");
				switch (choice) {
				
				case "1":
					try {
						this.addTeacherRecord();
					} catch (RequiredValueException e) {
						System.out.println(e);
					}
					System.out.println("Press any key to continue...");
					break;
					
				case "2":
					try {
						this.addStudentRecord();
					} catch (RequiredValueException e) {
						System.out.println(e);
					}
					System.out.println("Press any key to continue...");
					break;
					
				case "3":
					System.out.println("----------Get Record Counts----------");
					System.out.println(frontEnd.getRecordCount(this.managerId));
					System.out.println("Press any key to continue...");
					break;
					
				case "4":
					try {
						this.editRecord();
					} catch (RequiredValueException e) {
						e.printStackTrace();
					}
					System.out.println("Press any key to continue...");
					break;
					
				case "5":
					try {
						this.transferRecord();
					} catch (RequiredValueException e) {
						e.printStackTrace();
					}
					System.out.println("Press any key to continue...");
					break;
					
				case "6":
					System.out.println("Good Bye!");
					break;
					
				default:
					System.out.println("Enter Correct Choice");
					
				}
				if (choice.equals("6")) {
					break;
				}
				userInput("");
			}

		}

	}

	private void addTeacherRecord() throws RemoteException, RequiredValueException {

		LoggerFactory.Log(this.managerId, "Adding Teacher");

		String location = "";
		System.out.println("----------Add Teacher Record----------");
		String firstName = userInput("Enter First Name:");
		String lastName = userInput("Enter Last Name:");
		String address = userInput("Enter Address:");
		String phone = userInput("Enter Phone:");
		String specialization = userInput("Enter Specialization:");
		String loc = userInput("Enter Server location:");

		if (loc.equalsIgnoreCase("MTL")) {
			location = "MTL";
		} else if (loc.equalsIgnoreCase("LVL")) {
			location = "LVL";
		} else if (loc.equalsIgnoreCase("DDO")) {
			location = "DDO";
		} else {
			throw new RequiredValueException("Invalid Location");
		}

		String result = frontEnd.createTRecord(firstName, lastName, address, phone, specialization, location, managerId);

		String str = String.format("FirstName:%s LastName:%s Address:%s Phone:%s Specialization:%s Location:%s",
				firstName, lastName, address, phone, specialization, location);
		
		if (result.equals("Success")) {
			System.out.println("---Teacher Record succesfully added---");
			LoggerFactory.Log(this.managerId, String.format("Teacher record added:%s", str));
		}
		else {
			System.out.println("---Add Teacher Record failed---");
			LoggerFactory.Log(this.managerId, String.format("Teacher record did not add:%s", str));
		}

	}

	private void addStudentRecord() throws RemoteException, RequiredValueException {

		LoggerFactory.Log(this.managerId, "Adding Student");

		String status = "";
		System.out.println("----------Add Student Record----------");
		String firstName = userInput("Enter First Name:");
		String lastName = userInput("Enter Last Name:");
		String courses = userInput("Enter Registered Course:");
		String[] courseRegistered = courses.split(",");
		String stat = userInput("Enter Status");
		if (stat.equalsIgnoreCase("Active")) {
			status = "ACTIVE";
		} else if (stat.equalsIgnoreCase("Inactive")) {
			status = "INACTIVE";
		} else {
			throw new RequiredValueException("Invalid Status");
		}
		String statusDate = userInput("Enter Status Date:");

		String result = frontEnd.createSRecord(firstName, lastName, courseRegistered, status, statusDate,
				this.managerId);

		String str = String.format("FirstName:%s LastName:%s Status:%s StatusDate:%s coursesRegistered:%s", firstName,
				lastName, status, statusDate, String.join(",", courseRegistered));
		if (result.equals("Success")) {
			System.out.println("---Student Record succesfully added---");
			LoggerFactory.Log(this.managerId, String.format("Student record added: %s", str));
			}
		else {
			System.out.println("---Add Student Record failed---");
			LoggerFactory.Log(this.managerId, String.format("Student record did not add: %s", str));
		}

	}

	private void editRecord() throws RemoteException, RequiredValueException {
		System.out.println("----------Edit Record----------");
		String recordId = userInput("Enter Record ID:");
		String fieldName = userInput("Enter Field Name:");
		String newvalue = userInput("Enter New Value:");

		LoggerFactory.Log(this.managerId, String.format("Request to edit record: %s", recordId));

		String result = frontEnd.editRecords(recordId, fieldName, newvalue, this.managerId);

		String str = String.format("RecordID:%s FieldName:%s Value:%s", recordId, fieldName, newvalue);
		
		if (result.equals("Success")) {
			System.out.println("---Record succesfully edited---");
			LoggerFactory.Log(this.managerId, String.format("Record edited:%s", str));
		}
		else {
			System.out.println("---Record edit failed---");
			LoggerFactory.Log(this.managerId, String.format("Record did not edit:%s", str));
		}
	}
	
	private void transferRecord() throws RemoteException, RequiredValueException {

		LoggerFactory.Log(this.managerId, "Transfer Record");

		System.out.println("----------Trasnfer Record----------");
		String recordID = userInput("Enter RecordID:");
		String loc = userInput("Enter New Server location:");
		String result = frontEnd.transferRecord(this.managerId, recordID,loc);

		if (result.equals("Success")) {
			System.out.println("---Record succesfully Transfered---");
			LoggerFactory.Log(this.managerId, String.format("RecordID:%s transfered to", recordID, loc));
		}
		else {
			System.out.println("---Record Transfer failed---");
			LoggerFactory.Log(this.managerId, String.format("RecordID:%s did not transfer to", recordID, loc));
		}
	}

	private String userInput(String var2) {
		System.out.print(var2);
		s = new Scanner(System.in);
		return s.nextLine();
		
	}

}
