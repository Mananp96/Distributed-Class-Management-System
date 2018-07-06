package DistributedClassManagementSystem;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Scanner;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;


import DistributedClassManagementSystem.RequiredValueException;

public class ClientManager {

	CenterServer server = null;
	private URL url;

	private String managerId;

	public static void main(String[] args) throws IOException, RequiredValueException, NotBoundException {

		ClientManager client = new ClientManager();
		client.menu(args);
	}

	/**
	 * Command Line Interface which gives managers options to perform some of CURD operations on Records
	 * Menu choices includes 
	 * 1. Creating a teacher/student record.
	 * 2. Editing a record.
	 * 3. Transfer of record. 
	 * 4. Total Record counts.
	 * Menu will continue unless manager exits the program.
	 * 
	 * First manager will enter the manager id and according to it, 
	 * respective server object is assigned using CORBA.
	 * 
	 * @param args
	 * @throws MalformedURLException 
	 * @throws RemoteException
	 * @throws NotBoundException
	 */
	
	public void setup(String serverRegion) throws MalformedURLException {
		try {
			this.server = ws_setup(serverRegion);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public CenterServer ws_setup(String serverRegion) throws Exception {
		 QName qname = new QName("http://DistributedClassManagementSystem/", "CenterServerImplService");
		if(serverRegion.equals("MTL")) {
			url = new URL("http://localhost:8080/MTL?wsdl");
          
		}
		else if(serverRegion.equals("LVL")) {
			url = new URL("http://localhost:8080/LVL?wsdl");
          
		}
		else if(serverRegion.equals("DDO")) {
			 url = new URL("http://localhost:8080/DDO?wsdl");
	   
		}
		 Service service = Service.create(url, qname);
         return service.getPort(CenterServer.class);
	
		
	}
	
	public void menu(String[] args) throws RemoteException, NotBoundException {
		String region = "";
		while (true) {

			String managerId = userInput("Enter Manager ID:");

			String serverRegion = managerId.substring(0, 3);

			boolean lengthCheck = (managerId.length() == 7);

			boolean regionCheck = (serverRegion.equalsIgnoreCase("MTL") || serverRegion.equalsIgnoreCase("LVL")
					|| serverRegion.equalsIgnoreCase("DDO"));
			try {
				this.setup(serverRegion);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
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
			} else {
				region = serverRegion.toUpperCase();
				
				this.managerId = managerId;
				
				LoggerFactory.Log(this.managerId, "Registering manager");
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
					System.out.println(server.getRecordCount(this.managerId));
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

		Boolean result = server.createTRecord(firstName, lastName, address, phone, specialization, location, managerId);

		String str = String.format("FirstName:%s LastName:%s Address:%s Phone:%s Specialization:%s Location:%s",
				firstName, lastName, address, phone, specialization, location);
		if (result)
			LoggerFactory.Log(this.managerId, String.format("Teacher record added:%s", str));
		else
			LoggerFactory.Log(this.managerId, String.format("Teacher record did not add:%s", str));

	}

	private void addStudentRecord() throws RemoteException, RequiredValueException {

		LoggerFactory.Log(this.managerId, "Adding Student");

		String status = "";
		System.out.println("----------Add Student Record----------");
		String firstName = userInput("Enter First Name:");
		String lastName = userInput("Enter Last Name:");
		String courses = userInput("Enter Registered Course:");
		String[] courseRegistered = courses.split(",");
		String stat = userInput("Enter Status(Active/InActive):");
		if (stat.equalsIgnoreCase("Active")) {
			status = "ACTIVE";
		} else if (stat.equalsIgnoreCase("Inactive")) {
			status = "INACTIVE";
		} else {
			throw new RequiredValueException("Invalid Status");
		}
		String statusDate = userInput("Enter Status Date:");

		Boolean result = server.createSRecord(firstName, lastName, courseRegistered, status, statusDate,
				this.managerId);

		String str = String.format("FirstName:%s LastName:%s Status:%s StatusDate:%s coursesRegistered:%s", firstName,
				lastName, status, statusDate, String.join(",", courseRegistered));
		if (result)
			LoggerFactory.Log(this.managerId, String.format("Student record added: %s", str));
		else
			LoggerFactory.Log(this.managerId, String.format("Student record did not add: %s", str));

	}

	private void editRecord() throws RemoteException, RequiredValueException {
		System.out.println("----------Edit Record----------");
		String recordId = userInput("Enter Record ID:");
		String fieldName = userInput("Enter Field Name:");
		String newvalue = userInput("Enter New Value:");

		LoggerFactory.Log(this.managerId, String.format("Request to edit record: %s", recordId));

		Boolean result = server.editRecords(recordId, fieldName, newvalue, this.managerId);

		String str = String.format("RecordID:%s FieldName:%s Value:%s", recordId, fieldName, newvalue);
		if (result)
			LoggerFactory.Log(this.managerId, String.format("Record edited:%s", str));
		else
			LoggerFactory.Log(this.managerId, String.format("Record did not edit:%s", str));
	}
	
	private void transferRecord() throws RemoteException, RequiredValueException {

		LoggerFactory.Log(this.managerId, "Transfer Record");

		System.out.println("----------Transfer Record----------");
		String recordID = userInput("Enter RecordID:");
		String loc = userInput("Enter New Server location:");
		Boolean result = server.transferRecord(this.managerId, recordID,loc);

		if (result)
			LoggerFactory.Log(this.managerId, String.format("RecordID:%s transfered to", recordID, loc));
		else
			LoggerFactory.Log(this.managerId, String.format("RecordID:%s did not transfer to", recordID, loc));

	}

	private String userInput(String var2) {
		System.out.print(var2);
		Scanner s = new Scanner(System.in);
		return s.nextLine();
		
	}

}